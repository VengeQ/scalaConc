version := "0.1"

scalaVersion := "2.12.7"

val scalaTest="org.scalatest" %% "scalatest" % "3.0.5"
val commons="commons-io" % "commons-io" % "2.4"

fork := true

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
  fork := true
)

lazy val concurrency = (project in file("."))
  .settings(
    name := "concurrency",
    libraryDependencies ++= Seq((scalaTest % Test),commons)
  ).aggregate(part1,part2).dependsOn(part1,part2)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at
    "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at
    "http://repo.typesafe.com/typesafe/releases/"
)