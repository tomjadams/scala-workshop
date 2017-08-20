import Dependencies._
import sbt.Keys.mainClass

lazy val root = (project in file(".")).
    settings(
      inThisBuild(List(
        organization := "com.redbubble",
        scalaVersion := "2.12.1",
        version := "0.1.0-SNAPSHOT"
      )),
      name := "Pricer",
      mainClass in Compile := Some("com.redbubble.pricer.http.HttpApp"),
      libraryDependencies ++= Seq(
        circeCore,
        circeGeneric,
        circeParser,
        finchCore,
        finchCirce,
        scalaTest,
        scalaCheck
      )
    )
