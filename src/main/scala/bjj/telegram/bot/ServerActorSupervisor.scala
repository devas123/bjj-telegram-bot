package bjj.telegram.bot

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, PoisonPill, Props}

import scala.concurrent.duration._

class ServerActorSupervisor(token: String) extends Actor with ActorLogging{

  private val server = context.actorOf(Props(classOf[ServerActor]))

  import akka.actor.{OneForOneStrategy, SupervisorStrategy}

  private val strategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case _: NullPointerException     => Restart
    case _: IllegalArgumentException => Restart
    case _: Exception                => Escalate
  }



  override def supervisorStrategy: SupervisorStrategy = strategy


  override def receive: Receive = {
    case p@PoisonPill =>
      server ! p
      context.system.terminate()
    case _ => log.warning("Unknown message")
  }
}
