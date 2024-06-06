package com.drogovski.reviewboard.repositories

import zio.test.ZIOSpecDefault
import zio.Scope
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.assertTrue
import java.time.Instant
import com.drogovski.reviewboard.domain.data.Review
import com.drogovski.reviewboard.syntax.*
import zio.*
import java.sql.SQLException

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec {

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

  private val badReview = Review(
    id = 2L,
    companyId = 1L,
    userId = 1L,
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 0,
    review = "It's trash lol",
    created = Instant.now(),
    updated = Instant.now()
  )

  override val initScript: String = "sql/reviews.sql"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewRepositorySpec")(
      test("create a review") {
        val program = for {
          repository <- ZIO.service[ReviewRepository]
          review     <- repository.create(goodReview)
        } yield review

        program.assert { review =>
          review.management == goodReview.management &&
          review.culture == goodReview.culture &&
          review.salary == goodReview.salary &&
          review.benefits == goodReview.benefits &&
          review.wouldRecommend == goodReview.wouldRecommend &&
          review.review == goodReview.review
        }
      },
      test("get by ids (id, companyId, userId)") {
        for {
          repository         <- ZIO.service[ReviewRepository]
          review             <- repository.create(goodReview)
          fetchedById        <- repository.getById(review.id)
          fetchedByUserId    <- repository.getByUserId(review.userId)
          fetchedByCompanyId <- repository.getByCompanyId(review.companyId)

        } yield assertTrue(
          fetchedById.contains(review) &&
            fetchedByCompanyId.contains(review) &&
            fetchedByUserId.contains(review)
        )
      },
      test("get all reviews by companyId and userId") {
        for {
          repository                  <- ZIO.service[ReviewRepository]
          review1                     <- repository.create(goodReview)
          review2                     <- repository.create(badReview)
          companiesFetchedByCompanyId <- repository.getByCompanyId(review1.companyId)
          companiesFetchedByUserId    <- repository.getByUserId(review1.userId)
        } yield assertTrue(
          companiesFetchedByUserId.toSet == Set(review1, review2) &&
            companiesFetchedByCompanyId.toSet == Set(review1, review2)
        )
      },
      test("edit review") {
        for {
          repository <- ZIO.service[ReviewRepository]
          review     <- repository.create(goodReview)
          updatedReview <- repository.update(
            review.id,
            _.copy(review = "I still think that this company is awesome.")
          )
          fetchedUpdated <- repository.getById(review.id)
        } yield assertTrue(
          review.id == updatedReview.id &&
            review.companyId == updatedReview.companyId &&
            review.userId == updatedReview.userId &&
            review.management == updatedReview.management &&
            review.culture == updatedReview.culture &&
            review.salary == updatedReview.salary &&
            review.benefits == updatedReview.benefits &&
            review.wouldRecommend == updatedReview.wouldRecommend &&
            updatedReview.review == "I still think that this company is awesome." &&
            review.created == updatedReview.created &&
            review.updated != updatedReview.updated
        )
      },
      test("delete review") {
        for {
          repository     <- ZIO.service[ReviewRepository]
          review         <- repository.create(goodReview)
          deleted        <- repository.delete(goodReview.id)
          fetchedDeleted <- repository.getById(review.id)
        } yield assertTrue(
          deleted == review && fetchedDeleted.isEmpty
        )
      }
    ).provide(
      ReviewRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
}
