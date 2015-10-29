package actors

import akka.actor.{Props, Actor, ActorRef}
import play.api.libs.json.JsNumber

// Es el websocket
class RecepcionistaActor(outChannel: ActorRef) extends Actor {

  def receive = {
    case id: JsNumber => // enviar mensaje al actor de recepcion con el id enviado, fijarse como hacerlo
  }
}

object RecepcionistaActor {
  def props(outChannel: ActorRef) = Props(classOf[Reverser], outChannel)

  // mensajes que puede recibir

}