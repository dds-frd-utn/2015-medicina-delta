package actors

import actors.DBActor.Agregar
import actors.JsonManager.Lista

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{Props, Actor, ActorRef}
import models.Paciente
import play.api.libs.json.{Json, JsValue}
import scala.concurrent.ExecutionContext.Implicits.global

class DBActor extends Actor {
  val timeout: Duration = 5.seconds

  def receive = {

    case Agregar(json: String) => {
      println("DBactor: Recibi json")
      val jsonToPaciente = Json.parse(json).validate[Paciente]
      Paciente.create(jsonToPaciente.get)
      val lista = Paciente.listar.map { xs => sender() ! Lista(Json.toJson(xs)) }
    }
  }
}

object DBActor {
  def props = Props[DBActor]

  case class Agregar(json: String)

}
