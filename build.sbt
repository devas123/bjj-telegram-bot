name := "bjj-telegram-bot"

version := "0.1"

scalaVersion := "2.12.7"

val akkaHttpVersion = "10.1.10"
val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)