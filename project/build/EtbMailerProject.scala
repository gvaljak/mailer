import sbt._
import de.element34.sbteclipsify.Eclipsify

class EtbMailerProject(info: ProjectInfo) extends DefaultWebProject(info)
                                     with Eclipsify {
  val liftVersion = "2.3"
  val liftUtil = "net.liftweb" %% "lift-util" % liftVersion % "compile"
  val liftSqueryl = "net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile"

  val commonsIo = "commons-io" % "commons-io" % "2.0.1"

  val elementRepo = "Element d.o.o. Ivy Repository" at "http://element.hr/ivy"
  val etb = "hr.element.etb" %% "etb" % "0.1.15"

  val pgscala = "hr.element.pgscala" %% "pgscala" % "0.5.3"

}
