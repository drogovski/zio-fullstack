package com.drogovski.reviewboard.http.controllers

import com.drogovski.reviewboard.http.endpoints.ReviewEndpoints
import com.drogovski.reviewboard.services.ReviewService

import zio.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.Endpoint
import com.drogovski.reviewboard.domain.data.Review

class ReviewController private (reviewService: ReviewService)
    extends BaseController
    with ReviewEndpoints {
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic { req =>
      reviewService.create(req, -1L /* TODO ADD USER ID */ ).either
    }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      reviewService.getById(id).either
    }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic { id =>
      reviewService.getByCompanyId(id).either
    }

  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.serverLogic { id =>
      reviewService.getByUserId(id).either
    }

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getById, getByCompanyId, getByUserId)
}

object ReviewController {
  val makeZIO = ZIO.service[ReviewService].map(service => new ReviewController(service))
}
