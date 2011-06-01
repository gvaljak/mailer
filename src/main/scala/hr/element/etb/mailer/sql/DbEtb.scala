package hr.element.etb.mailer.sql

import net.liftweb.util.Mailer._
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest

import java.sql.Timestamp

import Etb._
import org.squeryl.PrimitiveTypeMode._

trait DbEtb {
  val DbHost: String
  val DbName: String
  val DbUsername: String
  val DbPassword: String

  val ExtRegex = """^.*\.([^\.]+)$""".r

  def md5(bytes: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("MD5").digest(bytes)
  }

  def createAttachments(rest: MailTypes*) = {

    val time = new Timestamp(System.currentTimeMillis)

    val attachmentsList =
      rest.collect{
        case XHTMLPlusImages(_, items@_*) =>
          items
      } flatten

    attachmentsList map{att => {
        val ExtRegex(fileType) = att.name
        val body = Base64.encodeBase64(att.bytes)
        val hash = md5(body)
        val size = body.size

        new Attachment(fileType, att.name, size, body, hash, time, time)
      }
    }
  }

  def createMailQueue(from: From, subject: Subject, rest: MailTypes*) = {
    val time = new Timestamp(System.currentTimeMillis)

    val sentTo = rest.collect{case to: To => to} head
    val textBody = rest.collect{case PlainMailBodyType(text) => text} head
    val htmlBody = rest.collect{case XHTMLPlusImages(html, _*) => Some(html.toString)} head

    new MailQueue(from.address, sentTo.address, subject.subject, textBody, htmlBody, Some(time), None, Some(1))
  }

  def transTrye[T](f: => T): Either[Exception,T] =
    try transaction {
      Right(f)
    }
    catch {
      case e: Exception =>
        Left(e)
    }

  def insertData(mail: MailQueue, attachmentsList: Seq[Attachment]) = transTrye {
    mailQueue.insert(mail)
    attachments.insert(attachmentsList)

    println(mailToAttachments)

  }

  def insertMail(from: From, subject: Subject, rest: MailTypes*) {

    val attachmentsList = createAttachments(rest: _*)
    val mailData = createMailQueue(from, subject, rest: _*)

//    println(insertData(mailData, attachmentsList).isLeft)


  }

}