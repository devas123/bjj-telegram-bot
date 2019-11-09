package bjj.telegram.bot

import akka.actor.{ActorRef, PoisonPill}
import bjj.telegram.bot.di.MainModule

import scala.io.StdIn

object WebServer extends MainModule {

  def main(args: Array[String]) {
    import com.softwaremill.macwire.akkasupport._
    val devMode = args.contains("dev")
    val serverActor: ActorRef = wireActor[ServerActorSupervisor]("serverActorSupervisor")
    logger.info(s"Server online at http://localhost:8191/")
    if (devMode) {
      logger.info("Dev mode, Press RETURN to stop...")
      StdIn.readLine()
      serverActor ! PoisonPill
      mongoDbAPI.stop()
      system.terminate().andThen({
        case _ => System.exit(0)
      })
    }
  }
}
