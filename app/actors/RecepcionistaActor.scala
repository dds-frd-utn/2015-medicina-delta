package actors


import java.time.LocalDateTime

import actors.RecepcionActor.EsAtendido
import akka.actor.{Props, Actor, ActorRef}


// Es el websocket
class RecepcionistaActor(outChannel: ActorRef) extends Actor {


  def receive = {
    case "Ah re locoo" => {
      println("Se recibio un mensaje de una recepcion!")
      outChannel ! "Ah re locoo"
    }
    case id: String => context.system.actorSelection("akka://application/user/recepcion" + id + "*") ! EsAtendido(LocalDateTime.now().getHour)

  }
}

object RecepcionistaActor {
  def props(outChannel: ActorRef) = Props(classOf[RecepcionistaActor], outChannel)

}