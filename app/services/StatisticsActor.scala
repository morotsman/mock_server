package services

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout
import model.MockSpec
import model.MockResource



object StatisticsActor {
  def props = Props[StatisticsActor]
  
  case class CompletedRequest(mockResource : MockResource, timeInMillis: Long)
  case class AgggregateStatistcs()
}



class StatisticsActor extends Actor {
  import StatisticsActor._
  
  context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs) 
  
  var receivedRequestsLastSecond : scala.collection.mutable.Map[MockResource, Int] = scala.collection.mutable.Map()
  
  def receive = {
    case CompletedRequest(mockResource,timeInMillis) => 
      if(receivedRequestsLastSecond.contains(mockResource)){
        receivedRequestsLastSecond(mockResource) = receivedRequestsLastSecond(mockResource) + 1
      } else {
        receivedRequestsLastSecond = receivedRequestsLastSecond + (mockResource -> 1)
      }
    case AgggregateStatistcs =>
      context.system.scheduler.scheduleOnce(1000.millis,self, AgggregateStatistcs) 
      println(receivedRequestsLastSecond)
      receivedRequestsLastSecond = scala.collection.mutable.Map()
  }
 
}