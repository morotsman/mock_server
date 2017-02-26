package services

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout
import model.MockSpec
import model.MockResource



object MockActor {
  def props = Props[MockActor]
  
  case class MockRequest(resource: MockResource)
  case class ListMocks()
  case class GetMock(resource:MockResource)
  case class DeleteMock(resource:MockResource)
  case class AddMock(resource:MockResource, mock: MockSpec)
}



class MockActor extends Actor {
  import MockActor._
  
  
  var mocks : Map[MockResource,MockSpec] = Map()
  
  def receive = {
    case MockRequest(m) =>
      val mock = mocks.get(m).getOrElse(MockSpec(404, 0, "Could not find a specification for the mock."))
      val timeToWait = mock.responseTimeMillis
      context.system.scheduler.scheduleOnce(timeToWait.millis,sender, mock) 
    case ListMocks =>
      println("MockActor: ListMocks")
      sender() ! mocks.keys
    case DeleteMock(m) =>
      println("MockActor: DeleteMock")
      mocks = mocks - m
      sender() ! "OK"
    case AddMock(m,mock) =>
      println("MockActor: AddMock")
      mocks = mocks + (m -> mock) 
      sender() ! mock  
    case GetMock(m) =>
      println("MockActor: GetMock")
      sender() ! mocks.get(m)
  }
 
}