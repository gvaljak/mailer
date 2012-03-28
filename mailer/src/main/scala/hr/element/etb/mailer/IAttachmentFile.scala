package hr.element.etb
package mailer

import java.security.MessageDigest

trait IAttachmentFile {
  val fileName: String
  val mimeType: String
  val bytes: Array[Byte]

  def md5(bytes: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance("MD5").digest(bytes)
  }

  lazy val ExtRegex = """^.*\.([^\.]+)$""".r
  lazy val ExtRegex(ext) = fileName
  lazy val body = bytes
  lazy val hash = md5(body)
  lazy val size = body.size
}