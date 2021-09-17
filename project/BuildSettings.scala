import sbt.Keys.libraryDependencies
import sbt.{ plugins, AutoPlugin, Plugins }

object BuildSettings extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  override def projectSettings =
    Seq(libraryDependencies ++= Dependencies.jars ++ Dependencies.testJars)
}
