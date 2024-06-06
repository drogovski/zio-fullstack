package com.drogovski.reviewboard.services

import com.drogovski.reviewboard.domain.data.Review
import zio.*
import com.drogovski.reviewboard.http.requests.CreateReviewRequest
import com.drogovski.reviewboard.repositories.ReviewRepository
import java.time.Instant

trait ReviewService {
  def create(req: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]
  // def update()
}

class ReviewServiceLive private (repository: ReviewRepository) extends ReviewService {
  override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
    repository.create(
      Review(
        id = -1L,
        companyId = req.companyId,
        userId = userId,
        management = req.management,
        culture = req.culture,
        salary = req.salary,
        benefits = req.benefits,
        wouldRecommend = req.wouldRecommend,
        review = req.review,
        created = Instant.now(),
        updated = Instant.now()
      )
    )
  override def getById(id: Long): Task[Option[Review]] =
    repository.getById(id)
  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repository.getByCompanyId(companyId)
  override def getByUserId(userId: Long): Task[List[Review]] =
    repository.getByUserId(userId)

}

object ReviewServiceLive {
  val layer = ZLayer {
    ZIO.service[ReviewRepository].map(repo => new ReviewServiceLive(repo))
  }
}
