lazy val commonSettings = Seq(
  organization := "com.emc.gs.eat",
  version := "0.0.1",
  // Stuck at scala version 2.10.* here because scala assembly, which I think is needed, only supports this version.
  scalaVersion := "2.10.4"
)
lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "eat",
    libraryDependencies += scalaTest,
    libraryDependencies += scalaCsv,
    libraryDependencies += akkaActor,
    libraryDependencies += akkaTestKit,
    libraryDependencies += scopt,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2",
    libraryDependencies += "com.decodified" %% "scala-ssh" % "0.7.0",

    assemblyJarName in assembly := "eat.jar",
    mainClass in assembly := Some("com.emc.gs.eat.Main")
  )
val akkaVersion = "2.3.14"
val scalaTestVersion = "2.2.4"
val scalaCsvVersion = "1.2.2"
val scoptVersion = "3.3.0"
val schoogrVersion = "0.9.4"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
val scalaCsv = "com.github.tototoshi" %% "scala-csv" % scalaCsvVersion
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
val scopt = "com.github.scopt" %% "scopt" % scoptVersion