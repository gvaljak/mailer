package hr.element.etb.mailer.sql

import net.liftweb.util.Mailer._


import java.sql.Timestamp

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

  def createAttachments(rest: MailTypes*) = {

    val time = new Timestamp(System.currentTimeMillis)

    val attachmentsList =
      rest.collect{
        case XHTMLPlusImages(_, items@_*) =>
          items
      } flatten

//    attachmentsList map{att => {
//        val ExtRegex(fileType) = att.name
//        val body = Base64.encodeBase64(att.bytes)
//        val hash = md5(body)
//        val size = body.size
//
//        new Attachment(fileType, att.name, size, body, hash, time, time)
//      }
//    }
  }

  def createAddressList(rest: MailTypes*) = {
    val addressList =
      rest.collect{
          case to: To => to
          case cc: CC => cc
          case bcc: BCC => bcc
      }

    println(addressList)

    addressList
  }

  def createMailQueue(from: From, subject: Subject, rest: MailTypes*) = {
    val time = new Timestamp(System.currentTimeMillis)

    val sentTo = rest.collect{case to: To => to} head
    val textBody = rest.collect{case PlainMailBodyType(text) => text} head
    val htmlBody = rest.collect{case XHTMLPlusImages(html, _*) => Some(html.toString)} head

    new Mail(from.address, subject.subject, textBody, htmlBody)
  }

  def insertData(mailData: Mail, attachmentList: Seq[Attachment]) =
    transTrye {
      println("insertam mail")
      mail.insert(mailData)
      attachmentList foreach(att => attachment.insert(att) )

      println("!!")
      println(attachmentList)
      attachmentList foreach(a => println(a.id))
    }

  def insertMail(from: From, subject: Subject, rest: MailTypes*) = {

    val attachmentList = createAttachments(rest: _*)
    val addressList = createAddressList(rest: _*)
    val mailData = createMailQueue(from, subject, rest: _*)


    insertData(mailData, attachmentList).isLeft


  }

}