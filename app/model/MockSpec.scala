package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class MockSpec(responseCode: Int, responseTimeMillis: Int, body: String)

//{"method":"PUT","responseCode":201,"responseTimeMillis":1000}
object MockSpec {
  implicit val mockWrites = new Writes[MockSpec] {
    def writes(mock: MockSpec) = Json.obj(
      "responseCode" -> mock.responseCode,
      "responseTimeMillis" -> mock.responseTimeMillis,
      "body" -> mock.body);

  }

  implicit val mockReads: Reads[MockSpec] = (
    (JsPath \ "responseCode").read[Int] and
    (JsPath \ "responseTimeMillis").read[Int] and
    (JsPath \ "body").read[String])(MockSpec.apply _)

}