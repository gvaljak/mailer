package hr.element.etb.mailer

import javax.mail.{Authenticator, PasswordAuthentication}

import net.liftweb.common._
import net.liftweb.actor.SpecializedLiftActor
import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._
import net.liftweb.util.Mailer.MailTypes

import net.lag.configgy.Configgy

import collection.mutable.WrappedArray

import hr.element.etb.mailer.sql._

import org.squeryl.PrimitiveTypeMode._

/**
 *  EtbMailer
 *
 *  configPath specifies location of config file
 *  example of config file can be found in src/main/resources
 */
class EtbMailer(configPath: String) {

  import EtbMailer._

  private val logger = Logger(classOf[EtbMailer])
  lazy val config = getConfig
  lazy val db = getDb

  configureMail()

  def getConfig = {
    Configgy.configure(configPath)
    Configgy.config
  }

  def getDb = {
    val dbParams = config.getConfigMap("mailer.db").getOrElse(error("db block not specified"))

    val dbHost = dbParams.getString("host").getOrElse(error("value db.host not specified!"))
    val dbName = dbParams.getString("dbname").getOrElse(error("value db.name not specified!"))
    val dbUsername = dbParams.getString("user").getOrElse(error("value db.username not specified!"))
    val dbPassword = dbParams.getString("password").getOrElse(error("value db.password not specified!"))

//    println(dbHost, dbName, dbUsername, dbPassword)

    new DbEtbPostgres(dbHost, dbName, dbUsername, dbPassword)
  }

  def configureMail() {

    val authParams = config.getConfigMap("mailer.authentication").getOrElse(error("authentication block not specified"))

    val host = authParams.getString("host").getOrElse(error("value auth.host not specified"))
    val port = authParams.getString("port").getOrElse("25")
    val starttls = authParams.getBool("starttls").getOrElse(false).toString
    val username = authParams.getString("username").getOrElse(error("value db.password not specified!"))
    val password = authParams.getString("password").getOrElse(error("value db.password not specified!"))

    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable",starttls)
    //Set the host name
    System.setProperty("mail.smtp.port", port) // Enable authentication
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty
    Mailer.authenticator = Full(new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(username, password)
    })
  }

  sealed trait ToSend
  case class MailToSend(id: Long) extends ToSend
  case class AddressToSend(id: Long) extends ToSend


  protected class MailSender extends SpecializedLiftActor[ToSend] {
    protected def messageHandler = {
      case MailToSend(id) =>
        sendMailById(id) match {
          case Right(x) => logger.error("Mail successfully sent")
          case Left(e: Exception) =>
            println(e.printStackTrace)
        }
      case AddressToSend(id: Long) =>
        sendToAddress(id)
    }
  }


  protected lazy val mailSender = new MailSender

  def queueMail(
      from: From,
      subject: Subject,
      textBody: PlainPlusBodyType,
      htmlBodyOpt: Option[XHTMLMailBodyType],
      addresses: Seq[AddressType],
      attachments: Option[Seq[AttachmentFile]]) = {

    try {
      val htmlBody =
        htmlBodyOpt match {
          case Some(h) => h
          case _ =>
            XHTMLMailBodyType(<pre>{xml.Utility.escape(textBody.text)}</pre>)
        }

      val id =
        db.insertMail(from, subject, textBody, htmlBody, addresses, attachments) match {
          case Right(id) => id
          case Left(e: Exception) => throw e
        }

      mailSender ! MailToSend(id)

      Right(id)
    } catch {
      case e => Left(e)
    }
  }



  def sendMail(mailData: Mail, addressesFromDb: Seq[sql.Address], attachmentsFromDb: Option[Seq[AttachmentFile]]) = {

    try {

      val from = mailData.getFrom
      val subject = mailData.getSubject
      val textBody = mailData.getTextBody
      val htmlBody = mailData.getHtmlBody


      val addresses =
        addressesFromDb map{add => {
            add.fieldType match {
              case "To" =>
                To(add.address)
              case "CC" =>
                CC(add.address)
              case "BCC" =>
                BCC(add.address)
            }
          }
        }

      val htmlAttach =
        attachmentsFromDb match {
          case Some(atts) =>
            val files =
              atts map{att =>
                PlusImageHolder(att.fileName, att.mimeType, att.body)
              }
            XHTMLPlusImages(htmlBody.text, files: _*)
          case None =>
            XHTMLPlusImages(htmlBody.text)
        }

      val mailTypes: Array[MailTypes] = (Array.empty[MailTypes] :+ textBody :+ htmlAttach) ++ addresses

      Mailer.sendMail(from, subject, mailTypes: _*)

      val ids = addressesFromDb map(_.id)
      db.setSent(ids)

//FIXME: Maknuti jednom
      println("Mail uspješno poslat!")

      Right()
    }
    catch {
      case e: Exception => Left(e)
    }
  }

  def sendMailById(mailId: Long): Either[Exception, Unit] = {

    val mailFromDb = db.getMail(mailId)
    val addressesFromDb = db.getAddresses(mailFromDb)
    val attachmentsFromDb = db.getAttachments(mailFromDb)

    sendMail(mailFromDb, addressesFromDb, attachmentsFromDb)
  }

  def sendToAddress(id: Long) = {
    val addressFromDb = db.getAddressById(id)
    val mailFromDb = db.getMailByAddress(addressFromDb)
    val attachmentsFromDb = db.getAttachments(mailFromDb)

    sendMail(mailFromDb, Seq(addressFromDb), attachmentsFromDb)
  }

  def error(msg: String) =
    throw new Error(msg)
}

import scala.xml.NodeSeq
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest

object EtbMailer {

//  implicit def from2from(f: From): Mailer.From = new Mailer.From(f.address)
//  implicit def subject2subject(s: Subject): Mailer.Subject = new Mailer.Subject(s.subject)
//  implicit def from2from(f: To): Mailer.To = new Mailer.To(f.address)
//  implicit def from2from(f: CC): Mailer.CC = new Mailer.CC(f.address)
//  implicit def from2from(f: BCC): Mailer.BCC = new Mailer.BCC(f.address)
//
//  sealed trait MailData {
//    val getType = this.getClass.getSimpleName
//  }
//
//  case class Subject(subject: String) extends MailData
//
//  abstract class EmailAddress extends MailData {
//    val address: String
//  }
//
//  case class From(address: String) extends EmailAddress
//  case class To(address: String) extends EmailAddress
//  case class CC(address: String) extends EmailAddress
//  case class BCC(address: String) extends EmailAddress

//  case class TextBody(text: String) extends MailData
//  case class HtmlBody(html: NodeSeq) extends MailData

  implicit def address2type(ad: AddressType) =
    new {
      def getAddressTypeName =
        ad match {
          case To(_) => "To"
          case CC(_) => "CC"
          case BCC(_) => "BCC"
        }
    }

  case class AttachmentFile(fileName: String, mimeType: String, bytes: Array[Byte]) {

    def md5(bytes: Array[Byte]): Array[Byte] = {
      MessageDigest.getInstance("MD5").digest(bytes)
    }

    lazy val ExtRegex = """^.*\.([^\.]+)$""".r
    lazy val ExtRegex(ext) = fileName
    lazy val body = Base64.encodeBase64(bytes)
    lazy val hash = md5(body)
    lazy val size = body.size
  }
}