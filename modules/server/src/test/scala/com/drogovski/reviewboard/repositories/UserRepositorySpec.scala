package com.drogovski.reviewboard.repositories

import com.drogovski.reviewboard.domain.data.User
import zio.test.ZIOSpecDefault
import zio.test.assertTrue
import zio.Scope
import zio.test.Spec
import zio.test.TestEnvironment
import zio.*

object UserRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  private val firstUser = User(
    id = 1L,
    email = "example@email.com",
    hashedPassword = "21sas3232dsef32"
  )

  override val initScript: String = "sql/users.sql"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserRepositorySpec")(
      test("create a user") {
        for {
          repository <- ZIO.service[UserRepository]
          user       <- repository.create(firstUser)
        } yield assertTrue {
          user.id == firstUser.id &&
          user.email == firstUser.email &&
          user.hashedPassword == firstUser.hashedPassword
        }
      },
      test("get user by id") {
        for {
          repository                 <- ZIO.service[UserRepository]
          user                       <- repository.create(firstUser)
          userFetchedById            <- repository.getById(user.id)
          nonExistentUserFetchedById <- repository.getById(999)
        } yield assertTrue {
          userFetchedById.contains(user) &&
          nonExistentUserFetchedById.isEmpty
        }
      },
      test("get user by email") {
        for {
          repository                    <- ZIO.service[UserRepository]
          user                          <- repository.create(firstUser)
          userFetchedByEmail            <- repository.getByEmail(user.email)
          nonExistentUserFetchedByEmail <- repository.getByEmail("nonexisting@email.com")
        } yield assertTrue {
          userFetchedByEmail.contains(user) &&
          nonExistentUserFetchedByEmail.isEmpty
        }
      }
    ).provide(
      UserRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
