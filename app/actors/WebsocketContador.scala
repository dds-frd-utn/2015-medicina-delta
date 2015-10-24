package actors

import akka.actor.{Props, Actor, ActorRef}
import play.api.libs.json.JsValue


class WebSocketContador(outChannel: ActorRef) extends Actor {

  def receive = {
    case n: String => outChannel ! s"${ n.toInt + 1}"
  }
}

object WebSocketContador {
  def props(outChannel: ActorRef) = Props(classOf[WebSocketContador], outChannel)
}