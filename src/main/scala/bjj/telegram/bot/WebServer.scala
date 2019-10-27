package bjj.telegram.bot

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.event.slf4j.Logger
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val logger = Logger("main")

  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val token = config.getString("bot.token")
    val serverActor = system.actorOf(Props(new ServerActorSupervisor(token)), name = "serverSupervisor")

    logger.info(s"Server online at http://localhost:8191/")
    if (args.contains("dev")) {
      logger.info("Dev mode, Press RETURN to stop...")
      StdIn.readLine()
      serverActor ! PoisonPill
    }
  }
}
