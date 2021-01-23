name := "graphql-api"
version := "0.1"

scalaVersion := "2.12.12"
scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.bintrayRepo("freshwood", "maven")

val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val akkaHTTPRestClientVersion = "0.2.1"
val circeVersion = "0.9.3"
val akkaHttpCirceVersion = "1.21.0"
val scalaTestVersion = "3.0.5"
val macwireVersion = "2.3.7"
val sangriaVersion = "2.0.1"
val sangriaSlowLogVersion = "2.0.1"
val sangriaCirceVersion = "1.3.1"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-slowlog" % sangriaSlowLogVersion,
  "org.sangria-graphql" %% "sangria-circe" % sangriaCirceVersion,
  "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "net.softler" %% "akka-http-rest-client" % akkaHTTPRestClientVersion,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalatest" %% "scalatest-flatspec" % "3.2.0" % "test"
)

Revolver.settings
enablePlugins(JavaAppPackaging)
