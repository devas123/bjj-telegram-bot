package bjj.telegram.bot.model

import spray.json._
import spray.json.DefaultJsonProtocol


private [bot] object TelegramBotJsonProtocol extends DefaultJsonProtocol {

  //root objects

  implicit val failedResponseFormat: RootJsonFormat[FailedResponse] = jsonFormat1(FailedResponse)

  implicit val updateFormat: RootJsonFormat[Update] = jsonFormat2(Update)

  //entities

  implicit val groupChatFormat: RootJsonFormat[GroupChat] = jsonFormat2(GroupChat)

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)

  implicit val photoSizeFormat: RootJsonFormat[PhotoSize] = jsonFormat4(PhotoSize)

  implicit val documentFormat: RootJsonFormat[Document] = jsonFormat5(Document)

  implicit val videoFormat: RootJsonFormat[Video] = jsonFormat7(Video)

  implicit val audioFormat: RootJsonFormat[Audio] = jsonFormat6(Audio)

  implicit val stickerFormat: RootJsonFormat[Sticker] = jsonFormat5(Sticker)

  implicit val voiceFormat: RootJsonFormat[Voice] = jsonFormat4(Voice)

  implicit val contactFormat: RootJsonFormat[Contact] = jsonFormat4(Contact)

  implicit val locationFormat: RootJsonFormat[Location] = jsonFormat2(Location)

  implicit val rkmFormat: RootJsonFormat[ReplyKeyboardMarkup] = jsonFormat4(ReplyKeyboardMarkup)

  implicit val rkhFormat: RootJsonFormat[ReplyKeyboardHide] = jsonFormat2(ReplyKeyboardHide)

  implicit val frFormat: RootJsonFormat[ForceReply] = jsonFormat2(ForceReply)

  //messages formats

  implicit val textMessageFormat: RootJsonFormat[TextMessage] = jsonFormat8(TextMessage)

  implicit val audioMessageFormat: RootJsonFormat[AudioMessage] = jsonFormat8(AudioMessage)

  implicit val documentMessageFormat: RootJsonFormat[DocumentMessage] = jsonFormat8(DocumentMessage)

  implicit val photoMessageFormat: RootJsonFormat[PhotoMessage] = jsonFormat9(PhotoMessage)

  implicit val stickerMessageFormat: RootJsonFormat[StickerMessage] = jsonFormat8(StickerMessage)

  implicit val videoMessageFormat: RootJsonFormat[VideoMessage] = jsonFormat9(VideoMessage)

  implicit val voiceMessageFormat: RootJsonFormat[VoiceMessage] = jsonFormat8(VoiceMessage)

  implicit val contactMessageFormat: RootJsonFormat[ContactMessage] = jsonFormat8(ContactMessage)

  implicit val locationMessageFormat: RootJsonFormat[LocationMessage] = jsonFormat8(LocationMessage)

  implicit val memberAddedFormat: RootJsonFormat[MemberAddedToGroup] = jsonFormat8(MemberAddedToGroup)

  implicit val memberRemovedFormat: RootJsonFormat[MemberRemovedFromGroup] = jsonFormat8(MemberRemovedFromGroup)

  implicit val groupTitleChangedFormat: RootJsonFormat[GroupTitleChanged] = jsonFormat8(GroupTitleChanged)

  implicit val groupPhotoChangedFormat: RootJsonFormat[GroupPhotoChanged] = jsonFormat8(GroupPhotoChanged)

  implicit val groupPhotoDeletedFormat: RootJsonFormat[GroupPhotoDeleted] = jsonFormat7(GroupPhotoDeleted)

  implicit val groupChatCreatedFormat: RootJsonFormat[GroupChatCreated] = jsonFormat7(GroupChatCreated)


  implicit object ResponseFormat extends RootJsonFormat[Response] {
    def write(r: Response) = throw new UnsupportedOperationException

    def read(value: JsValue): Response = value.asJsObject.getFields("ok") match {
      case Seq(JsBoolean(ok)) if ok =>
        value.asJsObject.getFields("result") match {
          case Seq(JsArray(results)) => SuccessfulResponseWithUpdates(results.map(_.convertTo[Update]))
          case Seq(singleEntity) => SuccessfulResponse(singleEntity.convertTo[ResponseEntity])
        }
      case Seq(JsBoolean(ok)) if !ok => value.asJsObject.convertTo[FailedResponse]
      case _ => throw DeserializationException("Response expected")
    }
  }

  implicit object ResponseEntityFormat extends RootJsonFormat[ResponseEntity] {
    def write(r: ResponseEntity) = throw new UnsupportedOperationException

    def read(value: JsValue): ResponseEntity = value.asJsObject.fields.keys.collectFirst {
      case "update_id" => value.convertTo[Update]
      case "message_id" => value.convertTo[Message]
    }.getOrElse(throw DeserializationException("ResponseEntity expected"))
  }

  implicit object MessageFormat extends RootJsonFormat[Message] {
    def write(m: Message) = throw new UnsupportedOperationException

    def read(value: JsValue): Message = value.asJsObject.fields.keys.collectFirst {
      case "text" => value.convertTo[TextMessage]
      case "audio" => value.convertTo[AudioMessage]
      case "document" => value.convertTo[DocumentMessage]
      case "photo" => value.convertTo[PhotoMessage]
      case "sticker" => value.convertTo[StickerMessage]
      case "video" => value.convertTo[VideoMessage]
      case "voice" => value.convertTo[VoiceMessage]
      case "contact" => value.convertTo[ContactMessage]
      case "location" => value.convertTo[LocationMessage]
      case "new_chat_participant" => value.convertTo[MemberAddedToGroup]
      case "left_chat_participant" => value.convertTo[MemberRemovedFromGroup]
      case "new_chat_title" => value.convertTo[GroupTitleChanged]
      case "new_chat_photo" => value.convertTo[GroupPhotoChanged]
      case "delete_chat_photo" => value.convertTo[GroupPhotoDeleted]
      case "group_chat_created" => value.convertTo[GroupChatCreated]
    }.getOrElse(throw DeserializationException(s"Message expected, but got $value"))
  }

  implicit object ChatFormat extends RootJsonFormat[WithId] {
    def write(c: WithId) = throw new UnsupportedOperationException

    def read(value: JsValue): WithId = value.asJsObject.fields.keys.collectFirst {
      case "first_name" => value.convertTo[User]
      case "title" => value.convertTo[GroupChat]
    }.getOrElse(throw DeserializationException("User or GroupChat expected"))
  }

  implicit object ReplyMarkupFormat extends RootJsonFormat[ReplyMarkup] {
    def write(replyMarkup: ReplyMarkup): JsValue = replyMarkup match {
      case rkm: ReplyKeyboardMarkup => rkm.toJson
      case rkh: ReplyKeyboardHide => rkh.toJson
      case fr: ForceReply => fr.toJson
    }

    def read(value: JsValue) = throw new UnsupportedOperationException
  }
}
