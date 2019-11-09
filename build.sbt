name := "bjj-telegram-bot"

version := "0.1-SNAPSHOT"

isSnapshot := true

scalaVersion := "2.12.7"

maintainer := "devas7sky@gmail.com"

val akkaHttpVersion = "10.1.10"
val akkaVersion = "2.5.26"
val wireVersion = "2.3.3"

credentials += Credentials("Some Nexus Repository Manager", "95.169.186.20:8081", "admin", "admin123")
credentials += Credentials("Sonatype Nexus Repository Manager", "95.169.186.20", "admin", "admin123")
resolvers += "Compmanager Releases" at "http://95.169.186.20:8081/repository/compman-releases/"
resolvers += "Compmanager Snapshots" at "http://95.169.186.20:8081/repository/compman-snapshots/"
resolvers += DefaultMavenRepository
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.bintrayRepo("akka", "snapshots")

publishTo := {
  val nexus = "http://95.169.186.20:8081/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "repository/compman-snapshots")
  else
    Some("releases" at nexus + "repository/compman-releases")
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0"
libraryDependencies += "com.google.guava" % "guava" % "28.1-jre"
libraryDependencies += "com.softwaremill.macwire" %% "macros" % wireVersion % "provided"

libraryDependencies += "com.softwaremill.macwire" %% "macrosakka" % wireVersion % "provided"

libraryDependencies += "com.softwaremill.macwire" %% "util" % wireVersion

libraryDependencies += "com.softwaremill.macwire" %% "proxy" % wireVersion


enablePlugins(JavaAppPackaging)
packageName in Universal := "application"

updateOptions := updateOptions.value.withGigahorse(false)

