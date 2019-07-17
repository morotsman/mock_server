name := """mock-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" % "angularjs" % "1.5.8",
  "org.webjars" % "requirejs" % "2.1.11-1", 
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "jquery" % "2.2.4",
  "org.webjars" % "flot" % "0.8.3",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.3"
)

