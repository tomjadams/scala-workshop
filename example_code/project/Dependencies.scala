import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test

  val circeVersion = "0.8.0"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  val finchVersion = "0.15.1"
  lazy val finchCore = "com.github.finagle" %% "finch-core" % finchVersion
  lazy val finchCirce = "com.github.finagle" %% "finch-circe" % finchVersion
}
