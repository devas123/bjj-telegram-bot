package bjj.telegram.bot.model.domain

import org.mongodb.scala.bson.ObjectId

trait WithObjectId {
  def _id: ObjectId
}
