package actors

import actors.DBActor.Agregar
import actors.JsonManager.Lista
import akka.actor.{Props, Actor, ActorRef}
import play.api.libs.json.{Json, JsValue}




class JsonManager(outChannel: ActorRef, dBActor: ActorRef) extends Actor {

  def receive = {
    case json: String => dBActor ! Agregar(json)
    case Lista(json: JsValue) => {
      println("JSONManager: Recibi json y se lo envio al canal de salida!")
      outChannel ! json
    }
  }
}

object JsonManager {
  def props(outChannel: ActorRef, dBActor: ActorRef) = Props(classOf[JsonManager], outChannel, dBActor)

  case class Lista(json: JsValue)
}
