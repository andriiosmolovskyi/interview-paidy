package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }
import io.circe.syntax.{ EncoderOps, KeyOps }
import io.circe.{ Encoder, Json }

object errors {

  sealed trait Error extends Exception {
    val msg: String
  }
  object Error {
    final case class RateNotFound(msg: String) extends Error
    final case class RateLookupFailed(msg: String) extends Error

    implicit def encoder[E <: Error]: Encoder[E] = Encoder.instance { e =>
      Json.obj("msg" := e.msg.asJson)
    }
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupNotFound(msg) => Error.RateNotFound(msg)
    case RatesServiceError.OneFrameLookupFailed(msg)   => Error.RateLookupFailed(msg)
  }
}
