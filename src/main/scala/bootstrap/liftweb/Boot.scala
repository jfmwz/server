package bootstrap.liftweb

/**
 * @author eiennohito
 * @since 07.10.11 
 */

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import ws.kotonoha.server.actors.{InitUsers, ReleaseAkkaMain}
import ws.kotonoha.server.mongodb.MongoDbInit
import ws.kotonoha.server.records.UserRecord
import ws.kotonoha.server.web.rest._
import admin.{Stats, OFHistory}
import ws.kotonoha.server.web.rest.model.{Cards, Words}
import ws.kotonoha.server.actors.lift.Ping
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import ws.kotonoha.server.web.snippet.{ModeSnippet, ClasspathResource}
import com.typesafe.scalalogging.slf4j.Logging
import scala.xml.{NodeSeq, Text}
import ws.kotonoha.server.web.loc.WikiLoc
import net.liftweb.actor.{ILAExecute, LAScheduler}


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Logging {
  def boot {
    MongoDbInit.init()

    /*val c = Pointer.allocateByte()
    c.set(0.toByte)
    val m = MecabLibrary.mecab_new2(c)
    MecabLibrary.mecab_destroy(m)*/

    //MecabInit.init()
    //LiftRules.unloadHooks.append(() => MecabInit.unload())

    //Bootstrap lazy akka
    ReleaseAkkaMain.global ! Ping
    ReleaseAkkaMain.global ! InitUsers

    val ec = ReleaseAkkaMain.context.prepare()

    val liftex = new ILAExecute {
      def shutdown() {}

      def execute(f: () => Unit) {
        ec.execute(new Runnable {
          def run() = { f() }
        })
      }
    }

    Schedule.threadPoolSize = 1
    Schedule.maxThreadPoolSize = 16
    LAScheduler.createExecutor = () => liftex


    // where to search snippet
    LiftRules.addToPackages("ws.kotonoha.server.web")

    val loggedin = If(UserRecord.loggedIn_? _, "You are not logged in")
    // Build SiteMap
    val admin = Menu.i("Admin") / "admin" / "index" >> If(() => UserRecord.isAdmin, "Only administrators allowed here") submenus(
      Menu.i("Client List") / "admin" / "clients",
      Menu.i("Access list") / "admin" / "access",
      Menu.i("Configuration") / "admin" / "config",
      Menu.i("Debug") / "admin" / "debug",
      Menu.i("Global learning") / "admin" / "learning",
      Menu.i("OF history") / "admin" / "ofhistory"
      )

    val emptyLink: LinkText[Unit] = new LinkText({ case _ => Nil })
    val loc = Loc("static", Link(List("static"), true, "/static"), emptyLink , Hidden)
    val static = Menu.apply(loc)

    def sitemap = {
      SiteMap(
        Menu.i("Home") / "index" submenus(
          Menu.i("Client Authorizations") / "user" / "tokens" >> loggedin >> UserRecord.AddUserMenusAfter
          ),
        Menu.i("Learning") / "learning" / "index" >> loggedin submenus(
          Menu.i("Repetition") / "learning" / "repeat",
          Menu.i("OF Matrix") / "learning" / "ofmatrix",
          Menu.i("Words for review") / "learning" / "bad_cards",
          Menu.i("Tags") / "learning" / "tags",
          Menu.i("History") / "learning" / "history"
          ),
        admin,
        Menu.i("Words") / "words" / "index" >> loggedin submenus(
          Menu.i("Add") / "words" / "add",
          Menu.i("Approve & Review") / "words" / "approve_added",
          Menu.i("Detail") / "words" / "detail" >> Hidden
          ),
        Menu.i("Wiki") / "wiki",
        Menu.i("Tools") / "tools" / "index" submenus(
          Menu.i("Test parser") / "tools" / "parser",
          Menu.i("JMDict") / "tools" / "jmdict",
          Menu.i("Warodai") / "tools" / "warodai",
          Menu.i("Examples") / "tools" / "examples",
          Menu.i("Stroke orders") / "tools" / "kakijyun",
          Menu.i("Kanji") / "tools" / "kanji" >> If(() => Props.devMode, "Not debug"),
          Menu.i("Last wiki edits") / "tools" / "wikiedits",
          Menu.i("Sandbox") / "tools" / "sandbox" >> If(() => Props.devMode, "Inaccessible")
      ),
        Menu.i("Oauth") / "oauth" >> Hidden submenus (
          Menu.i("OAuth request") / "oauth" / "request"
      ),
        wikiPages,
        static
      )
    }

    def wikiPages = Menu(new WikiLoc)

    // more complex because this menu allows anything in the
    // /static path to be visible
    //Menu(Loc("Static", Link(List("static"), true, "/static/index"),
    //	       "Static Content")))

    def sitemapMutators = UserRecord.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    LiftRules.dispatch.append(Learning)
    LiftRules.dispatch.append(QrRest)
    LiftRules.dispatch.append(new StatusApi)

    LiftRules.dispatch.append(Words)
    LiftRules.dispatch.append(Stats)
    LiftRules.dispatch.append(Grants)
    LiftRules.dispatch.append(Cards)
    LiftRules.dispatch.append(Juman)

    LiftRules.dispatch.append(OFHistory)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQueryArtifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => UserRecord.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      if (r.path(0) != "static") {
        new Html5Properties(r.userAgent)
      } else {
        new Html5StaticProperties(r.userAgent)
      }
    )

    LiftRules.statelessReqTest.append({
      case StatelessReqTest("static" :: _, _) => true
    })

    // Make a transaction span the whole HTTP request
    //S.addAround(DB.buildLoanWrapper)

    LiftRules.unloadHooks.append({
      () => ReleaseAkkaMain.shutdown()
    })

    LiftRules.snippetDispatch.append(
      Map(
        "cpres" -> ClasspathResource,
        "mode" -> ModeSnippet
      )
    )
  }
}
