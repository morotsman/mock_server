package services

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout
import model.Mock
import model.MockSpec
import model.MockResource



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


  var mocks : Map[MockResource,Mock] = Map()

  var resources : Map[Int,Mock] = Map()

  var index = 0

  def receive = {
    case MockRequest(mockResource) =>
      println(mockResource)
      val mock = mocks.get(mockResource).getOrElse(
        Mock(Option.empty, mockResource,MockSpec(404, 0, "Could not find a specification for the mock.", "text/plain")))
      val timeToWait = mock.mockSpec.responseTimeMillis
      if(timeToWait == 0){
        sender() ! mock
      } else {
        context.system.scheduler.scheduleOnce(timeToWait.millis,sender, mock)
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
    mocks = resources.values.map { mock => (mock.mockResource -> mock)}.toMap
  }

}
