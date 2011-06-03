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
  ext : String,
  alt : Option[String],
  `type` : String,
  mime : String) extends BaseEntity {

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

case class Mail(
    @Column("sent_from")
    val sentFrom : String,
    val subject : String,
    @Column("text_body")
    val textBody : String,
    @Column("html_body")
    val htmlBody : Option[String]) extends BaseEntity {

  //  lazy val attachments = Etb.mailToAttachments.left(this)

  def this() = this("", "", "", Some(""))
}

case class Mail2Attachments(
  @Column("mail_id") val mailId : Long,
  @Column("attachment_id") val attachmentsId : Long) extends BaseEntity

case class Mail2Addresses(
    @Column("mail_id")
    val mailId: Long,
    @Column("field_type")
    val fieldType: String,
    val address: String,
    @Column("queued_at")
    val queuedAt: Timestamp,
    @Column("sent_at")
    val sentAt: Option[Timestamp],
    val bounced: Int) extends BaseEntity {

  def this() = this(0L, "", "", new Timestamp(0), Some(new Timestamp(0)), 0)
}

object Etb extends Schema {
  val fileType = table[FileType]("file_type")
  val attachment = table[Attachment]("attachment")
  val mail = table[Mail]("mail")
  val mail2Attachments = table[Mail2Attachments]("mail2attachments")
  val mail2Addresses = table[Mail2Addresses]("mail2addresses")

//  val mailToAttachments =
//    manyToManyRelation(mailQueue, attachments, "mail2attachments").
//      via[Mail2Attachments]((m, a, ma) => (m.id === ma.mailId, a.id === ma.attachmentsId))

  on(fileType)(ft => declare(
    ft.id is (primaryKey, autoIncremented("seq_file_type")),
    ft.ext is (unique),
    ft.`type` is (indexed)))

  on(attachment)(att => declare(
    att.id is (primaryKey, autoIncremented("seq_attachment"))))

  on(mail)(mq => declare(
    mq.id is (primaryKey, autoIncremented("seq_mail"))))

  on(mail2Attachments)(m2at => declare(
    m2at.id is(primaryKey, autoIncremented("seq_mail2attachments"))
  ))

  on(mail2Addresses)(m2ad => declare(
    m2ad.id is(primaryKey, autoIncremented("seq_mail2addresses"))
  ))
}