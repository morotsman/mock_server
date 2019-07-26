package model

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class Matcher(resource: MockResource, body: Option[String])

object Matcher {
  implicit val writes = new Writes[Matcher] {
    def writes(matcher: Matcher) = Json.obj(
      "resource" -> matcher.resource,
      "body" -> matcher.body);

  }

  implicit val reads: Reads[Matcher] = (
    (JsPath \ "resource").read[MockResource] and
      (JsPath \ "body").readNullable[String]
    )(Matcher.apply _)

}


