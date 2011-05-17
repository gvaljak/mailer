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

class EtbMailer(host: String, username: String, password: String) {

  configureMail()

  def configureMail() {

    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable","true");
    //Set the host name
    System.setProperty("mail.smtp.port", "25") // Enable authentication
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty
    Mailer.authenticator = Full(new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(username, password)
    })
  }


  def send(from: From, subject: Subject, rest: MailTypes*) {

    val db = new DbEtbPostgres(
        "sk.cehtunger-debian.debian-local.net",
        "etb_00",
        "etb",
        "etbxhyper")

    db.insertMail(from, subject, rest: _*)

    sendMail(from, subject, rest: _*)

    println("DINAMOOOOOO")
  }

}

