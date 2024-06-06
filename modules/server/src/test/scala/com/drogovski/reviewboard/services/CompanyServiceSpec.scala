package com.drogovski.reviewboard.services

import zio.*
import zio.test.*
import com.drogovski.reviewboard.http.requests.CreateCompanyRequest
import com.drogovski.reviewboard.syntax.*
import com.drogovski.reviewboard.repositories.CompanyRepository
import com.drogovski.reviewboard.domain.data.Company

object CompanyServiceSpec extends ZIOSpecDefault {
  val service = ZIO.serviceWithZIO[CompanyService]

  val stubRepoLayer = ZLayer.succeed(
    new CompanyRepository {
      val db = collection.mutable.Map[Long, Company]()

      override def create(company: Company): Task[Company] =
        ZIO.succeed {
          val nextId     = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = nextId)
          db += (nextId -> newCompany)
          newCompany
        }

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt {
          val company        = db(id)
          val updatedCompany = op(company)
          db += (id -> updatedCompany)
          updatedCompany
        }

      override def delete(id: Long): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          db -= id
          company
        }

      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed {
          db.values.find(_.slug == slug)
        }

      override def getAll(): Task[List[Company]] =
        ZIO.succeed(db.values.toList)
    }
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyServiceTest")(
      test("create") {
        val companyZIO = service(
          _.create(
            CreateCompanyRequest(
              "Test Company",
              "testcompany.com"
            )
          )
        )

        companyZIO.assert { company =>
          company.name == "Test Company" &&
          company.slug == "test-company" &&
          company.url == "testcompany.com"
        }
      },
      test("get by id") {
        val getByIdProgram = for {
          company <- service(
            _.create(
              CreateCompanyRequest(
                "Test Company",
                "testcompany.com"
              )
            )
          )
          companyOpt <- service(_.getById(company.id))
        } yield (company, companyOpt)

        getByIdProgram.assert {
          case (company, Some(companyOpt)) =>
            company.name == "Test Company" &&
            company.slug == "test-company" &&
            company.url == "testcompany.com" &&
            company == companyOpt
          case _ => false
        }
      },
      test("get by slug") {
        val getBySlugProgram = for {
          company <- service(
            _.create(
              CreateCompanyRequest(
                "Test Company",
                "testcompany.com"
              )
            )
          )
          companyOpt <- service(_.getBySlug(company.slug))
        } yield (company, companyOpt)

        getBySlugProgram.assert {
          case (company, Some(companyOpt)) =>
            company.name == "Test Company" &&
            company.slug == "test-company" &&
            company.url == "testcompany.com" &&
            company == companyOpt
          case _ => false
        }
      },
      test("get all") {
        val getAllProgram = for {
          company1 <- service(
            _.create(
              CreateCompanyRequest(
                "Test Company",
                "testcompany.com"
              )
            )
          )
          company2 <- service(
            _.create(
              CreateCompanyRequest(
                "Another Company",
                "anothercompany.com"
              )
            )
          )
          companies <- service(_.getAll())
        } yield (company1, company2, companies)

        getAllProgram.assert { case (company1, company2, companies) =>
          companies.toSet == Set(company1, company2)
        }
      }
    ).provide(
      CompanyServiceLive.layer,
      stubRepoLayer
    )
}
