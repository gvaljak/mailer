package hr.element.etb
package mailer

case class SimpleMail(
  val sentFrom : String,
  val subject : String,
  val textBody : String,
  val htmlBody : String) extends IMail

case class SimpleAddress(
  val fieldType: String,
  val address: String) extends IAddress

case class SimpleAttachment(
  val fileName: String,
  val mimeType: String,
  val bytes: Array[Byte]) extends IAttachmentFile

class SimpleMailSender extends IEtbMailer {

  lazy val db = null

  initMail()

  def configureMail() = {

    val host = ""
    val port = "25"
    val starttls = "false"
    val username = ""
    val password = ""

    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable",starttls)
    //Set the host name
    System.setProperty("mail.smtp.port", port) // Enable authentication
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty

    (username, password)

  }

}