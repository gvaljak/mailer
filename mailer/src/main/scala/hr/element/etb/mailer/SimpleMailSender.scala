package hr.element.etb
package mailer

case class SimpleMail(
  val sentFrom : String,
  val subject : String,
  val textBody : String,
  val htmlBody : String,
  val replyTo: Option[String] = None) extends IMail

case class SimpleAddress(
  val fieldType: String,
  val address: String) extends IAddress

case class SimpleAttachment(
  val fileName: String,
  val mimeType: String,
  val bytes: Array[Byte]) extends IAttachmentFile

class SimpleMailSender(
    val host: String,
    val port: String,
    val starttls: String,
    val username: String,
    val password: String) extends IEtbMailer {

  lazy val db = null

  initMail()

}