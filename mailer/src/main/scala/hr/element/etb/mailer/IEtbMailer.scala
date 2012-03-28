package hr.element.etb
package mailer

import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._
import net.liftweb.common.Full
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication


trait IEtbMailer {
  val db: IDbEtb

  protected def configureMail(): (String, String)

  protected def initMail(): Unit = {
    val data = configureMail()

    Mailer.authenticator = Full(new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(data._1, data._2)
    })
  }

  def sendMail(mailData: IMail, addressesFromDb: Seq[IAddress], attachmentsFromDb: Option[Seq[IAttachmentFile]]) = {

    try {

      val from = mailData.getFrom
      val subject = mailData.getSubject
      val textBody = mailData.getTextBody
      val htmlBody = mailData.getHtmlBody


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

      val htmlAttach =
        attachmentsFromDb match {
          case Some(atts) =>
            val files =
              atts map{att =>
                PlusImageHolder(att.fileName, att.mimeType, att.body)
              }
            XHTMLPlusImages(htmlBody.text, files: _*)
          case None =>
            XHTMLPlusImages(htmlBody.text)
        }

      val mailTypes: Array[MailTypes] = (Array.empty[MailTypes] :+ textBody :+ htmlAttach) ++ addresses

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