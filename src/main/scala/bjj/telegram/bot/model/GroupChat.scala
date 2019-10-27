package bjj.telegram.bot.model

/**
 * This object represents a group chat.
 * @param id Unique identifier for this group chat
 * @param title Group name
 */
case class GroupChat(id: Long, title: String) extends WithId
