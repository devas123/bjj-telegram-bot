package bjj.telegram.bot.api

import java.util.UUID

import bjj.telegram.bot.model.Response

import scala.util.Try

/**
 * Class representing response from api
 * @param id unique id of the response
 * @param response response result
 */
case class ApiResponse(id: UUID, response: Try[Response])
