package forex

import cats.arrow.FunctionK
import cats.effect.IO

import scala.concurrent.Future

package object util {
  lazy val futureToIOMapper: FunctionK[Future, IO] =
    new FunctionK[Future, IO] {
      override def apply[A](fa: Future[A]): IO[A] = IO.fromFuture(IO(fa))
    }
}
