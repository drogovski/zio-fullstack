package com.drogovski.reviewboard.repositories

import zio.*
import zio.test.*
import com.drogovski.reviewboard.domain.data.Company
import com.drogovski.reviewboard.syntax.*
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import java.sql.SQLException

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  private val testCompany = Company(1L, "test-company", "Test Company", "testcompany.com")

  private def genString(): String =
    scala.util.Random.alphanumeric.take(8).mkString

  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString()
    )

  override val initScript: String = "sql/companies.sql"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          company    <- repository.create(testCompany)
        } yield company

        program.assert {
          case (Company(_, "test-company", "Test Company", "testcompany.com", _, _, _, _, _)) =>
            true
          case _ => false
        }
      },
      test("prevent creating a duplicate company") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          _          <- repository.create(testCompany)
          err        <- repository.create(testCompany).flip
        } yield err

        program.assert(_.isInstanceOf[SQLException])
      },
      test("get company by id and by slug") {
        val program = for {
          repository    <- ZIO.service[CompanyRepository]
          company       <- repository.create(testCompany)
          fetchedById   <- repository.getById(company.id)
          fetchedBySlug <- repository.getBySlug(company.slug)
        } yield (company, fetchedById, fetchedBySlug)

        program.assert { case (company, fetchedById, fetchedBySlug) =>
          fetchedById.contains(company) && fetchedBySlug.contains(company)
        }
      },
      test("update company") {
        val program = for {
          repository     <- ZIO.service[CompanyRepository]
          company        <- repository.create(testCompany)
          updatedCompany <- repository.update(company.id, _.copy(url = "newdomain.testcompany.com"))
          fetchedById    <- repository.getById(company.id)
        } yield (updatedCompany, fetchedById)

        program.assert { case (updatedCompany, fetchedById) =>
          fetchedById.contains(updatedCompany)
        }
      },
      test("delete company") {
        val program = for {
          repository  <- ZIO.service[CompanyRepository]
          company     <- repository.create(testCompany)
          deleted     <- repository.delete(company.id)
          fetchedById <- repository.getById(company.id)
        } yield (company, deleted, fetchedById)

        program.assert { case (company, deleted, fetchedById) =>
          company == deleted && fetchedById.isEmpty
        }
      },
      test("get all companies") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          companies  <- ZIO.collectAll((1 to 10).map(_ => repository.create(genCompany())))
          fetchedAll <- repository.getAll()
        } yield (companies, fetchedAll)

        program.assert { case (companies, fetchedAll) =>
          companies.toSet == fetchedAll.toSet
        }
      }
    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
}
