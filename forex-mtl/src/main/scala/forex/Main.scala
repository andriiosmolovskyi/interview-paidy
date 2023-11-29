package forex

import cats.arrow.FunctionK
import cats.effect._
import com.comcast.ip4s.{ Host, Port }
import forex.config._
import forex.util.futureToIOMapper
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import scala.concurrent.Future

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val server =
      new Application[IO].build(futureToIOMapper)

    server.use(_ => IO.never).as(ExitCode.Success)
  }

}

class Application[F[_]: Async] {

  def build(mapper: FunctionK[Future, F])(implicit network: Network[F]): Resource[F, Server] = {
    val config = Config.load("app")
    val module = new Module[F](config, mapper)

    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(config.http.host).get)
      .withPort(Port.fromInt(config.http.port).get)
      .withHttpApp(module.httpApp)
      .build
  }

}
