package hr.element.etb.mailer

//import net.liftweb.util.Helpers._
import net.liftweb.common._
//import net.liftweb.http._
//import net.liftweb.util._

//import java.io.File
//import org.apache.commons.io.FileUtils

import javax.mail._
//import javax.mail.internet._


import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._

import hr.element.etb.mailer.sql._

import net.lag.configgy.Configgy

class EtbMailer {

  lazy val config = getConfig
  lazy val db = getDb

  configureMail()

  def getConfig = {
    Configgy.configure("src/test/resources/mailer.conf")
    Configgy.config
  }

  def getDb = {
    val dbParams = config.getConfigMap("mailer.db").getOrElse(error("db block not specified"))

    val dbHost = dbParams.getString("host").getOrElse(error("value db.host not specified!"))
    val dbName = dbParams.getString("dbname").getOrElse(error("value db.name not specified!"))
    val dbUsername = dbParams.getString("user").getOrElse(error("value db.username not specified!"))
    val dbPassword = dbParams.getString("password").getOrElse(error("value db.password not specified!"))

//    println(dbHost, dbName, dbUsername, dbPassword)

    new DbEtbPostgres(dbHost, dbName, dbUsername, dbPassword)
  }

  def configureMail() {

    val authParams = config.getConfigMap("mailer.authentication").getOrElse(error("authentication block not specified"))

    val host = authParams.getString("host").getOrElse(error("value auth.host not specified"))
    val port = authParams.getString("port").getOrElse("25")
    val starttls = authParams.getBool("starttls").getOrElse(false).toString
    val username = authParams.getString("username").getOrElse(error("value db.password not specified!"))
    val password = authParams.getString("password").getOrElse(error("value db.password not specified!"))

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

  def send(from: From, subject: Subject, rest: MailTypes*) {
    db.insertMail(from, subject, rest: _*)
//    sendMail(from, subject, rest: _*)
  }

  def error(msg: String) =
    throw new Error(msg)

}

