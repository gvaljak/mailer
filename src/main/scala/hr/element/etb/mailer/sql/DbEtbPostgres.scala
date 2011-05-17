package hr.element.etb.mailer.sql

import org.squeryl.{Session, Schema, SessionFactory}
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.PrimitiveTypeMode._

import Etb._
import net.liftweb.util.Mailer._

class DbEtbPostgres(
    host: String,
    dbName: String,
    username: String,
    password: String) extends DbEtb {

  Class.forName("org.postgresql.Driver")

//  val DbHost = "sk.cehtunger-debian.debian-local.net"
//  val DbName = "etb_00"
//  val DbUsername = "etb"
//  val DbPassword = "etbxhyper"

  val DbHost = host
  val DbName = dbName
  val DbUsername = username
  val DbPassword = password

/**
 *  Lift squeryl
 */
//  SquerylRecord.initWithSquerylSession(Session.create(
//    java.sql.DriverManager.getConnection(
//        "jdbc:postgresql://"+ DbHost +"/"+ DbName, DbUsername, DbPassword),
//    new PostgreSqlAdapter
//  ))

/**
 *  Squeryl
 */
  SessionFactory.concreteFactory = Some(()=>
    Session.create(
      java.sql.DriverManager.getConnection(
          "jdbc:postgresql://"+ DbHost +"/"+ DbName, DbUsername, DbPassword),
      new PostgreSqlAdapter))


  def insertMail(from: From, subject: Subject, rest: MailTypes*) {
    rest.foreach(mailType => {
      mailType match {
        case XHTMLPlusImages(html, inlineImg) =>
          println(inlineImg)
        case x =>
          println(x.getClass)
      }
    })
  }


/********************
 *     EXAMPLES
 ********************/
/*
  transaction {
    val jpg =
      from(fileType)(row => where(row.id gt 0) select(row))
    val jpg =
      from(fileType)(row => where(row.id > 0) select(row))
    jpg foreach(r => println(r))
  }

  val jpg =
    transaction {
       Etb.fileType.insert(new FileType("jpg", Some("jpeg"), "image", "image/jpg"))
       Etb.fileType.insert(new FileType("gif", None, "image", "image/gif"))
       Etb.fileType.insert(new FileType("pdf", None, "document", "application/pdf"))
      fileType.where(row => row.ext === "jpg").single
        from(fileType)(row => where(row.ext === "jpg") select(row))
    }
  println(jpg.getClass) //class org.squeryl.dsl.boilerplate.Query1
*/
}





