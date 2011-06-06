package hr.element.etb.mailer

//import net.liftweb.util.Helpers._
import net.liftweb.common._
//import net.liftweb.http._
//import net.liftweb.util._

//import java.io.File
//import org.apache.commons.io.FileUtils

import javax.mail._
//import javax.mail.internet._


import net.liftweb.util.Mailer
//import net.liftweb.util.Mailer._

import hr.element.etb.mailer.sql._

import net.lag.configgy.Configgy


/**
 *  EtbMailer
 *
 *  configPath specifies location of config file
 *  example of config file can be found in src/main/resources
 */


class EtbMailer(configPath: String) {

  import EtbMailer._

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

//  sealed trait MailAttachments

  def send(
      from: From,
      subject: Subject,
      textBody: TextBody,
      htmlBody: Option[HtmlBody],
      addresses: Seq[EmailAddress],
      attachments: Option[Seq[AttachmentFile]]): Either[Exception, _] = {

    db.insertMail(from, subject, textBody, htmlBody, addresses, attachments)

//    sendMail(from, subject, rest: _*)
  }

  def sendFromDb(mailId: Long) = {

    try {
      val mailToSend = db.getMail(mailId)

      val from = mailToSend.getFrom
      val subject = mailToSend.getSubject
      val textBody = mailToSend.getTextBody
      val htmlBody = mailToSend.getHtmlBody

//      val addresses = db.getAddresses

      Right("Success")
    }
    catch {
      case e: Exception => Left(e)
    }
  }

  def error(msg: String) =
    throw new Error(msg)
}

import scala.xml.NodeSeq
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest

object EtbMailer {

  sealed trait MailData

  case class Subject(subject: String) extends MailData

  abstract class EmailAddress extends MailData {
    val address: String
    val getType = this.getClass.getSimpleName
  }

  case class From(address: String) extends EmailAddress

  case class To(address: String) extends EmailAddress

  case class CC(address: String) extends EmailAddress

  case class BCC(address: String) extends EmailAddress

  case class TextBody(text: String) extends MailData
  case class HtmlBody(html: NodeSeq) extends MailData

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

