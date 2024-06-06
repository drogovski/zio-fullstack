package com.drogovski.reviewboard.http.controllers

import com.drogovski.reviewboard.http.endpoints.HealthEndpoint
import zio.*
import sttp.tapir.*
import com.drogovski.reviewboard.domain.errors.HttpError

class HealthController private extends BaseController with HealthEndpoint {
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

  val errorRoute = errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Boom!")).either)

  val routes = List(health, errorRoute)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
