package hr.element.etb
package mailer

import Mailer._
import net.liftweb.common.Full
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

import scala.xml._


trait IEtbMailer {
  val db: IDbEtb

  val host: String
  val port: String
  val starttls: String
  val username: String
  val password: String


  protected def initMail(): Unit = {
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

  def sendMail(mailData: IMail, addressesFromDb: Seq[IAddress], attachmentsFromDb: Option[Seq[IAttachmentFile]]) = {

    try {

      val from = mailData.getFrom
      val subject = mailData.getSubject
      val textBody = mailData.getTextBody
      val htmlBody = mailData.getHtmlBody
      val replyTo = mailData.getReplyTo


      val addresses: Seq[AddressType] =
        addressesFromDb map{add => {
            add.fieldType match {
              case "To" =>
                To(add.address)
              case "CC" =>
                CC(add.address)
              case "BCC" =>
                BCC(add.address)
            }
          }
        }

      val htmlAttach: Option[MailBodyType] =
        attachmentsFromDb match {
          case Some(atts) =>
            val html = htmlBody.map(_.text).getOrElse("<pre>" + textBody.text + "</pre>")
            val files =
              atts map{att =>
                PlusImageHolder(att.fileName, att.mimeType, att.body)
              }
            Some(XHTMLPlusImages(html, files: _*))
          case None =>
            htmlBody
        }

      val mailTypes: Array[MailTypes] = (Array.empty[MailTypes] :+ textBody) ++ addresses ++ htmlAttach ++ replyTo

      mailTypes.foreach(println)

      Mailer.blockingSendMail(from, subject, mailTypes: _*)

//FIXME: Maknuti jednom
      addresses foreach{a => println("Mail uspješno poslat: " + a.address)}

      Right()
    }
    catch {
      case e: Exception => Left(e)
    }
  }


}