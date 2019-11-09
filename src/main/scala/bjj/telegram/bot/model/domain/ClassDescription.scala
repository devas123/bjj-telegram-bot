package bjj.telegram.bot.model.domain

import java.util.Date

import bjj.telegram.bot.model.User
import org.mongodb.scala.bson.ObjectId

case class ClassDescription(_id: ObjectId, channelId: String, attendees: Option[List[User]], description: String, timeZone: Option[String], lessonPlan: Option[LessonPlan], creationTime: Date) extends WithObjectId with WithChannelId
