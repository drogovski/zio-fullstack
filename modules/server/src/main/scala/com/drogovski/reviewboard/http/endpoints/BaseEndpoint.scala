package com.drogovski.reviewboard.http.endpoints

import sttp.tapir.*
import com.drogovski.reviewboard.domain.errors.HttpError

trait BaseEndpoint {
  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
}
