package actors

import akka.actor.{Props, Actor, ActorRef}
import play.api.libs.json.JsValue


class WebSocketChannel(outChannel: ActorRef) extends Actor {

  def receive = {
    case json: JsValue => outChannel ! s"Se ha recibido ${json.toString}"
  }
}

object WebSocketChannel {
  def props(outChannel: ActorRef) = Props(classOf[WebSocketChannel], outChannel)
}
