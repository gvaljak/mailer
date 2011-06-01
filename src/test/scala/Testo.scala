import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen

import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._

import java.io.File
import org.apache.commons.io.FileUtils

import scala.xml._

import hr.element.etb.mailer.EtbMailer

class MailTest extends FeatureSpec with GivenWhenThen {

  lazy val prettyPrinter = new scala.xml.PrettyPrinter(80,2)

  def formatXML(in: Elem) =
    XML.loadString(prettyPrinter.format(in))

  feature("Sending mail with file attachments") {

    info("Two files are read, and send as attachments in email")

    scenario("Two file attachments are being sent") {

      given("two file attachments, text and html body")

      val fileo = FileUtils.readFileToByteArray(new File("r:\\mail-test.pdf"))
      val sliko = FileUtils.readFileToByteArray(new File("r:\\mail-test.png"))

      val attachment = PlusImageHolder("testo.pdf", "application/pdf", fileo)
      val inlineImg = PlusImageHolder("testo.png", "image/png", sliko)

      val text = "ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k"

      val xml =
        <span>
          <a href="http://dreampostcards.com">_Drimpostkarde</a>
          <img src="testo.png" alt="slikoo"/>
          <b>ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k</b>
        </span>

      val html = formatXML(xml)

      when("ETBMailer is initialised")
      val etbMailer = new EtbMailer()

      then("Email is sent with given parameters")
      etbMailer.send(
          From("gordan@element.hr"),
          Subject("dobar dan"),
          To("cehtunger@gmail.com"),
          PlainMailBodyType(text),
          CC("gordan@dreampostcards.com"),
          BCC("gordan.valjak@zg.t-com.hr"),
          XHTMLMailBodyType(html),
          XHTMLPlusImages(html, inlineImg, attachment)
        )
    }
  }
}

//import hr.element.etb.mailer.EtbMailer
//
//object Testo {
//
//
//  def main(args: Array[String]) {
//
//    println("mainoooo")
//
//
//    val fileo = FileUtils.readFileToByteArray(new File("r:\\mail-test.pdf"))
//    val sliko = FileUtils.readFileToByteArray(new File("r:\\mail-test.png"))
//
//
//    val etbMailer = new EtbMailer()
//
//
//    Thread.sleep(10000)
//  }
//
//}