package bjj.telegram.bot.model.domain

import org.mongodb.scala.bson.ObjectId

case class LessonPlan(_id: ObjectId, topic: String, tags: Option[List[String]]) extends WithObjectId
