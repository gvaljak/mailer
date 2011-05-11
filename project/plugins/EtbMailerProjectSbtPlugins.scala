import sbt._

class EtbMailerProjectSbtPlugins(info: ProjectInfo) extends PluginDefinition(info) {

  val eclipse = "de.element34" % "sbt-eclipsify" % "0.7.0"
}
