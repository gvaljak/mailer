package hr.element.etb.mailer.sql

import net.liftweb.util.Mailer._


import java.sql.Timestamp

import hr.element.etb.mailer.EtbMailer._

import Etb._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Query


trait DbEtb {
  val DbHost: String
  val DbName: String
  val DbUsername: String
  val DbPassword: String

//  implicit def wrapRichQuery[A,B](q: Query[(A,B)]) =
//    new {
//      def bag(f:((A,B)) => B) =
//        q.toList.groupBy(_._1).mapValues(_.map(f))
//    }

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

  def createAddressList(addresses: Seq[AddressType], mailId: Long, time: Timestamp): Seq[Address] = {
    addresses.map{rec =>
      new Address(mailId, rec.getType, rec.adr, time, None, 0)
    }
  }

  def insertMail(
      from: From,
      subject: Subject,
      textBody: PlainMailBodyType,
      htmlBody: XHTMLMailBodyType,
      addresses: Seq[AddressType],
      attachments: Option[Seq[AttachmentFile]]): Either[Exception,Long] = {

    transTrye {
      val time = new Timestamp(System.currentTimeMillis)
      val newMail = Mail(from.address, subject.subject, textBody.text, htmlBody.text.toString)

      mail.insert(newMail)

      val addToIns =
        for(add <- addresses) yield {
          val newAddress = Address(add.getType, add.adr, time)
          newMail.addresses.assign(newAddress)
        }

      address.insert(addToIns)

      val attToIns =
        attachments match {
          case Some(aFL) => {
            for(attFile <- aFL) yield {
              val newAttachment = Attachment(attFile.ext, attFile.fileName, attFile.size, attFile.body, attFile.hash, time, time)
              attachment.insert(newAttachment)
              newMail.attachments.assign(newAttachment)
            }
          }
          case None =>
            Seq[Mail2Attachments]()
      }

      mail2Attachments.insert(attToIns)

      newMail.id
    }
  }


  def getMail(mailId: Long) = {
    transTrye {
      mail.lookup(mailId)
    } match {
      case Right(Some(x)) => x
      case Right(None) => throw new Exception("No mail with given id: "+mailId)
      case Left(e) => throw e
    }
  }

  def getAddresses(ids: List[Long]) = {
  }

  def getAddresses(mailo: Mail) = {
    transTrye {
      from(mailo.addresses)(a => select(a)).toList
    } match {
      case Right(x)=>
        x.isEmpty match {
          case true => throw new Exception("No addresses for given mail id: "+mailo.id)
          case _ => x
        }
      case Left(e) => throw e
    }
  }

  def getAttachments(mailo: Mail) = {
    transTrye {
      join(mailo.attachments, fileType)((a, ft) =>
        select(a, ft.mime)
        on(a.fileExt === ft.ext)).toList
    } match {
      case Right(x) =>
        x.isEmpty match {
          case true => None
          case false =>
            val attFiles =
              x.map{
                att => AttachmentFile(att._1.filename, att._2, att._1.body)
              }
            Some(attFiles)
        }
      case Left(e) => throw e
    }
  }

}