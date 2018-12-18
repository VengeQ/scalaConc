version := "0.1"

scalaVersion := "2.12.7"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"
val commons="commons-io" % "commons-io" % "2.4"
val scalaLogging="com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val logbackClassic="ch.qos.logback" % "logback-classic" % "1.2.3"

fork := false

connectInput in run := true

lazy val part1=Project(
  id = "part1",
  base = file("part1")
).settings(
    version := "0.1.1",
    scalaVersion := "2.12.7",
    name := "part1"
  )

lazy val part2=Project(
  id = "part2",
  base = file("part2")
).settings(
  version := "0.1.2",
  scalaVersion := "2.12.7",
  name := "part2",
  fork := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test)),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch2.Part2")
)

lazy val part3=Project(
  id = "part3",
  base = file("part3")
).settings(
  version := "0.1.3",
  scalaVersion := "2.12.7",
  name := "part3",
  fork := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch3.Part3")
)

lazy val part4=Project(
  id = "part4",
  base = file("part4")
).settings(
  version := "0.1.4",
  scalaVersion := "2.12.7",
  name := "part4",
  fork := true,
  connectInput in run := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch4.Part4")

)


lazy val concurrency = (project in file("."))
  .settings(
    name := "concurrency",
    libraryDependencies ++= Seq((scalaTest % Test),commons, scalaLogging, logbackClassic)
  ).aggregate(part1,part2,part3,part4).dependsOn(part1,part2,part3,part4)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at
    "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at
    "http://repo.typesafe.com/typesafe/releases/"
)