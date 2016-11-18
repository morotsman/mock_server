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
  
  case class CompletedRequest(mockResource : MockResource, timeInMillis: Long)
  case class AgggregateStatistcs()
  case class WatchStatistics()
  case class UnWatchStatistics()
  
  case class MockCreated(mockResource: MockResource)
  case class MockDeleted(mockResource: MockResource)
  
  case class StatisticsEvent(mockResource: MockResource, numberOfRequests: Int)
}



class StatisticsActor extends Actor {
  import StatisticsActor._
  
  context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs) 
  
  var receivedRequestsLastSecond : scala.collection.mutable.Map[MockResource, Int] = scala.collection.mutable.Map()
  
  var observers: Set[ActorRef] = Set()
  
  def receive = {
    case CompletedRequest(mockResource,timeInMillis) => 
      if(receivedRequestsLastSecond.contains(mockResource)){
        receivedRequestsLastSecond(mockResource) = receivedRequestsLastSecond(mockResource) + 1
      } 
    
    case AgggregateStatistcs =>
      context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs)
      observers.foreach { out =>   
        receivedRequestsLastSecond.foreach(s =>
            out ! StatisticsEvent(s._1, s._2) 
        )  
      }
      receivedRequestsLastSecond = receivedRequestsLastSecond.map( v => (v._1 -> 0))
    
    case WatchStatistics =>
      println("New observer!!!") 
      observers = observers + sender()
    
    case UnWatchStatistics => 
      println("Remove observer!!!")
      observers = observers - sender
    
    case MockCreated(mockResource) =>
      receivedRequestsLastSecond = receivedRequestsLastSecond + (mockResource -> 0)
      
    case MockDeleted(mockResource) =>
      receivedRequestsLastSecond = receivedRequestsLastSecond - mockResource
      
    case test@_ =>
      println("Unknown message (StatisticsActor): " + test);
  } 
 
}