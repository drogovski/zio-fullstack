package com.drogovski.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object Repository {
  def quillLayer =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  def dataSourceLayer =
    Quill.DataSource.fromPrefix("reviewboard.db")

  def dataLayer =
    dataSourceLayer >>> quillLayer
}
