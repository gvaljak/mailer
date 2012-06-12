import sbt._
import Keys._

object BuildSettings {
  import Default._

  val bsEtbMailer = scalaSettings ++ Seq(
    name    := "etb-mailer",
    version := "0.3.2"
  )
}

object Publications {
  val etbMailer       = "hr.element.etb"  %  "mailer"
}

object Dependencies {
  import Publications._

  val liftVersion = "2.4"

  val liftUtil = "net.liftweb" %% "lift-util" % liftVersion % "compile"

  val squeryl = "org.squeryl" %% "squeryl" % "0.9.4"
  val pgJdbc4 = "postgresql" % "postgresql" % "9.0-801.jdbc4"

  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()

  val iorc = "hr.element.etb" %% "iorc" % "0.0.21"

  val scalaIo = "com.github.scala-incubator.io" %% "scala-io-file" % "0.3.0"
  val commonsIo = "commons-io" % "commons-io" % "2.0.1" % "test"
  val scalaTest = "org.scalatest" %% "scalatest" % "1.7.2" % "test"

  val jodaTime = Seq(
    "org.joda" % "joda-convert" % "1.2",
    "joda-time" % "joda-time" % "2.0"
  )
}

import Implicits._

object ProjectDeps {
  import Dependencies._
  import Publications._

  val depsEtbMailer = libDeps(
    liftUtil

  , scalaIo
  , squeryl
  , pgJdbc4

  , configgy

  , iorc
  , scalaTest
  )

}

//  ---------------------------------------------------------------------------

object EtbMailerBuild extends Build {
  import BuildSettings._
  import ProjectDeps._

  lazy val etbMailer = Project(
    "etb-mailer",
    file("mailer"),
    settings = bsEtbMailer :+ depsEtbMailer
  )

}

//  ---------------------------------------------------------------------------

object Repositories {
  val ElementNexus     = "Element Nexus"     at "http://maven.element.hr/nexus/content/groups/public/"
  val ElementReleases  = "Element Releases"  at "http://maven.element.hr/nexus/content/repositories/releases/"
  val ElementSnapshots = "Element Snapshots" at "http://maven.element.hr/nexus/content/repositories/snapshots/"
}

//  ---------------------------------------------------------------------------

object Resolvers {
  import Repositories._

  val settings = Seq(
    resolvers := Seq(ElementNexus, ElementReleases, ElementSnapshots),
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = false, scalaTools = false)
    }
  )
}

//  ---------------------------------------------------------------------------

object Publishing {
  import Repositories._

  val settings = Seq(
    publishTo <<= (version) { version => Some(
      if (version.endsWith("SNAPSHOT")) ElementSnapshots else ElementReleases
    )},
    credentials += Credentials(Path.userHome / ".publish" / "element.credentials"),
    publishArtifact in (Compile, packageDoc) := false
  )
}

//  ---------------------------------------------------------------------------

object Default {
  val scalaSettings =
    Defaults.defaultSettings ++
    Resolvers.settings ++
    Publishing.settings ++ Seq(
      organization := "hr.element.etb",
      crossScalaVersions := Seq("2.9.1", "2.9.0-1", "2.9.0"),
      scalaVersion <<= (crossScalaVersions) { versions => versions.head },
      scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "UTF-8", "-optimise"), // , "-Yrepl-sync"
      unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)( _ :: Nil),
      unmanagedSourceDirectories in Test    <<= (scalaSource in Test   )( _ :: Nil)
    )
}


object Implicits {
  implicit def depToFunSeq(m: ModuleID) = Seq((_: String) => m)
  implicit def depFunToSeq(fm: String => ModuleID) = Seq(fm)
  implicit def depSeqToFun(mA: Seq[ModuleID]) = mA.map(m => ((_: String) => m))

  def libDeps(deps: (Seq[String => ModuleID])*) = {
    libraryDependencies <++= scalaVersion( sV =>
      for (depSeq <- deps; dep <- depSeq) yield dep(sV)
    )
  }
}





