package hr.element.etb.mailer.sql

import org.squeryl.{ Schema }
import org.squeryl.PrimitiveTypeMode._

import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column
import java.sql.Timestamp

/**
 * Base entity trait defines common fields for all tables
 */
trait BaseEntity extends KeyedEntity[Long] {
  val id : Long = 0
  @Column("created_at")
  val createdAt = new Timestamp(System.currentTimeMillis)
  @Column("updated_at")
  var updatedAt = new Timestamp(System.currentTimeMillis)
}

/**
 * Represents file_type table with all allowed file types
 */
case class FileType(
  val ext : String,
  val alt : Option[String],
  val `type` : String,
  val mime : String) extends BaseEntity {

  def this() = this("", Some(""), "", "")

  override def toString =
    "("+ext+", "+alt+", "+`type`+", "+mime+")"
}

case class Attachment(
  @Column("file_type_ext") val fileExt : String,
  val filename : String,
  val size : Int,
  val body : Array[Byte],
  val hash : Array[Byte],
  @Column("mod_time") val modTime : Timestamp,
  @Column("uploaded_at") val uploadedAt : Timestamp) extends BaseEntity {

  //  lazy val mails = Etb.mailToAttachments.right(this)
}

class MailQueue(
  @Column("sent_from") val sentFrom : String,
  @Column("sent_to") val sentTo : String,
  val subject : String,
  @Column("text_body") val textBody : String,
  @Column("html_body") val htmlBody : Option[String],
  @Column("queued_at") val queuedAt : Option[Timestamp],
  @Column("sent_at") val sentAt : Option[Timestamp],
  val bounced : Option[Int]) extends BaseEntity {

  //  lazy val attachments = Etb.mailToAttachments.left(this)

  def this() = this("", "", "", "", Some(""), Some(new Timestamp(System.currentTimeMillis)), Some(new Timestamp(System.currentTimeMillis)), Some(0))
}

class Mail2Attachments(
  @Column("mail_id") val mailId : Long,
  @Column("attachments_id") val attachmentsId : Long) extends BaseEntity

object Etb extends Schema {
  val fileType = table[FileType]("file_type")
  val attachments = table[Attachment]("attachments")
  val mailQueue = table[MailQueue]("mail_queue")
  //  val mail2Attachments = table[mail2attachments]

  val mailToAttachments =
    manyToManyRelation(mailQueue, attachments, "mail2attachments").
      via[Mail2Attachments]((m, a, ma) => (m.id === ma.mailId, a.id === ma.attachmentsId))

  on(fileType)(ft => declare(
    ft.id is (primaryKey, autoIncremented("seq_file_type")),
    ft.ext is (unique),
    ft.`type` is (indexed)))

  on(attachments)(att => declare(
    att.id is (primaryKey, autoIncremented("seq_attachments"))))

  on(mailQueue)(mq => declare(
    mq.id is (primaryKey, autoIncremented("seq_mail_queue"))))

  //  on(mail2attachments)(m2a => declare(
  //    m2a.id is(primaryKey, autoIncremented("seq_mail2attachments"))
  //  ))
}