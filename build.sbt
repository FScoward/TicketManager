name := "TicketManager"

version := "1.0-SNAPSHOT"

resolvers += "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.webjars" %% "webjars-play" % "2.2.1-2",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.typesafe.slick" %% "slick" % "2.0.0",
  "org.twitter4j" % "twitter4j-core" % "4.0.1"
)     

play.Project.playScalaSettings
