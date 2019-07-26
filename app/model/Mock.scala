package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Mock(id: Option[Int], matchType: String, matcher: Matcher, mockSpec: MockSpec)

object Mock {
  implicit val mockWrites = new Writes[Mock] {
    def writes(mock: Mock) = Json.obj(
      "id" -> mock.id,
      "matchType" -> mock.matchType,
      "matcher" -> mock.matcher,
      "mockSpec" -> mock.mockSpec)

  }

  implicit val mockReads: Reads[Mock] = (
    (JsPath \ "id").readNullable[Int] and
    (JsPath \ "matchType").read[String] and
    (JsPath \ "matcher").read[Matcher] and
    (JsPath \ "mockSpec").read[MockSpec])(Mock.apply _)

}
