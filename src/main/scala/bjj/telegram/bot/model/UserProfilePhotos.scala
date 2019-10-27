package bjj.telegram.bot.model

case class UserProfilePhotos(total_count: Int, photos: Seq[Seq[PhotoSize]]) extends ResponseEntity
