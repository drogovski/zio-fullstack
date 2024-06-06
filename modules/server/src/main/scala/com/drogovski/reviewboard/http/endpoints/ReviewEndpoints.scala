package com.drogovski.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.drogovski.reviewboard.http.requests.CreateReviewRequest
import com.drogovski.reviewboard.domain.data.Review

trait ReviewEndpoints extends BaseEndpoint {
  val createEndpoint =
    baseEndpoint
      .tag("reviews")
      .name("create")
      .description("create a review")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getByIdEndpoint =
    baseEndpoint
      .tag("reviews")
      .name("getById")
      .description("get review by id")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[Option[Review]])

  val getByUserIdEndpoint =
    baseEndpoint
      .tag("reviews")
      .name("getByUserId")
      .description("get reviews by user id")
      .in("reviews" / "user" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])

  val getByCompanyIdEndpoint =
    baseEndpoint
      .tag("reviews")
      .name("getByCompanyId")
      .description("get reviews for company")
      .in("reviews" / "company" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])
}
