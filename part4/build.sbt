version := "0.1"

scalaVersion := "2.12.7"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"
val commons="commons-io" % "commons-io" % "2.4"
val scalaLogging="com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val logbackClassic="ch.qos.logback" % "logback-classic" % "1.2.3"

fork := false

lazy val part4=Project(
  id = "part4",
  base = file("part4")
).settings(
  version := "0.1.4",
  scalaVersion := "2.12.7",
  name := "part4",
  fork := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch4.Part4")
)
