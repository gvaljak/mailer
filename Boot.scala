package bootstrap.liftweb

import net.liftweb.util.Helpers._
import net.liftweb.sitemap.Loc._
import code.model._
import net.liftweb.common._
import net.liftweb.http._
import bootstrap.liftweb._
import net.liftweb.sitemap._
import net.liftweb.util._

import code.model.sql._

import _root_.code.lib._

// import mapper._
// import code.model._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

import java.io.File
import org.apache.commons.io.FileUtils

import javax.mail._
import javax.mail.internet._

object Boot {

  def configureMail(host: String, user: String, password: String) {

    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable","true");
    //Set the host name
    System.setProperty("mail.smtp.port", "25") // Enable authentication
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty
    Mailer.authenticator = Full(new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(user, password)
    })
  }

  def main(args: Array[String]) {
    println("mainoooo")

    configureMail("kaligula.element.hr", "gordan", "gvimbddm")



    import net.liftweb.util.Mailer
    import net.liftweb.util.Mailer._

    val fileo = FileUtils.readFileToByteArray(new File("r:\\mail-test.pdf"))
    val sliko = FileUtils.readFileToByteArray(new File("r:\\mail-test.png"))

    val attachment = PlusImageHolder("testo.pdf", "application/pdf", fileo)
    val inlineImg = PlusImageHolder("testo.png", "image/png", sliko)

    val text = "Smeće radi jedino s dizejblanim starttls-om akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k"
    val html =
      <span>
        <a href="http://dreampostcards.com">_Drimpostkarde</a>
        <img src="testo.png" alt="slikoo"/>
        <b>akuk  ukukuuukk  kuikzmkizmizik kizikzk ikzikz kizkiz ikzkizki zkk ikz kzikzk zk zki zk zkkiz ikz kuzkuz ukzukzk kzk uzuk zuk  ukz kuz kuzukzk zuk zk zkz kzuk zkzku zkuzkuzkuz ku zk uz ku zku z ukz k</b>
      </span>

    sendMail(
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

class Boot {

  def boot {

    Db.ensurePersistentConnection

    // where to search snippet
    LiftRules.addToPackages("code")

    val menuEntries = List(
      Menu.i("Početna stranica") / "index",
      Menu.i("Info stranice") / "billboards" >> Hidden
    ) ::: {
      cms.page.selectAll().values.
        filter(Page.isSidebarLink).
        map(p => Menu.i(p.link_text) / p.link_href)
    }.toList

    // Build SiteMap
    def sitemap = SiteMap(menuEntries: _*)

/*
    def sitemapMutators = User.sitemapMutator
    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
*/

    def hilighter(sM: SiteMap): SiteMap = {
      sM
    }

    LiftRules.setSiteMapFunc(() => hilighter(sitemap))
    LiftRules.setSiteMap(sitemap)

    LiftRules.statelessDispatchTable.append {
      case Req("favicon" :: Nil, "ico", _) =>
        () => Full(CachedFavicon.getResponse)
    }

/*
    LiftRules.liftRequest.append{
      case Req("classpath" :: _, _, _) => true
      case Req("ajax_request" :: _, _, _) => true
      case Req("favicon" :: Nil, "ico", GetRequest) => false
      case Req(_, "css", GetRequest) => false
      case Req(_, "js", GetRequest) => false
    }
*/

    LiftRules.statelessRewrite.prepend(NamedPF("BillboardRewrite") {
      case b @ RewriteRequest(ParsePath(link :: Nil, _, _, _), _, _)
        if cms.page.select('link_href, link).isDefined =>
          RewriteResponse("billboards" :: Nil, Map("link" -> link))
    })

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
/*
    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
*/
    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

/*
    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
*/
  }
}
