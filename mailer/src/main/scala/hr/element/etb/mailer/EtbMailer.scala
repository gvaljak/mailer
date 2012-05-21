package hr.element.etb.mailer

import EtbMailer.AttachmentFile
import hr.element.etb.mailer.sql.DbEtbPostgres
import hr.element.etb.mailer.EtbMailer
import hr.element.etb.mailer.IAttachmentFile
import hr.element.etb.mailer.IEtbMailer
import net.lag.configgy.Configgy
import net.liftweb.actor.SpecializedLiftActor
import net.liftweb.common.Logger
import net.liftweb.util.Mailer.AddressType
import net.liftweb.util.Mailer.BCC
import net.liftweb.util.Mailer.CC
import net.liftweb.util.Mailer.From
import net.liftweb.util.Mailer.PlainPlusBodyType
import net.liftweb.util.Mailer.Subject
import net.liftweb.util.Mailer.To
import net.liftweb.util.Mailer.XHTMLMailBodyType

/**
 *  EtbMailer
 *
 *  configPath specifies location of config file
 *  example of config file can be found in src/main/resources
 */
class EtbMailer(configPath: String) extends IEtbMailer{

  import EtbMailer._

  private val logger = Logger(classOf[EtbMailer])
  lazy val config = getConfig
  lazy val db = getDb

  val authParams = config.getConfigMap("mailer.authentication").getOrElse(error("authentication block not specified"))

  val host = authParams.getString("host").getOrElse(error("value auth.host not specified"))
  val port = authParams.getString("port").getOrElse("25")
  val starttls = authParams.getBool("starttls").getOrElse(false).toString
  val username = authParams.getString("username").getOrElse(error("value db.password not specified!"))
  val password = authParams.getString("password").getOrElse(error("value db.password not specified!"))


  initMail()

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
          case To(_, _) => "To"
          case CC(_, _) => "CC"
          case BCC(_, _) => "BCC"
        }
    }

  case class AttachmentFile(val fileName: String, val mimeType: String, bytes: Array[Byte])
    extends IAttachmentFile
}