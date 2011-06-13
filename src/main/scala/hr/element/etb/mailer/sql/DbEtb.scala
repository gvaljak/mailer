package hr.element.etb.mailer.sql

import collection.IterableLike

import net.liftweb.util.Mailer._

import scala.collection.immutable.IndexedSeqMap

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



  def transTrye[T](f: => T): Either[Exception, T] =
    try transaction {
      Right(f)
    }
    catch {
      case e: Exception => Left(e)
    }

  def trans[A <: IterableLike[_,_], B](f: => A)(a: A => B)(t: => B): B = {
    transTrye {f}
    match {
      case Right(x) =>
        x.isEmpty match {
          case true => t
          case _ => a(x)
        }
      case Left(e) => throw e
    }
  }

  def transGet[T <: IterableLike[_,_]](f: => T)(msg: String) =
    trans {f} {(x: T) => x} {throw new Exception(msg)}


  def transOptGet[T <: IterableLike[_,_]](f: => T) =
    trans{f} {(x: T) => Some(x):Option[T]} {None:Option[T]}



  def transGetOne[T](f: => Option[T])(msg: String): T = {
    transTrye {f}
      match {
        case Right(x) =>
          x match {
            case Some(s) => s
            case None => throw new Exception(msg)
          }
        case Left(e) => throw e
    }
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

  def setAllSent(mailId: Long) = {

    val time = new Timestamp(System.currentTimeMillis)

    transTrye {
      update(address)(a =>
        where(a.mailId === mailId)
        set(a.sentAt := Some(time))
      )
    }
  }

  def setSent(addressId: Long) = {

    val time = new Timestamp(System.currentTimeMillis)

    transTrye {
      update(address)(a =>
        where(a.id === addressId)
        set(a.sentAt := Some(time))
      )
    }
  }


  def insertMail(
      from: From,
      subject: Subject,
      textBody: PlainPlusBodyType,
      htmlBody: XHTMLMailBodyType,
      addresses: Seq[AddressType],
      attachments: Option[Seq[AttachmentFile]]): Either[Exception,Long] = {

    transTrye {
      val time = new Timestamp(System.currentTimeMillis)
      val newMail = Mail(from.address, subject.subject, textBody.text, htmlBody.text.toString)

      mail.insert(newMail)

      val addToIns =
        for(add <- addresses) yield {
          val newAddress = Address(add.getAddressTypeName, add.adr, time)
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
    transGetOne {
      mail.lookup(mailId)
    }("No mail with given id: " + mailId)
  }

  def getAddresses(mailo: Mail) = {
    transGet {
      from(mailo.addresses)(a => select(a)).toList
    }("No addresses for given mail id: " + mailo.id)
  }

  def getAddressById(id: Long): Address = {
    transGetOne {
      address.lookup(id)
    }("No address with given id: " + id)
  }

  def getMailByAddress(addro: Address) = {
    transOptGet {
      from(addro.mailo)(m => select(m)).toList
    }
  }


  def getAttachments(mailo: Mail): Option[Seq[AttachmentFile]] = {
    val attachments =
      transOptGet{
        join(mailo.attachments, fileType)((a, ft) =>
          select(a, ft.mime)
          on(a.fileExt === ft.ext)).toMap
      }

    attachments map{_.map{att => new AttachmentFile(att._1.filename, att._2, att._1.body)} toSeq}
  }

}