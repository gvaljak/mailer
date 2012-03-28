package hr.element.etb
package mailer

import net.liftweb.util.Mailer._

trait IDbEtb {
  val DbHost: String
  val DbName: String
  val DbUsername: String
  val DbPassword: String

  def insertMail(
      from: From,
      subject: Subject,
      textBody: PlainPlusBodyType,
      htmlBody: XHTMLMailBodyType,
      addresses: Seq[AddressType],
      attachments: Option[Seq[IAttachmentFile]]): Either[Throwable,Long]
}