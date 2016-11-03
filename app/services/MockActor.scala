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
  
  type Name = String
  type Method = String
  
  var mocks : Map[MockResource,MockSpec] = Map()
  var numberOfRequest: Int = 0;
  
  def receive = {
    case MockRequest(m) =>
      val mySender = sender()
      val mock = mocks.get(m).getOrElse(MockSpec(500, 0, "Could not find a specification for the mock."))
      val timeToWait = mock.responseTimeMillis
      println(numberOfRequest)
      numberOfRequest = numberOfRequest + 1
      context.system.scheduler.scheduleOnce(timeToWait.millis) {
        println("Timeout")
        mySender ! mock
      } 
    case ListMocks =>
      sender() ! mocks.keys
    case DeleteMock(m) =>
      mocks = mocks - m
      sender() ! "OK"
    case AddMock(m,mock) =>
      mocks = mocks + (m -> mock) 
      sender() ! mock  
    case GetMock(m) =>
      sender() ! mocks.get(m)
  }
 
}