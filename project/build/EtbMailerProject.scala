import sbt._
import de.element34.sbteclipsify.Eclipsify

class EtbMailerProject(info: ProjectInfo) extends DefaultProject(info)
                                          with Eclipsify {

  override def managedStyle = ManagedStyle.Maven

  val credentials = new java.io.File(Path.userHome / ".ivy2" / "ivy-element.ppk" toString)
  lazy val publishTo = Resolver.sftp("Element d.o.o. Maven2 Repository", "element.hr", "/ivy") as ("ivy", credentials, "sbtivym2")

  override def packageSrcJar = defaultJarPath("-sources.jar")
  val sourceArtifact = Artifact.sources(artifactID)

  override def packageDocsJar = defaultJarPath("-javadoc.jar")
  val docsArtifact = Artifact.javadoc(artifactID)

  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)
  override def pomIncludeRepository(repo: MavenRepository) = false

  override def pomExtra =
    <repositories>
      <repository>
        <id>ElementMaven2Repository</id>
        <name>Element d.o.o. Maven2 Repository</name>
        <url>http://element.hr/m2</url>
      </repository>
    </repositories>


  lazy val snapshots = ScalaToolsSnapshots

  val liftVersion = "2.4-SNAPSHOT"
  val liftUtil = "net.liftweb" %% "lift-util" % liftVersion % "compile"

  val liftSqueryl = "org.squeryl" %% "squeryl" % "0.9.4"
  val pgJdbc4 = "postgresql" % "postgresql" % "9.0-801.jdbc4"

  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()

//  --------------------------------------------------------------------------

  val commonsIo = "commons-io" % "commons-io" % "2.0.1" % "test"
  val scalaTest = "org.scalatest" %% "scalatest" % "1.6-SNAPSHOT"

/*
//  val configgy = "hr.element.configgy" % "configgy" % "2.0.0"

  val elementRepo = "Element d.o.o. Ivy Repository" at "http://element.hr/ivy"
  val etb = "hr.element.etb" %% "etb" % "0.1.18"

  val retcol = "hr.element.etb.retcol" %% "retcol" % "0.0.16"
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"
*/
}
