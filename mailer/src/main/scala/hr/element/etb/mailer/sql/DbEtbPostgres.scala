package hr.element.etb
package mailer
package sql

import org.squeryl.{ Session, Schema, SessionFactory }
import org.squeryl.adapters.PostgreSqlAdapter
//import org.squeryl.PrimitiveTypeMode._

class DbEtbPostgres(
  host : String,
  dbName : String,
  username : String,
  password : String) extends DbEtb {

  Class.forName("org.postgresql.Driver")

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
  SessionFactory.concreteFactory = Some(() =>
    Session.create(
      java.sql.DriverManager.getConnection(
        "jdbc:postgresql://"+DbHost+"/"+DbName, DbUsername, DbPassword),
      new PostgreSqlAdapter))

  /**
   * ******************
   *     EXAMPLES
   * ******************
   */
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





