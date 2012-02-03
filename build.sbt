import sbt._

seq(webSettings :_*)

organization := "org.eiennohito"

name := "kotonoha-server"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

moduleName := "plast_calc"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

libraryDependencies ++=
					Seq("org.scalaz" %% "scalaz-core" % "6.0.3",
						"com.github.jsuereth.scala-arm" %% "scala-arm" % "1.0"
					)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.2.0"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0"

libraryDependencies ++= {
  val liftVersion = "2.4-RC1" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mongodb-record" % liftVersion,
    "net.liftweb" %% "lift-json-scalaz" % liftVersion,
	  "javax.servlet" % "servlet-api" % "2.5" % "provided->default")
}

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaVer = "2.0-M3"
  Seq(
    "com.typesafe.akka" % "akka-actor" % akkaVer,
    "com.typesafe.akka" % "akka-testkit" % akkaVer % "test"
  )
}

libraryDependencies +=  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container"

libraryDependencies += "com.foursquare" %% "rogue" % "1.1.1" intransitive()

libraryDependencies += "ch.qos.logback" % "logback-classic" % "0.9.28" % "runtime"

libraryDependencies += "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7"