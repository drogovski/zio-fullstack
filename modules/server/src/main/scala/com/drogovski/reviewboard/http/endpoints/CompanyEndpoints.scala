package com.drogovski.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.drogovski.reviewboard.domain.data.Company
import com.drogovski.reviewboard.http.requests.*

trait CompanyEndpoints extends BaseEndpoint {
  val createEndpoint =
    baseEndpoint
      .tag("companies")
      .name("create")
      .description("create a listing for a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("get all company listings")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getByIdEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("get company by id")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])
}
