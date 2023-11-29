package forex.services.rates.interpreters

import cats.Applicative
import cats.arrow.FunctionK
import cats.implicits.{ catsSyntaxEitherId, toFunctorOps, toShow }
import com.github.blemale.scaffeine.Scaffeine
import forex.domain.{ Pair, Rate }
import forex.services.rates.{ errors, Algebra }
import forex.util.Logging

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class OneFrameCacheDecorator[F[_]: Applicative](decorated: Algebra[F], mapper: FunctionK[Future, F])
    extends Algebra[F]
    with Logging {

  // According to functional requirements max TTL for the currency rate is 5 minutes
  // Because of this cache used to decrease latency and avoid unnecessary load of OneFrame
  private val ratesCache = Scaffeine()
    .expireAfterWrite(5.minutes)
    // The key is a pair of Currencies, for now 9 currencies were defined,
    // max size of cache is amount of combinations from 9 by 2 = 72, so 1000 is more then enough
    // Can be increased for future needs
    .maximumSize(1000)
    .buildAsync[Pair, Rate]()

  override def get(request: Pair): F[Either[errors.Error, Rate]] =
    ratesCache.getIfPresent(request) match {
      case Some(future) => mapper(future).map(_.asRight[errors.Error])
      case None =>
        decorated.get(request).map {
          case result @ Right(rate) =>
            logger.info(s"Adding new value to the cache: ${rate.show}")
            ratesCache.put(rate.pair, Future.successful(rate))

            result
          case result @ Left(e) =>
            logger.error(e)(s"Cannot put new value to the cache")

            result
        }
    }

}
