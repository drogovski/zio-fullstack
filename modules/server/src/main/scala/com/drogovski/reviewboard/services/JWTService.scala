package com.drogovski.reviewboard.services

import zio.*
import com.drogovski.reviewboard.domain.data.*

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}
