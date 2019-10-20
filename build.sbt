name := "bjj-telegram-bot"

version := "0.1-SNAPSHOT"

isSnapshot := true

scalaVersion := "2.12.7"

maintainer := "devas7sky@gmail.com"

val akkaHttpVersion = "10.1.10"
val akkaVersion = "2.5.23"

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
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

enablePlugins(JavaAppPackaging)
packageName in Universal := "application"

updateOptions := updateOptions.value.withGigahorse(false)

