package bjj.telegram.bot

import java.util.{Date, UUID}

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import bjj.telegram.bot.api.ApiHelper.apiUri
import bjj.telegram.bot.api.{ApiRequest, MongoDbAPI, UpdateReceiver}
import bjj.telegram.bot.method.SendMessage
import bjj.telegram.bot.model.TelegramBotJsonProtocol._
import bjj.telegram.bot.model._
import bjj.telegram.bot.model.domain.ClassDescription
import com.google.common.cache.CacheBuilder
import com.typesafe.config.ConfigFactory
import org.bson.types.ObjectId
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.Try

class ServerActor(private val devMode: Boolean = false, private val mongodbApi: MongoDbAPI) extends Actor with ActorLogging with SprayJsonSupport {
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()
  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val config = ConfigFactory.load()
  private val PORT = 8191
  private val BOT_TOKEN = config.getString("bot.token")
  private val availableProcessors = Runtime.getRuntime.availableProcessors()
  private val idCache = CacheBuilder.newBuilder().maximumSize(1000).build[String, String]()
  val updateReceiver = new UpdateReceiver(BOT_TOKEN, idCache)


  override def preStart(): Unit = {
    log.info("Starting!")
    Runtime.getRuntime.addShutdownHook(new Thread(() => bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => {
        val onceAllConnectionsTerminated: Future[Http.HttpTerminated] =
          Await.result(bindingFuture, 10.seconds)
            .terminate(hardDeadline = 3.seconds)
        onceAllConnectionsTerminated.flatMap { _ =>
          context.system.terminate()
        }
      }
      )))
  }

  private val apiRequestToHttpRequest: Flow[ApiRequest, Option[(HttpRequest, UUID)], NotUsed] =
    Flow[ApiRequest].mapAsyncUnordered(4) {
      case ApiRequest(method, id) =>
        val filteredParams = method.allParams.filterNot(_._2 == None).map {
          case (name, Some(value)) => name -> value
          case e@(_, _) => e
        }
        val entity = createEntity(filteredParams)
        val Uri = apiUri(method.name, BOT_TOKEN)
        entity.map { e =>
          Some(HttpRequest(method = POST, uri = Uri, entity = e) -> id)
        }
    }

  private val httpResponse: Flow[(Try[HttpResponse], UUID), HttpResponse, NotUsed] =
    Flow[(Try[HttpResponse], UUID)]
      .mapAsync(availableProcessors) { case (tryResponse, _) =>
        tryResponse.
          map(response => Future.successful(response))
          .recover {
            case e => Future.successful(HttpResponse(entity = e.getMessage))
          }.get
      }


  val apiFlow: Flow[ApiRequest, HttpResponse, NotUsed] =
    Flow[ApiRequest]
      .via(apiRequestToHttpRequest)
      .filter(_.nonEmpty)
      .map(_.get)
      .mapAsyncUnordered(availableProcessors) { request =>
        if (devMode) {
          log.info(s"Sending to telegram is skipped because we're in dev mode. Mesage was: ${request._1}")
          Future.successful(Try(HttpResponse()) -> request._2)
        } else {
          Http().singleRequest(request._1).map(resp => Try(resp) -> request._2)
        }
      }
      .via(httpResponse)


  private def createEntity(params: Map[String, Any]): Future[RequestEntity] = {
    def stringBodyPart(name: String, value: Any) = Multipart.FormData.BodyPart(
      name,
      HttpEntity(value.toString)
    )

    val formData =
      Multipart.FormData(
        params.toSeq.map { case (name, value) =>
          value match {
            case l: Long => stringBodyPart(name, l)
            case s: String => stringBodyPart(name, s)
            case b: Boolean => stringBodyPart(name, b)
            case d: Double => stringBodyPart(name, d)
            case rm: ReplyMarkup => stringBodyPart(name, rm.toJson.compactPrint)
            case InputFile(file) =>
              Multipart.FormData.BodyPart.fromFile(
                name,
                MediaTypes.`application/octet-stream`,
                file,
                100000
              )
            case StreamedInputFile(fileName, contentType, length, dataBytes) =>
              Multipart.FormData.BodyPart(
                name,
                HttpEntity(
                  contentType,
                  length,
                  dataBytes
                ),
                Map("filename" -> fileName)
              )
          }
        }: _*
      )
    Marshal(formData).to[RequestEntity]
  }

  val reactToConnectionFailure: Flow[HttpRequest, HttpRequest, NotUsed] = Flow[HttpRequest]
    .recover[HttpRequest] {
      case ex =>
        log.error("Error", ex)
        throw ex
    }
  val messageLogicFlow: Flow[Option[Message], ApiRequest, NotUsed] = Flow[Option[Message]]
    .map({
      case Some(msg) =>
        val default = s"Чето я ничо не понял... Чо это такое вообще?? (${msg.message_id})"
        val reply = msg match {
          case TextMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, t) =>
            log.info(s"Received text message: $message_id, $from, $date, $chat, $forward_from, $forward_date, $reply_to_message, $t")
            val text = t.trim

            if (text.startsWith("/test")) {
              "Пассед, епта..."
            } else if (text.startsWith("/create")) {
              val description = text.stripPrefix("/create")
              if (description.nonEmpty) {
                Await.result(mongodbApi.saveClassDescritpion(ClassDescription(ObjectId.get(), chat.id.toString, None, s"$description (${from.first_name}, ${from.username.getOrElse("")})", None, None, new Date())).toFuture(), 10.seconds)
                s"Создал новую тренировку: $description. By ${from.first_name}, ${from.username.getOrElse("")}"
              } else {
                "Описание тренировки: когда, во сколько, какая тема, всё такое..."
              }
            } else if (text.startsWith("/info")) {
              val cls = Await.result(mongodbApi.getLatestClassDescription(chat.id.toString).toFuture(), 10.seconds)
              if (cls != null && cls.creationTime.after(new Date(System.currentTimeMillis() - 7 * 24 * 3600 * 1000))) {
                s"Следующая тренировка: ${cls.description}"
              } else {
                s"Нет информации за следующую тренировку."
              }
            }
            else {
              default
            }
          case AudioMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, audio) => default
          case DocumentMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, document) => default
          case PhotoMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, caption, photo) => default
          case StickerMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, sticker) => default
          case VideoMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, caption, video) => default
          case VoiceMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, voice) => default
          case ContactMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, contact) => default
          case LocationMessage(message_id, from, date, chat, forward_from, forward_date, reply_to_message, location) => default
          case MemberAddedToGroup(message_id, from, date, chat, forward_from, forward_date, reply_to_message, new_chat_participant) => default
          case MemberRemovedFromGroup(message_id, from, date, chat, forward_from, forward_date, reply_to_message, left_chat_participant) => default
          case GroupTitleChanged(message_id, from, date, chat, forward_from, forward_date, reply_to_message, new_chat_title) => default
          case GroupPhotoChanged(message_id, from, date, chat, forward_from, forward_date, reply_to_message, new_chat_photo) => default
          case GroupPhotoDeleted(message_id, from, date, chat, forward_from, forward_date, reply_to_message) => default
          case GroupChatCreated(message_id, from, date, chat, forward_from, forward_date, reply_to_message) => default
        }
        log.info(s"Reply is: $reply")
        ApiRequest(SendMessage(msg.chat.id, reply))

      case None => ApiRequest(SendMessage(0L, s"I received no message ;("))
    })
    .recover({
      case t =>
        log.error("Exception.", t)
        ApiRequest(SendMessage(0L, "Something's wrong, check logs."))
    })

  val asyncResponseFlow: Flow[Option[Message], HttpResponse, NotUsed] =
    Flow[Option[Message]]
      .via(messageLogicFlow)
      .via(apiFlow)
      .map(msg => {
        log.info(s"Response from telegram: $msg")
        if (msg.status == StatusCodes.OK) {
          msg
        } else {
          HttpResponse()
        }
      })

  val requestHandler: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest]
    .via(reactToConnectionFailure)
    .map(request => request.entity)
    .mapAsync(availableProcessors)(b => Unmarshal(b).to[Update])
    .via(updateReceiver)
    .log("/new_message Request")
    .via(asyncResponseFlow)
    .log("/new_message Response")

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(requestHandler, "0.0.0.0", PORT)

  override def receive: Receive = {
    case PoisonPill =>
      log.info("Stopping.")
    case x: Any => log.warning(s"Unknown message: $x")
  }

  override def postStop(): Unit = {
    Await.result(bindingFuture
      .flatMap(_.unbind()), 10.seconds)
    log.info("Stopped server.")
  }
}
