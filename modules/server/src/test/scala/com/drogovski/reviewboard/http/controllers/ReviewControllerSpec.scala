package com.drogovski.reviewboard.http.controllers

import zio.test.ZIOSpecDefault
import zio.test.assertTrue
import zio.*
import zio.json.*
import zio.test.Spec
import zio.test.TestEnvironment
import sttp.monad.MonadError
import sttp.monad.syntax.MonadErrorOps
import sttp.tapir.ztapir.RIOMonadError
import sttp.client3.*
import com.drogovski.reviewboard.http.requests.CreateReviewRequest
import com.drogovski.reviewboard.domain.data.Review
import com.drogovski.reviewboard.services.ReviewService
import com.drogovski.reviewboard.syntax.*
import java.time.Instant
import sttp.tapir.server.ServerEndpoint
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.stub.TapirStubInterpreter

object ReviewControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val goodReview = Review(
    id = 1L,
    companyId = 1L,
    userId = 1L,
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 10,
    review = "This is awesome company",
    created = Instant.now(),
    updated = Instant.now()
  )

  private val serviceStub = new ReviewService {
    override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(goodReview)
    override def getById(id: Long): Task[Option[Review]] =
      ZIO.succeed {
        if (id == 1) Some(goodReview)
        else None
      }
    override def getByCompanyId(id: Long): Task[List[Review]] =
      ZIO.succeed {
        if (id == 1) List(goodReview)
        else List()
      }
    override def getByUserId(id: Long): Task[List[Review]] =
      ZIO.succeed {
        if (id == 1) List(goodReview)
        else List()
      }
  }

  private def backendStubZIO(endpointFun: ReviewController => ServerEndpoint[Any, Task]) =
    for {
      controller <- ReviewController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(zioME))
          .whenServerEndpointRunLogic(endpointFun(controller))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewControllerSpec")(
      test("post review") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"/reviews")
            .body(
              CreateReviewRequest(
                companyId = 1L,
                management = 5,
                culture = 5,
                salary = 5,
                benefits = 5,
                wouldRecommend = 10,
                review = "This is awesome company"
              ).toJson
            )
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
        }
      },
      test("get by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/reviews/1")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"/reviews/999")
            .send(backendStub)
        } yield assertTrue {
          response.body.toOption.flatMap(_.fromJson[Review].toOption).contains(goodReview) &&
          responseNotFound.body.toOption.flatMap(_.fromJson[Review].toOption).isEmpty
        }
      },
      test("get by company id") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
            .get(uri"/reviews/company/1")
            .send(backendStub)
          responseEmpty <- basicRequest
            .get(uri"/reviews/company/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview)) &&
            responseEmpty.body.toOption
              .flatMap(_.fromJson[List[Review]].toOption)
              .contains(List.empty)
        )

      },
      test("get by user id") {
        for {
          backendStub <- backendStubZIO(_.getByUserId)
          response <- basicRequest
            .get(uri"/reviews/user/1")
            .send(backendStub)
          responseEmpty <- basicRequest
            .get(uri"/reviews/user/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview)) &&
            responseEmpty.body.toOption
              .flatMap(_.fromJson[List[Review]].toOption)
              .contains(List.empty)
        )

      }
    ).provide(
      ZLayer.succeed(serviceStub)
    )
}
