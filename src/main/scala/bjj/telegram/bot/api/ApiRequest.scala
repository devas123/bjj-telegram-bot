package bjj.telegram.bot.api

import java.util.UUID

import bjj.telegram.bot.method.ApiMethod

/**
 * Class representing request to api
 * @param id unique id of the request
 * @param method request method
 */
case class ApiRequest(method: ApiMethod, id: UUID = UUID.randomUUID())
