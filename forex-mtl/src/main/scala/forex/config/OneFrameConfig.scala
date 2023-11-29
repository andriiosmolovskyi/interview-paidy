package forex.config

import org.http4s.Uri

case class OneFrameConfig(baseUri: String, token: String) {
  val uri: Uri = Uri.fromString(baseUri).toOption.get
}
