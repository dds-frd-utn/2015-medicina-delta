package actors

import actors.RecepcionesActor.{SubscribirWS, AtencionRecibida}
import actors.WebSocketChannel.Actualizar
import akka.actor.{Props, Actor, ActorRef}


// Es el websocket
class WebSocketChannel(outChannel: ActorRef, recepciones: ActorRef) extends Actor {

  override def preStart = {
    recepciones ! SubscribirWS(self)
  }

  def receive = {
    case id: String => recepciones ! AtencionRecibida(id)
    case Actualizar(id: String) => outChannel ! id
    //case id: String => buscarOCrear(id.toLong) ! EsAtendido(LocalDateTime.now().getHour)

  }
}

object WebSocketChannel {
  def props(outChannel: ActorRef, r: ActorRef) = Props(classOf[WebSocketChannel], outChannel, r)

  case class Actualizar(id: String)

}