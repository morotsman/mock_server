package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class MockResource(method: String, path: String)

object MockResource {
  implicit val mockWrites = new Writes[MockResource] {
    def writes(mock: MockResource) = Json.obj(
      "method" -> mock.method,
      "path" -> mock.path);

  }

  implicit val mockReads: Reads[MockResource] = (
    (JsPath \ "method").read[String] and
    (JsPath \ "path").read[String])(MockResource.apply _)

}