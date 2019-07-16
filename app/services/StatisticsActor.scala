package services

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout
import model.MockSpec
import model.MockResource
import play.api.libs.json._



object StatisticsActor {
  def props = Props[StatisticsActor]

  case class IncomingRequest(mockResource: MockResource)
  case class CompletedRequest(mockResource : MockResource, timeInMillis: Long)
  case class AgggregateStatistcs()
  case class WatchStatistics()
  case class UnWatchStatistics()

  case class MockCreated(mockResource: MockResource)
  case class MockDeleted(mockResource: MockResource)

  case class StatisticsEvent(mockResource: MockResource, numberOfRequests: Int, eventType: String)
}



class StatisticsActor extends Actor {
  import StatisticsActor._

  context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs)

  var completedRequestsLastSecond : scala.collection.mutable.Map[MockResource, Int] = scala.collection.mutable.Map()
  var receivedRequestsLastSecond : scala.collection.mutable.Map[MockResource, Int] = scala.collection.mutable.Map()

  var observers: Set[ActorRef] = Set()

  def receive = {
    case CompletedRequest(mockResource,timeInMillis) =>
      if(completedRequestsLastSecond.contains(mockResource)){
        println(timeInMillis)
        completedRequestsLastSecond(mockResource) = completedRequestsLastSecond(mockResource) + 1
      }

    case IncomingRequest(mockResource) =>
      if(receivedRequestsLastSecond.contains(mockResource)){
        receivedRequestsLastSecond(mockResource) = receivedRequestsLastSecond(mockResource) + 1
      }
    case AgggregateStatistcs =>
      context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs)
      observers.foreach { out =>
        completedRequestsLastSecond.foreach(s =>
            out ! StatisticsEvent(s._1, s._2, "completed")
        )
        receivedRequestsLastSecond.foreach(s =>
            out ! StatisticsEvent(s._1, s._2, "incoming")
        )
      }
      completedRequestsLastSecond = completedRequestsLastSecond.map( v => (v._1 -> 0))
      receivedRequestsLastSecond = receivedRequestsLastSecond.map( v => (v._1 -> 0))

    case WatchStatistics =>
      println("New observer!!!")
      observers = observers + sender()

    case UnWatchStatistics =>
      println("Remove observer!!!")
      observers = observers - sender

    case MockCreated(mockResource) =>
      completedRequestsLastSecond = completedRequestsLastSecond + (mockResource -> 0)
      receivedRequestsLastSecond = receivedRequestsLastSecond + (mockResource -> 0)

    case MockDeleted(mockResource) =>
      completedRequestsLastSecond = completedRequestsLastSecond - mockResource
      receivedRequestsLastSecond = receivedRequestsLastSecond - mockResource

    case test@_ =>
      println("Unknown message (StatisticsActor): " + test);
  }

}
