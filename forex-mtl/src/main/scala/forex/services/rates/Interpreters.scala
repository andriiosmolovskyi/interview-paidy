package forex.services.rates

import cats.Applicative
import cats.arrow.FunctionK
import cats.effect.Resource
import cats.effect.kernel.Concurrent
import forex.config.OneFrameConfig
import forex.services.rates.interpreters._
import org.http4s.client.Client

import scala.concurrent.Future

object Interpreters {
  def http[F[_]: Concurrent](client: Resource[F, Client[F]], oneFrameConfig: OneFrameConfig): Algebra[F] =
    new OneFrameHttp[F](client, oneFrameConfig)

  def cached[F[_]: Applicative](decorated: Algebra[F],
                         mapper: FunctionK[Future, F]): Algebra[F] =
    new OneFrameCacheDecorator[F](decorated, mapper)
}
