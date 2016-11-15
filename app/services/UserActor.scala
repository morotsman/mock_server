package services

import javax.inject._

import akka.actor._
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json._
import services.StatisticsActor.WatchStatistics
import services.StatisticsActor.StatisticsEvent

class UserActor @Inject()(@Assisted out: ActorRef,
                          @Named("statisticsActor") statisticsActor: ActorRef,
                          configuration: Configuration) extends Actor with ActorLogging {


  override def preStart(): Unit = {
    super.preStart()

    statisticsActor ! WatchStatistics
  }



  override def receive: Receive = LoggingReceive {

    case json: JsValue =>
      //val symbol = (json \ "symbol").as[String]
      //statisticsActor ! WatchStock(symbol)
      
    case StatisticsEvent(resource,numberOfRequests) =>
      val statisticsEvent = Json.obj("type" -> "statisticsEvent", "resource" -> resource, "numberOfRequestsPerSecond" -> numberOfRequests)
      out ! statisticsEvent
    case unknown@_ => 
      println("Unknown message received by UserActor: " + unknown)
  }
}

class UserParentActor @Inject()(childFactory: UserActor.Factory) extends Actor with InjectedActorSupport with ActorLogging {
  import UserParentActor._

  override def receive: Receive = LoggingReceive {
    case Create(id, out) =>
      println("Creating user actor!!!!!");
      val child: ActorRef = injectedChild(childFactory(out), s"userActor-$id")
      sender() ! child
  }
}

object UserParentActor {
  case class Create(id: String, out: ActorRef)
}

object UserActor {
  trait Factory {
    // Corresponds to the @Assisted parameters defined in the constructor
    def apply(out: ActorRef): Actor
  }
}
