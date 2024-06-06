package com.drogovski.reviewboard.services

import collection.mutable
import zio.*
import com.drogovski.reviewboard.domain.data.*
import com.drogovski.reviewboard.http.requests.CreateCompanyRequest
import com.drogovski.reviewboard.repositories.CompanyRepository

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
}

class CompanyServiceLive private (repository: CompanyRepository) extends CompanyService {
  override def create(req: CreateCompanyRequest): Task[Company] =
    repository.create(req.toCompany(-1L))

  override def getAll(): Task[List[Company]] =
    repository.getAll()

  override def getById(id: Long): Task[Option[Company]] =
    repository.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repository.getBySlug(slug)
}

object CompanyServiceLive {
  val layer = ZLayer {
    for {
      repo <- ZIO.service[CompanyRepository]
    } yield new CompanyServiceLive(repo)
  }
}