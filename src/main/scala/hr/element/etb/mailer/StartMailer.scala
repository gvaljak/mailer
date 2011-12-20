package hr.element.etb.mailer

import java.io.{File, FileWriter, BufferedWriter}
import java.io.FileInputStream
import org.apache.commons.io.FileUtils

import scala.xml._

import hr.element.etb.mailer._
import EtbMailer._

import net.liftweb.util.Mailer._

import org.apache.poi.hssf.usermodel.{HSSFWorkbook}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
object StartMailer {

  def main(args: Array[String]) = {

    lazy val prettyPrinter = new scala.xml.PrettyPrinter(80,2)

    def formatXML(in: Elem) =
      XML.loadString(prettyPrinter.format(in))

//    val mailFromFile = FileUtils.readFileToString(new File("""r:\Code\etb\mailer\src\main\resources\letter.html"""))

//    val is = new FileInputStream(new File("z:\\mailovi\\test.xls"))
//    val fs = new POIFSFileSystem(is)
//    val wb = new XSSFWorkbook("z:\\mailovi\\test.xlsx")
//
//    val shit = wb.getSheetAt(0)
//
//    println(shit.getRow(shit.getFirstRowNum()))

//    val addrAll = FileUtils.readFileToString(new File("""r:\Code\etb\mailer\src\main\resources\adreseZaSlat.txt"""),"utf8")


    val addr = io.Source.fromFile(new File("""src/resources/adrese_fix.txt"""))(io.Codec.UTF8).getLines()

    val text = FileUtils.readFileToString(new File("""src/resources/tekst.txt"""), "utf8")
    val attFile = FileUtils.readFileToByteArray(new File("""src/resources/POZIVNICABG.pdf"""))

    val att = AttachmentFile("POZIVNICABG.pdf", "application/pdf", attFile)

    val etbMailer = new EtbMailer("src/resources/mailer.conf")

  //  val AddrRegex = """((?:[^\s]+\s+)+)([^\s]+)""".r

    addr.foreach{t =>

//      val AddrRegex(nameRaw, addressRaw) = t.trim

//      val name = nameRaw.replaceAll( """[\s\xA0]+""", " " ).trim
      val address = t//addressRaw.replaceAll( """[\s\xA0]+""", " " ).trim

      try {


//        println("name: " + name)
        println("address: " + address)

        //val mailWithName = mailFromFile.replaceAll("""\[NAME\]""", name)
        //val xml = XML.loadString(mailWithName)

        //val html = formatXML(xml)

        def insendMail() =
          etbMailer.queueMail(
              From("element@element.hr"),
              Subject("Pozivamo Vas na 56. medjunarodni beogradski sajam knjiga"),
              PlainPlusBodyType(text, "utf8"),
              None,
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
          val out = new BufferedWriter(new FileWriter("""errors\failedList.txt""",true))
          out.write(address)
          out.newLine()
          out.close()
          FileUtils.writeStringToFile(new File("""errors\""" +address+".txt"), e.getMessage + "\n" + e.getStackTraceString)
      }
    }
  }
}