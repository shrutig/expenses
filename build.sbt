import sbt._
import sbt.Keys._


name := "expenses-manager"

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += jdbc

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"

libraryDependencies += "com.typesafe.play" % "anorm_2.10" % "2.4.0"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.2.2"

libraryDependencies += evolutions

libraryDependencies += "org.webjars" % "webjars-play_2.10" % "2.4.0-1"

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.5"

libraryDependencies += "org.webjars" % "jquery" % "2.1.4"

