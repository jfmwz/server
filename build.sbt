import sbt._
import KotonohaBuild._

organization := "ws.kotonoha"

name := "server"

version := "0.1-SNAPSHOT"

moduleName := "kotonoha"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies ++=
					Seq("org.scalaz" %% "scalaz-core" % "6.0.4",
						"com.jsuereth" %% "scala-arm" % "1.2",
            "javax.transaction" % "jta" % "1.0.1B" % "provided"
					)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.1"

resolvers += "eiennohito's repo" at "http://eiennohito.github.com/maven/"

libraryDependencies += "fuku.eb4j" % "eb4j-tools" % "1.0.5"

libraryDependencies ++= {
  val liftVersion = "2.5-M2" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-util" % liftVersion % "compile->default" exclude("joda-time", "joda-time"),
    "net.liftweb" %% "lift-json-ext" % liftVersion % "compile->default" exclude("joda-time", "joda-time"),
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mongodb-record" % liftVersion exclude("org.mongodb", "mongo-java-driver"),
    "net.liftweb" %% "lift-json-scalaz" % liftVersion exclude("org.scalaz", "scalaz-core_2.9.1"),
    "net.liftmodules" %% "oauth" % ("2.5-M2-1.1-kotonoha"),
    "net.liftweb" %% "lift-testkit" % liftVersion % "test",
	  "javax.servlet" % "servlet-api" % "2.5" % "provided")
}

libraryDependencies ++= {
  Seq(
    "org.mongodb" %% "casbah" % "2.4.1" exclude("org.specs2", "*")
  )
}

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaVer = "2.0.2"
  Seq(
    "com.typesafe.akka" % "akka-actor" % akkaVer,
    "com.typesafe.akka" % "akka-slf4j" % akkaVer,
    "com.typesafe.akka" % "akka-testkit" % akkaVer % "test"
  )
}

libraryDependencies +=  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"

libraryDependencies += "com.foursquare" % "rogue_2.9.1" % "1.1.8" intransitive()

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.0" % "compile"

libraryDependencies += "com.weiglewilczek.slf4s" % "slf4s_2.9.1" % "1.0.7"

libraryDependencies += "com.j256.ormlite" % "ormlite-jdbc" % "4.33" % "test"

resolvers += "some weird japanese repo" at "http://dev.mwsoft.jp/repo/"

libraryDependencies += "net.java.sen" % "lucene-gosen" % "2.1" exclude ("org.slf4j", "slf4j-jdk14")

libraryDependencies += "com.h2database" % "h2" % "1.3.163" % "test"

libraryDependencies += "com.google.zxing" % "javase" % "2.0"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.47"

javacOptions ++= Seq("-encoding", "utf8")

scalacOptions ++= Seq("-unchecked")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion, sbtVersion, gitId, gitDate)

buildInfoPackage := "ws.kotonoha.server.util"

buildInfoObject  := "BuildInfo"

scanInterval in Compile := 0

net.virtualvoid.sbt.graph.Plugin.graphSettings
