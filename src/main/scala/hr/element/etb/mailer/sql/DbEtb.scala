package hr.element.etb.mailer.sql

//import net.liftweb.util.Mailer._


import java.sql.Timestamp

import hr.element.etb.mailer.EtbMailer._

import Etb._
import org.squeryl.PrimitiveTypeMode._

trait DbEtb {
  val DbHost: String
  val DbName: String
  val DbUsername: String
  val DbPassword: String

  def transTrye[T](f: => T): Either[Exception,T] =
    try transaction {
      Right(f)
    }
    catch {
      case e: Exception =>
        Left(e)
    }

  def createAttachments(attachmentList: Option[Seq[AttachmentFile]], mailId: Long, time: Timestamp) = {
    attachmentList map{_.map{att =>
      new Attachment(att.ext, att.fileName, att.size, att.body, att.hash, time, time)
    }}
  }

  def createAddressList(addresses: Seq[EmailAddress], mailId: Long, time: Timestamp): Seq[Mail2Addresses] = {
    addresses.map{rec =>
      new Mail2Addresses(mailId, rec.getType, rec.address, time, None, 0)
    }
  }

  def insertMail(
      from: From,
      subject: Subject,
      textBody: TextBody,
      htmlBody: Option[HtmlBody],
      addresses: Seq[EmailAddress],
      attachments: Option[Seq[AttachmentFile]]) = {

    transTrye {
      val time = new Timestamp(System.currentTimeMillis)
      val html = for{h <- htmlBody} yield h.html.toString
      val newMail = new Mail(from.address, subject.subject, textBody.text, html)

      mail.insert(newMail)

      val addressList = createAddressList(addresses, newMail.id, time)
      addressList.foreach(add => mail2Addresses.insert(add))

      val attachmentList = createAttachments(attachments, newMail.id, time)
      attachmentList.map(_.foreach(att => attachment.insert(att)))

      "asdfsfd"
    }
  }
}