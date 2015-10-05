lazy val commonSettings = Seq(
  organization := "com.emc.gs.eat",
  version := "0.0.1",
  scalaVersion := "2.10.4"
)
lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "eat",
    libraryDependencies += scalaTest,
    assemblyJarName in assembly := "eat.jar",
    mainClass in assembly := Some("com.emc.gs.eat.Main")
  )
val scalaTest = "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
