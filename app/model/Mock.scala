package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Mock(id: Option[Int], mockResource: MockResource, mockSpec: MockSpec)

object Mock {
  implicit val mockWrites = new Writes[Mock] {
    def writes(mock: Mock) = Json.obj(
      "id" -> mock.id,
      "mockResource" -> mock.mockResource,
      "mockSpec" -> mock.mockSpec)

  }

  implicit val mockReads: Reads[Mock] = (
    (JsPath \ "id").readNullable[Int] and
    (JsPath \ "mockResource").read[MockResource] and
    (JsPath \ "mockSpec").read[MockSpec])(Mock.apply _)

}
