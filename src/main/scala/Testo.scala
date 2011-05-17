import hr.element.etb.mailer.EtbMailer

import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._


import java.io.File
import org.apache.commons.io.FileUtils

import scala.xml._

object Testo {

  lazy val prettyPrinter = new scala.xml.PrettyPrinter(80,2)

  def formatXML(in: Elem) =
    XML.loadString(prettyPrinter.format(in))

  def main(args: Array[String]) {

    println("mainoooo")


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

    val etbMailer = new EtbMailer("kaligula.element.hr", "gordan", "gvimbddm")

    etbMailer.send(
        From("gordan@element.hr"),
        Subject("dobar dan"),
        To("cehtunger@gmail.com"),
        PlainMailBodyType(text),
//        CC("gordan@dreampostcards.com"),
//        BCC("gordan.valjak@zg.t-com.hr"),
        XHTMLMailBodyType(html),
        XHTMLPlusImages(html, inlineImg)
      )

    Thread.sleep(10000)
    Runtime.getRuntime().halt(0)
  }

}