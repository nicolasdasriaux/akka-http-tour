import sbt.Keys.version

lazy val root = (project in file("."))
  .settings(
    organization := "com.axa.tutorial",
    name := "akka-http-tour",
    version := "0.1",
    scalaVersion := "2.12.7",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.5",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
      "com.typesafe.akka" %% "akka-stream" % "2.5.17",
      "com.typesafe.akka" %% "akka-actor" % "2.5.17"
    ),

    reStart / mainClass := Some("akkahttptour.AkkaHttpTourApp")
  )
