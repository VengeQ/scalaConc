version := "0.1"

scalaVersion := "2.12.7"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"

lazy val hello = (project in file("."))
  .settings(
    name := "concurrency",
    libraryDependencies ++= Seq((scalaTest % Test))
  )