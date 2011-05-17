package hr.element.etb.mailer.sql

import org.squeryl.{Schema}
import org.squeryl.PrimitiveTypeMode._

import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column
import java.sql.Timestamp


/**
 * Base entity trait defines common fields for all tables
 */
trait BaseEntity extends KeyedEntity[Long] {
  val id: Long = 0
  @Column("created_at")
  val createdAt = new Timestamp(System.currentTimeMillis)
  @Column("updated_at")
  var updatedAt = new Timestamp(System.currentTimeMillis)
}

/**
 * Represents file_type table with all allowed file types
 */
class FileType(
    val ext: String,
    val alt: Option[String],
    val `type`: String,
    val mime: String) extends BaseEntity {

  def this() = this("",Some(""),"","")

  override def toString =
    "(" + ext + ", " + alt + ", " + `type` + ", " + mime + ")"
}

object Etb extends Schema {
  val fileType = table[FileType]("file_type")

  on(fileType)(ft => declare(
    ft.id is(primaryKey, autoIncremented("seq_file_type")),
    ft.ext is(unique),
    ft.`type` is(indexed)
  ))
}