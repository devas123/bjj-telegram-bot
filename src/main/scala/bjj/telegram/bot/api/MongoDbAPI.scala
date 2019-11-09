package bjj.telegram.bot.api

import bjj.telegram.bot.di.ConfigModule
import bjj.telegram.bot.model.User
import bjj.telegram.bot.model.domain.ClassDescription
import org.mongodb.scala.model.{Filters, Updates, Indexes}
import org.mongodb.scala.result.UpdateResult

class MongoDbAPI extends ConfigModule {

  import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
  import org.mongodb.scala._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.mongodb.scala.bson.codecs.Macros._

  private val codecRegistry = fromRegistries(fromProviders(classOf[ClassDescription]), DEFAULT_CODEC_REGISTRY, fromProviders(classOf[User]))
  private val uri: String = config.getString("mongodb.url")
  private val classCollectionName = config.getString("mongodb.collections.class")
  private val client: MongoClient = MongoClient(uri)
  private val db: MongoDatabase = client.getDatabase("telegrambot").withCodecRegistry(codecRegistry)
  private val collection: MongoCollection[ClassDescription] = db.getCollection(classCollectionName)
  collection.createIndex(Indexes.descending("creationTime"))

  def saveClassDescritpion(classDescription: ClassDescription): SingleObservable[Completed] = {
    collection.insertOne(classDescription)
  }

  def getClassDescription(id: String): FindObservable[ClassDescription] = {
    collection.find(Filters.eq("_id", id))
  }

  def addAttendeeToTheLatest(user: User, channelId: String): Observable[UpdateResult] = {
    getLatestClassDescription(channelId).flatMap(cls => collection.updateOne(Filters.eq("_id", cls._id), Updates.addToSet("attendees", user)))
  }

  def getLatestClassDescription(channelId: String): SingleObservable[ClassDescription] = {
    import org.mongodb.scala.model.Sorts._
    collection.find(Filters.eq("channelId", channelId)).sort(descending("creationTime", "_id")).first()
  }

  def removeAttendeeFromTheLatest(user: User, channelId: String): Observable[UpdateResult] = {
    getLatestClassDescription(channelId).flatMap(cls => collection.updateOne(Filters.eq("_id", cls._id), Updates.pullByFilter(Filters.eq("_id", user.id))))
  }

  def stop(): Unit = {
    this.client.close()
  }

}
