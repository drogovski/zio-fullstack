package com.drogovski.reviewboard.services

import zio.*
import com.drogovski.reviewboard.domain.data.{User, UserToken}
import com.drogovski.reviewboard.repositories.UserRepository
import org.checkerframework.checker.units.qual.kg
import zio.config.magnolia.examples.P
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def generateToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (jwtService: JWTService, userRepo: UserRepository)
    extends UserService {
  override def registerUser(email: String, password: String): Task[User] =
    userRepo.create(
      User(
        id = -1L,
        email = email,
        hashedPassword = Hasher.generateHash(password)
      )
    )
  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"cannot verify user $email: user does not exist."))
      result <- ZIO.attempt(
        Hasher.validateHash(password, existingUser.hashedPassword)
      )
    } yield result

  override def generateToken(email: String, password: String): Task[Option[UserToken]] =
    ZIO.fail(new RuntimeException("not implemented yet"))

}

object UserServiceLive {
  val layer = ZLayer {
    for {
      jwtService     <- ZIO.service[JWTService]
      userRepository <- ZIO.service[UserRepository]
    } yield new UserServiceLive(jwtService, userRepository)
  }
}

object Hasher {
  private val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA512"
  private val PBKDF2_ITERATIONS: Int   = 2137
  private val SALT_BYTE_SIZE: Int      = 24
  private val HASH_BYTE_SIZE: Int      = 24
  private val skf: SecretKeyFactory    = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

  private def pbkdf2(
      message: Array[Char],
      salt: Array[Byte],
      iterations: Int,
      nBytes: Int
  ): Array[Byte] =
    val keySpec: PBEKeySpec = new PBEKeySpec(message, salt, iterations, nBytes * 8)
    skf.generateSecret(keySpec).getEncoded()

  private def toHex(array: Array[Byte]): String =
    array.map(b => "%02X".format(b)).mkString

  private def fromHex(string: String): Array[Byte] = {
    string.sliding(2, 2).toArray.map { hexValue =>
      Integer.parseInt(hexValue, 16).toByte
    }
  }

  private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean = {
    val range = 0 until math.min(a.length, b.length)
    val diff = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
      acc | (a(i) ^ b(i))
    }
    diff == 0
  }

  def generateHash(string: String): String = {
    val rng: SecureRandom = new SecureRandom()
    val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
    rng.nextBytes(salt) // create 24 random bytes
    val hashBytes = pbkdf2(string.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
    s"$PBKDF2_ITERATIONS:${toHex(salt)}:${toHex(hashBytes)}"
  }

  def validateHash(string: String, hash: String): Boolean = {
    val hashSegments = hash.split(":")
    val nIterations  = hashSegments(0).toInt
    val salt         = fromHex(hashSegments(1))
    val validHash    = fromHex(hashSegments(2))
    val testHash     = pbkdf2(string.toCharArray(), salt, nIterations, HASH_BYTE_SIZE)
    compareBytes(validHash, testHash)
  }
}
