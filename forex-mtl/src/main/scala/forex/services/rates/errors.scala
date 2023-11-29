package forex.services.rates

import cats.implicits.toShow
import forex.domain.Pair
import org.http4s.Status

object errors {

  sealed trait Error extends Exception {
    val msg: String

    override def getMessage: String = msg
  }
  object Error {
    final case class OneFrameLookupNotFound private (msg: String) extends Error
    final case class OneFrameLookupFailed private (msg: String) extends Error

    def notFound(pair: Pair): Error = OneFrameLookupNotFound(
      s"Not found rates from OneFrame for pair ${pair.show}"
    )

    def unexpected(pair: Pair, status: Status, body: String): Error = OneFrameLookupFailed(
      s"Error during getting rates from OneFrame for pair ${pair.show}, status is: ${status.show}, response is: $body"
    )
  }

}
