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
      val newMail = Mail(from.address, subject.subject, textBody.text, html)

      mail.insert(newMail)

      val a =
      attachments match {
        case Some(aFL) => {
          for(attFile <- aFL) yield {
            val att = Attachment(attFile.ext, attFile.fileName, attFile.size, attFile.body, attFile.hash, time, time)
            attachment.insert(att)
            val m2a = Mail2Attachments(newMail.id, att.id)
            mail2Attachments.insert(m2a)
            m2a
          }
        }
        case None =>
      }

      for(address <- addresses) yield {
        val m2a = Mail2Addresses(newMail.id, address.getType, address.address, time, None, 0)
        mail2Addresses.insert(m2a)
      }

      "Success"
    }
  }
}