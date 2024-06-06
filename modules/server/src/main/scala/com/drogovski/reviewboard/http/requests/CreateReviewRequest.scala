package com.drogovski.reviewboard.http.requests

import com.drogovski.reviewboard.domain.data.Review
import java.time.Instant
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class CreateReviewRequest(
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
) {
  def toReview(id: Long, userId: Long): Review =
    Review(
      id,
      companyId,
      userId,
      management,
      culture,
      salary,
      benefits,
      wouldRecommend,
      review,
      Instant.now(),
      Instant.now()
    )
}

object CreateReviewRequest {
  given codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
}
