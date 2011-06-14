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
      val attobajts = FileUtils.readFileToByteArray(new File("c:\\Users\\Administrator\\Documents\\Google Talk Received Files\\Tournament 3298 06-09-11.lin"))

      val pdfo = AttachmentFile("testo.pdf", "application/pdf", filobajts)
      val sliko = AttachmentFile("testo.png", "image/png", slikobajts)
      val lino = AttachmentFile("mec.lin", "document/lin", attobajts)

//      val text = " &&&& <&>>><<<><a></a>ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k"
      val text = "Mec"

//      val xml =
//        <span>
//          <a href="http://dreampostcards.com">_Drimpostkarde</a>
//          <img src="testo.png" alt="slikoo"/>
//          <b>ŠĐČĆŽšđčćž akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k</b>
//        </span>
      val xml =
        <font color="#6a6a6a" face="sans-serif" size="2">
          <font color="#274A70" size="3"><b>Poštovani,</b></font>
          <img src="testo.png" alt="slikoalt"/>
          <p>
            Rok dospijeća za vaš zajam je <b>JEDNOM</b> (ASD).
          </p>
          <p>
            Ovaj e-mail služi kao podsjetnik uplatite na vrijeme kako biste izbjegli troškove opomene.
            Ako ste dobili ovaj e-mail zabunom, molimo zanemarite ga.
            Popis banaka u koje možete uplatiti možete naći
            <a href="https://instantkredit.hr/info" title="Podaci za uplatu">na našim stranicama.</a>
            <ul>
              <li>Poziv na broj: <b>123123</b></li>
              <li>Iznos za uplatiti: <b>400 kuna</b></li>
            </ul>
            Za sva dodatna pitanja vezana uz vaš zajam možete nam se obratiti na
            e-mail adresu <a href="mailto:info@instantkredit.hr" title="Kontaktirajte nas">info@instantkredit.hr</a>
            ili telefon <b>123123</b>.
          </p>
          <p>
            Hvala,
            <i>instantkredit.hr</i>
          </p>
        </font>

      val html = formatXML(xml)

      when("ETBMailer is initialised")

      val etbMailer = new EtbMailer("src/test/resources/mailer.conf")

      def insendMail() =
        etbMailer.queueMail(
            From("gordan@element.hr"),
            Subject("One address"),
            PlainPlusBodyType(text, "utf8"),
            Some(XHTMLMailBodyType(xml)),
            Seq(
//              CC("gordan@dreampostcards.com"),
//              BCC("gordan.valjak@zg.t-com.hr"),
              To("cehtunger@gmail.com")
            ),
            Some(Seq(sliko))
          )

      and("mail is sent")
      val inserto = //Right("asdfsfad")
        insendMail()
//        etbMailer.sendMailById(103)
//        etbMailer.sendToAddress(258)

      inserto match {
        case Right(id) =>
          info("""New mail id is: """ +id)
        case Left(e) =>
          info("Exception: "+e.getMessage)
      }



      then("""Function must return Right""")
      inserto.isRight must be === true


    }
  }
}
