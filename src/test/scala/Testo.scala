import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers

import java.io.File
import org.apache.commons.io.FileUtils

import scala.xml._

import hr.element.etb.mailer._
import EtbMailer._

import net.liftweb.util.Mailer._

class MailTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  lazy val prettyPrinter = new scala.xml.PrettyPrinter(80,2)

  def formatXML(in: Elem) =
    XML.loadString(prettyPrinter.format(in))

  feature("Sending mail with file attachments") {

    info("Two files are read, and send as attachments in email")

    scenario("Two file attachments are being sent") {

      given("two file attachments, text and html body")

      val filobajts = FileUtils.readFileToByteArray(new File("r:\\mail-test.pdf"))
      val slikobajts = FileUtils.readFileToByteArray(new File("r:\\mail-test.png"))

      val pdfo = AttachmentFile("testo.pdf", "application/pdf", filobajts)
      val sliko = AttachmentFile("testo.png", "image/png", slikobajts)

      val text = " &&&& <&>>><<<><a></a>ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k"

      val xml =
        <span>
          <a href="http://dreampostcards.com">_Drimpostkarde</a>
          <img src="testo.png" alt="slikoo"/>
          <b>ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k</b>
        </span>

      val html = formatXML(xml)

      when("ETBMailer is initialised")

      val etbMailer = new EtbMailer("src/test/resources/mailer.conf")

      def insendMail() =
        etbMailer.queueMail(
            From("gordan@element.hr"),
            Subject("dobar dan"),
            PlainPlusBodyType(text, "utf8"),
            None, // Some(HtmlBody(xml)),
            Seq(
              To("cehtunger@gmail.com")
//              CC("gordan@dreampostcards.com")
//              BCC("gordan.valjak@zg.t-com.hr")
            ),
            Some(Seq(sliko))
          )

      and("mail is sent")
      val inserto = //Right("asdfsfad")
//        insendMail()
//        etbMailer.sendFromDb(25)
        etbMailer.sendToAddress(210)


      then("""Function must return Right""")
      inserto.isRight must be === true

      val id =
        inserto.right.get

      info("""New mail id is: """ +id)

    }
  }
}
