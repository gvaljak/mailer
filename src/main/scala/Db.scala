package hr.element.etb.mailer.sql

import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.{Session, Schema, SessionFactory}
import org.squeryl.adapters.PostgreSqlAdapter
import net.liftweb.squerylrecord.RecordTypeMode._

class Db {
  Class.forName("org.postgresql.Driver")

  val host = "sk.cehtunger-debian.debian-local.net"
  val name = "etb_00"
  val user = "etb"
  val pass = "etbxhyper"

//Lift squeryl
  SquerylRecord.initWithSquerylSession(Session.create(
    java.sql.DriverManager.getConnection(
        "jdbc:postgresql://"+ host +"/"+ name, user, pass),
    new PostgreSqlAdapter
  ))

//Squeryl
//  SessionFactory.concreteFactory = Some(()=>
//    Session.create(
//      java.sql.DriverManager.getConnection("..."),
//      new PostgreSqlAdapter))

  import Etb._

//  transaction {
//    val jpg =
//      from(fileType)(row => where(row.id gt 0) select(row))
////    val jpg =
////      from(fileType)(row => where(row.id > 0) select(row))
//    jpg foreach(r => println(r))
//  }

//  val jpg =
    transaction {
//       Etb.fileType.insert(new FileType("jpg", Some("jpeg"), "image", "image/jpg"))
       Etb.fileType.insert(new FileType("gif", None, "image", "image/gif"))
//       Etb.fileType.insert(new FileType("pdf", None, "document", "application/pdf"))
//      fileType.where(row => row.ext === "jpg").single
//        from(fileType)(row => where(row.ext === "jpg") select(row))
    }
//  println(jpg.getClass) //class org.squeryl.dsl.boilerplate.Query1
}

//import net.liftweb.record.{MetaRecord, Record}
//import net.liftweb.record.field._
//import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import org.squeryl.annotations.Column

import org.squeryl.KeyedEntity
import java.sql.Timestamp


class BaseEntity extends KeyedEntity[Long] {
  val id: Long = 0
  @Column("created_at")
  val createdAt = new Timestamp(System.currentTimeMillis)
  @Column("updated_at")
  var updatedAt = new Timestamp(System.currentTimeMillis)
}

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