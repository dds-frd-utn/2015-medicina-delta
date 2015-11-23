package actors


import actors.RecepcionActor.{atencionFinalizada, EsDiagnosticado, EsAtendido}
import akka.actor._

class RecepcionActor extends Actor {

  import context._

  def atendido: Receive = {
    case EsAtendido(tiempo: Long) => // hacer algo
    case EsDiagnosticado(id: Long, diag: String) => // hacer algo
    case atencionFinalizada(id: Long) =>
  }

  def receive = {
    case "sarasa" => {
      sender ! "Ah re locoo"
    }
    case EsAtendido(tiempo: Long) => {
      become(atendido)
    } // hacer algo mas, usando el tiempo
    case EsDiagnosticado(id: Long, diag: String) => sender() ! "Error"
    case atencionFinalizada(id: Long) =>
  }
}

object RecepcionActor {
  def props = Props(classOf[RecepcionActor])

  // Se puede guardar el horario de atencion como Long y luego pasarlo
  case class EsAtendido(tiempo: Long)

  // recibe el diagnostico enviado por el medico
  case class EsDiagnosticado(id: Long, diag: String)

  //finaliza el actor
  case class atencionFinalizada(id: Long)

}