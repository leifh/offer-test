
// Project dependencies

val akkaVersion = "2.5.12"
val akkaHttpVersion = "10.1.1"
val slickVersion = "3.2.3"
val macwireVersion = "2.3.0"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.3.0"
libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.14.0"


libraryDependencies += "com.typesafe.slick" %% "slick" % slickVersion
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

libraryDependencies += "com.h2database" % "h2" % "1.4.197"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

libraryDependencies += "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided
libraryDependencies += "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % Provided
libraryDependencies += "com.softwaremill.macwire" %% "util" % macwireVersion
libraryDependencies += "com.softwaremill.macwire" %% "proxy" % macwireVersion


libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.9.1"

// -------------

name := "offer-test"
version := "0.1.0-SNAPSHOT"

// Scala version used in the project
scalaVersion := "2.12.5"

// Emit warning and location for usages of features that should be imported explicitly.
scalacOptions += "-feature"

// The main class set in the manifest
mainClass in Compile := Some("com.github.leifh.offer.Application")

// don't run the tests in parallel
parallelExecution in Test := false

//native packager configuration
dockerExposedPorts := Seq(8080)
enablePlugins(JavaServerAppPackaging)
