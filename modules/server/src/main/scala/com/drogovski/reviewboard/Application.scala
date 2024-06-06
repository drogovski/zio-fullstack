package com.drogovski.reviewboard

import zio.*
import sttp.tapir.server.ziohttp.*
import zio.http.Server
import sttp.tapir.server.ziohttp.*
import com.drogovski.reviewboard.http.controllers.*
import com.drogovski.reviewboard.http.HttpApi
import com.drogovski.reviewboard.services.CompanyServiceLive
import com.drogovski.reviewboard.repositories.CompanyRepositoryLive
import com.drogovski.reviewboard.repositories.Repository
import com.drogovski.reviewboard.services.ReviewServiceLive
import com.drogovski.reviewboard.repositories.ReviewRepositoryLive

object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.zioEndpoints
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(endpoints)
    )
  } yield ()

  override def run =
    serverProgram.provide(
      Server.default,
      CompanyRepositoryLive.layer,
      CompanyServiceLive.layer,
      ReviewRepositoryLive.layer,
      ReviewServiceLive.layer,
      Repository.dataLayer
    )
}
