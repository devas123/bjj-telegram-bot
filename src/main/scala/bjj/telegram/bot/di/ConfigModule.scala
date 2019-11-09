package bjj.telegram.bot.di

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigModule {
  val config: Config = ConfigFactory.load()
  lazy val token: String = config.getString("bot.token")
}
