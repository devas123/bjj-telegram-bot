package bjj.telegram.bot

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, PoisonPill}
import bjj.telegram.bot.api.MongoDbAPI
import bjj.telegram.bot.di.ConfigModule
import com.softwaremill.macwire.akkasupport._

import scala.concurrent.duration._

class ServerActorSupervisor(devMode: Boolean = false, private val mongoDbAPI: MongoDbAPI) extends Actor with ConfigModule with ActorLogging{
  import akka.actor.{OneForOneStrategy, SupervisorStrategy}
  private val server = wireActor[ServerActor]("server")

  private val strategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case _: NullPointerException     => Restart
    case _: IllegalArgumentException => Restart
    case _: Exception                => Escalate
  }



  override def supervisorStrategy: SupervisorStrategy = strategy


  override def receive: Receive = {
    case p@PoisonPill =>
      server ! p
    case _ => log.warning("Unknown message")
  }
}
