package com.drogovski.reviewboard.http

import com.drogovski.reviewboard.http.controllers.*

object HttpApi {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  def makeControllers = for {
    health    <- HealthController.makeZIO
    companies <- CompanyController.makeZIO
    reviews   <- ReviewController.makeZIO
  } yield List(health, companies, reviews)

  val zioEndpoints = makeControllers.map(gatherRoutes)
}
