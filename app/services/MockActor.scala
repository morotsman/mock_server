package services

import akka.actor._

import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout
import model.{Matcher, Mock, MockResource, MockSpec}
import play.libs.F.Tuple



object MockActor {
  def props = Props[MockActor]

  case class MockRequest(resource: MockResource)
  case class ListMocks()
  case class GetMock(id:Int)
  case class DeleteMock(id: Int)
  case class AddMock(resource: Mock)
  case class UpdateMock(id: Int, resource: Mock)
}



class MockActor extends Actor {
  import MockActor._

  var ignoreBodyMatches : Map[MockResource, MockSpec] = Map()
  var exactBodyMatches : Map[Matcher,MockSpec] = Map()
  var regExpBodyMatches: Map[MockResource, MockSpec] = Map()

  var resources : Map[Int,Mock] = Map()

  var index = 0

  def receive: PartialFunction[Any,Unit] = {
    case matcher @ Matcher(mockResource, responeBody) =>
      println("Received call to resource:  " + mockResource + "with response body: " + responeBody)
      val mockSpec: Option[MockSpec] = exactBodyMatches.get(matcher)
        .orElse(regExpBodyMatches.get(matcher.resource))
        .orElse(ignoreBodyMatches.get(matcher.resource))
        .orElse(Option(MockSpec(404, 0, "Could not find a specification for the mock.", "text/plain")))

      mockSpec.foreach { spec =>
        val timeToWait = spec.responseTimeMillis
        if(timeToWait == 0){
          sender() ! spec
        } else {
          context.system.scheduler.scheduleOnce(timeToWait.millis,sender, spec)
        }
      }
    case ListMocks =>
      println("ListMocks: " + resources.values)
      sender() ! resources.values.toList
    case DeleteMock(id) =>
      resources = resources - id
      println("DeleteMock: " + id)
      updateMocks()
      sender() ! "OK"
    case AddMock(mock) =>
      val newMock = mock.copy(id = Option(index))
      resources = resources + (index -> newMock)
      index = index + 1
      println("AddMock: " + newMock)
      updateMocks()
      sender() ! newMock
    case UpdateMock(id, mock) =>
      val newMock = mock.copy(id = Option(id))
      resources = resources + (id -> newMock)
      println("UpdateMock: " + newMock)
      updateMocks()
      sender() ! mock
    case GetMock(mockResource) =>
      println("MockActor: GetMock")
      sender() ! resources.get(mockResource)
  }

  def updateMocks(): Unit = {
    exactBodyMatches = resources.values
      .filter{ mock => mock.matchType.contains("exact")}
      .map { mock => mock.matcher -> mock.mockSpec}
      .toMap

    regExpBodyMatches = resources.values
      .filter{ mock => mock.matchType.contains("regexp")}
      .map { mock => mock.matcher.resource -> mock.mockSpec}
      .toMap

    ignoreBodyMatches = resources.values
      .filter{ mock => mock.matchType.contains("ignore")}
      .map { mock => mock.matcher.resource -> mock.mockSpec}
      .toMap

    println(exactBodyMatches)
    println(regExpBodyMatches)
    println(ignoreBodyMatches)
  }

}
