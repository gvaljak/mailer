package hr.element.etb.mailer

import scala.xml.NodeSeq
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest


sealed trait MailData

sealed trait Recipient extends MailData

case class From(from: String) extends MailData
case class Subject(subject: String) extends MailData

case class To(to: String) extends Recipient
case class CC(cc: String) extends Recipient
case class BCC(bcc: String) extends Recipient

case class TextBody(text: String) extends MailData
case class HtmlBody(html: NodeSeq) extends MailData

case class Attachment(fileName: String, mimeType: String, bytes: Array[Byte]) {

  def md5(bytes: Array[Byte]): Array[Byte] = {
      MessageDigest.getInstance("MD5").digest(bytes)
  }

  lazy val ExtRegex = """^.*\.([^\.]+)$""".r
  lazy val ExtRegex(ext) = fileName
  lazy val body = Base64.encodeBase64(bytes)
  lazy val hash = md5(body)
  lazy val size = body.size

}