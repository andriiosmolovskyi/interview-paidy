package forex

import cats.arrow.FunctionK
import cats.effect.Async
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import fs2.io.net.Network
import org.http4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.server.middleware.{ AutoSlash, Timeout }

import scala.concurrent.Future

class Module[F[_]: Async](config: ApplicationConfig, mapper: FunctionK[Future, F])(implicit network: Network[F]) {

  private val clientResource = EmberClientBuilder.default[F].build

  private val httpRatesService: RatesService[F]   = RatesServices.http[F](clientResource, config.oneFrame)
  private val cachedRatesService: RatesService[F] = RatesServices.cached[F](httpRatesService, mapper)
  private val ratesProgram: RatesProgram[F]       = RatesProgram[F](cachedRatesService)
  private val ratesHttpRoutes: HttpRoutes[F]      = new RatesHttpRoutes[F](ratesProgram).routes

  private type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  private type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
