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
import org.eiennohito.kotonoha.actors.ReleaseAkkaMain
import org.eiennohito.kotonoha.web.rest.{Learning, SimpleRest}
import org.eiennohito.kotonoha.mongodb.MongoDbInit


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
//    if (!DB.jndiJdbcConnAvailable_?) {
//      val vendor =
//	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
//			     Props.get("db.url") openOr
//			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
//			     Props.get("db.user"), Props.get("db.password"))
//
//      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
//
//      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
//    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    //Schemifier.schemify(true, Schemifier.infoF _, User)

    MongoDbInit.init()

    // where to search snippet
    LiftRules.addToPackages("org.eiennohito.kotonoha.web.snippets")

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Home") / "index"
    )

      // more complex because this menu allows anything in the
      // /static path to be visible
      //Menu(Loc("Static", Link(List("static"), true, "/static/index"),
//	       "Static Content")))

    //def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    LiftRules.statelessDispatchTable.append(Learning)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    //S.addAround(DB.buildLoanWrapper)

    LiftRules.unloadHooks.append({ () => ReleaseAkkaMain.shutdown() })
  }
}
