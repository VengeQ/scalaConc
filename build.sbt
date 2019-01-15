version := "0.1"

scalaVersion := "2.12.8"


val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"
val commons="commons-io" % "commons-io" % "2.4"
val scalaLogging="com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val logbackClassic="ch.qos.logback" % "logback-classic" % "1.2.3"
val async="org.scala-lang.modules" %% "scala-async" % "0.9.7"
val rxScala="io.reactivex" % "rxscala_2.12" % "0.26.5"
val scalaSwing="org.scala-lang.modules" %% "scala-swing" % "2.1.0"
val akkaActor= "com.typesafe.akka" %% "akka-actor" % "2.5.19"
val akkaTest= "com.typesafe.akka" %% "akka-testkit" % "2.5.19" % Test
val akkaStream= "com.typesafe.akka" %% "akka-stream" % "2.5.19"
val akkaStreamTest= "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % Test
val akkaHttp="com.typesafe.akka" %% "akka-http" % "10.1.6"
val akkaHttpTest= "com.typesafe.akka" %% "akka-http-testkit" % "10.1.6" % Test


fork := false

connectInput in run := true

lazy val part1=Project(
  id = "part1",
  base = file("part1")
).settings(
    version := "0.1.1",
    scalaVersion := "2.12.8",
    name := "part1"
  )

lazy val part2=Project(
  id = "part2",
  base = file("part2")
).settings(
  version := "0.1.2",
  scalaVersion := "2.12.8",
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
  scalaVersion := "2.12.8",
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
  scalaVersion := "2.12.8",
  name := "part4",
  fork := true,
  connectInput in run := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons, async),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch4.Part4")

)

lazy val part5=Project(
  id = "part5",
  base = file("part5")
).settings(
  version := "0.1.5",
  scalaVersion := "2.12.8",
  name := "part5",
  fork := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test)),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch5.Part5")
)

lazy val part6=Project(
  id = "part6",
  base = file("part6")
).settings(
  version := "0.1.6",
  scalaVersion := "2.12.8",
  name := "part6",
  fork := true,
  connectInput in run := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons,rxScala, scalaSwing),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch6.Part6")

)

lazy val part8=Project(
  id = "part8",
  base = file("part8")
).settings(
  version := "0.1.8",
  scalaVersion := "2.12.8",
  name := "part8",
  fork := true,
  connectInput in run := true,
  libraryDependencies ++= Seq(scalaLogging, logbackClassic,(scalaTest % Test),commons, akkaActor, akkaTest, akkaStream, akkaHttp),
  mainClass in assembly := Some("com.dvbaluki.concurrency.ch8.Part8")
)

lazy val concurrency = (project in file("."))
  .settings(
    name := "concurrency",
    libraryDependencies ++= Seq((scalaTest % Test),commons, scalaLogging, logbackClassic,rxScala, scalaSwing)
  ).aggregate(part1,part2,part3,part4,part5,part6,part8).dependsOn(part1,part2,part3,part4,part5,part6,part8)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at
    "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at
    "http://repo.typesafe.com/typesafe/releases/"
)