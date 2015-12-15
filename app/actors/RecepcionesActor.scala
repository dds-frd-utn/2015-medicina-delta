package actors

import java.time.LocalDateTime

import actors.RecepcionActor.EsAtendido
import actors.RecepcionesActor.{SubscribirWS, AtencionRecibida}
import actors.WebSocketChannel.Actualizar
import akka.actor._

class RecepcionesActor extends Actor {

  var recepciones = Map.empty[Long, ActorRef]
  var websockets = Set.empty[ActorRef]

  def buscarOCrear(id: Long): ActorRef = {
    recepciones.getOrElse(id, {
      val actor = context.actorOf(RecepcionActor.props, "recepcion" + id)
      recepciones += id -> actor
      println("Cantidad recepciones " + recepciones.size)
      actor
    })

  }
  def receive = {
    case AtencionRecibida(id: String) => {
      println("Cantidad de Websockets: " + websockets.size)
      buscarOCrear(id.toLong) !  EsAtendido(LocalDateTime.now().getHour)
      websockets.foreach { ws: ActorRef => ws ! Actualizar(id) }
      //system.eventStream.publish(Electronic("Parov Stelar"))
    }
    case SubscribirWS(a: ActorRef) => {
      println("Se ha subscripto " + a )
      websockets += a
      // system.eventStream.subscribe(jazzListener, classOf[Jazz])
    }
  }
}

object RecepcionesActor {
  def props = Props(classOf[RecepcionesActor])

  case class AtencionRecibida(id: String)
  case class SubscribirWS(a: ActorRef)
}