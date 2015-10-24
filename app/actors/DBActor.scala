package actors

import actors.DBActor.{QueryByID, Insert}

import scala.concurrent.duration._
import akka.actor.{Props, Actor, ActorRef}
import models.Paciente
import play.api.libs.json.{Json, JsValue}
import scala.concurrent.ExecutionContext.Implicits.global

class DBActor(outChannel: ActorRef) extends Actor {
  val timeout: Duration = 5.seconds

  def receive = {
    case Insert(paciente: Paciente) => Paciente.create(paciente)
    case QueryByID(id: Long) => Paciente.getByID(id)
  }
}

object DBActor {
  def props(outChannel: ActorRef) = Props(classOf[DBActor], outChannel)

  case class Insert(paciente: Paciente)

  case class QueryByID(id: Long)

}
