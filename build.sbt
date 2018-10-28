version := "0.1"

scalaVersion := "2.12.7"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"

lazy val part1=Project(
  id = "part1",
  base = file("part1")
)
  .settings(
    name := "part1"
  )

lazy val concurrency = (project in file("."))
  .settings(
    name := "concurrency",
    libraryDependencies ++= Seq((scalaTest % Test))
  ).aggregate(part1).dependsOn(part1)

