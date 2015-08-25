
name := "expenses-manager"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(jdbc,
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.play" % "anorm_2.10" % "2.4.0",
  "com.github.tototoshi" %% "scala-csv" % "1.2.2",
  evolutions,
  "org.webjars" % "webjars-play_2.10" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "jquery" % "2.1.4")

lazy val root = (project in file(".")).enablePlugins(PlayScala)