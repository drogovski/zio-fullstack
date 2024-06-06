package com.drogovski.reviewboard.http.controllers

import collection.mutable
import zio.*
import com.drogovski.reviewboard.http.endpoints.CompanyEndpoints
import com.drogovski.reviewboard.domain.data.*
import sttp.tapir.server.ServerEndpoint
import com.drogovski.reviewboard.services.CompanyService

class CompanyController private (service: CompanyService)
    extends BaseController
    with CompanyEndpoints {

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { req =>
    service.create(req).either
  }

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic { _ =>
      service.getAll().either
    }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById)
      .catchSome { case _: NumberFormatException =>
        service.getBySlug(id)
      }
      .either
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}

object CompanyController {
  val makeZIO = for {
    service <- ZIO.service[CompanyService]
  } yield new CompanyController(service)
}
