package com.drogovski.reviewboard.http.controllers

import zio.*
import zio.test.*
import zio.json.*
import com.drogovski.reviewboard.http.controllers.CompanyController
import sttp.monad.MonadError
import sttp.tapir.generic.auto.*
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import com.drogovski.reviewboard.domain.data.Company
import com.drogovski.reviewboard.http.requests.CreateCompanyRequest
import com.drogovski.reviewboard.syntax.*
import sttp.tapir.server.ServerEndpoint
import com.drogovski.reviewboard.services.CompanyService

object CompanyControllerSpec extends ZIOSpecDefault {

  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val testCompany = Company(1, "test-company", "Test Company", "testcompany.com")
  private val serviceStub = new CompanyService{
    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(testCompany)
    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed {
        if (id == 1) Some(testCompany)
        else None
      }
    override def getBySlug(slug: String): Task[Option[Company]] =
      ZIO.succeed {
        if (slug == "test-company") Some(testCompany)
        else None
      }
    override def getAll(): Task[List[Company]] =
      ZIO.succeed(List(testCompany))
  }

  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) =
    for {
      controller <- CompanyController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(zioME))
          .whenServerEndpointRunLogic(endpointFun(controller))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post company") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("Test Company", "testcompany.com").toJson)
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption)
            .contains(Company(1, "test-company", "Test Company", "testcompany.com"))
        }
      },
      test("getAll") {
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
            .get(uri"/companies")
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[List[Company]].toOption)
            .contains(List(testCompany))
        }
      },
      test("get by id") {
        val program = for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/companies/1")
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption)
            .contains(testCompany)
        }
      }
    ).provide(ZLayer.succeed(serviceStub))
}
