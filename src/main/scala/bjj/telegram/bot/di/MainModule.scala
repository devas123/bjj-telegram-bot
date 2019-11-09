package bjj.telegram.bot.di

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import bjj.telegram.bot.api.MongoDbAPI

import scala.concurrent.ExecutionContextExecutor

trait MainModule extends ConfigModule {
  import com.softwaremill.macwire._
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  lazy val logger = Logger("main")
  lazy val mongoDbAPI: MongoDbAPI = wire[MongoDbAPI]
}
