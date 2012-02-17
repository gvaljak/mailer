package hr.element.etb.mailer

import java.io.{File}
import scala.xml._
import hr.element.etb.mailer._
import EtbMailer._
import net.liftweb.util.Mailer._
//import java.io.BufferedWriter
//import java.io.FileWriter

object StartMailer {

  def main(args: Array[String]) = {

    lazy val prettyPrinter = new scala.xml.PrettyPrinter(80,2)

    def formatXML(in: Elem) =
      XML.loadString(prettyPrinter.format(in))

    val mailFromFile =
      scala.io.Source.fromFile("""src/main/resources/fakulteti-dopis.html""")(io.Codec.UTF8).mkString

    val addr = io.Source.fromFile("""src/main/resources/adrese_fix.txt""")(io.Codec.UTF8).getLines()

//    val text = FileUtils.readFileToString(new File("""src/main/resources/tekst.txt"""), "utf8")
    val attFile = io.Source.fromFile("""src/main/resources/924-Ornik-letak.jpg""")(scala.io.Codec.ISO8859).map(_.toByte).toArray


    val att = AttachmentFile("924-Ornik-letak.jpg", "image/jpg", attFile)

    val etbMailer = new EtbMailer("src/main/resources/mailer.conf")

  //  val AddrRegex = """((?:[^\s]+\s+)+)([^\s]+)""".r

    addr.foreach{t =>

//      val AddrRegex(nameRaw, addressRaw) = t.trim

//      val name = nameRaw.replaceAll( """[\s\xA0]+""", " " ).trim
      val address = t//addressRaw.replaceAll( """[\s\xA0]+""", " " ).trim

      try {


//        println("name: " + name)
        println("address: " + address)

//        val mailWithName = mailFromFile.replaceAll("""\[NAME\]""", name)
        val xml = XML.loadString(mailFromFile)

        //val html = formatXML(xml)

        def insendMail() =
          etbMailer.queueMail(
              From("element@element.hr"),
              Subject("Knjižničarima i studentima"),
              PlainPlusBodyType(mailFromFile, "utf8"),
              Some(XHTMLMailBodyType(xml)),
              Seq(
                  To(address)
  //              CC("gordan@dreampostcards.com"),
  //              BCC("gordan.valjak@zg.t-com.hr"),
  //              BCC("gordan@dreampostcards.com"),
  //              BCC("cehtunger@gmail.com")
              ),
              Some(Seq(att))
            )

        val inserto = //Right("asdfsfad")
          insendMail()

        inserto match {
          case Right(id) =>
            println("""New mail id is: """ +id)
          case Left(e) =>
            println("Exception: "+e.getMessage)
            throw e
        }
      }catch{
        case e: Throwable =>
          println("error: " + address)
          e.printStackTrace
//          val out = new BufferedWriter(new FileWriter("""errors\failedList.txt""",true))
//          out.write(address)
//          out.newLine()
//          out.close()
//          FileUtils.writeStringToFile(new File("""errors\""" +address+".txt"), e.getMessage + "\n" + e.getStackTraceString)
      }
    }
  }
}