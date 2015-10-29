package actors


import actors.RecepcionActor.{EsDiagnosticado, EsAtendido}
import akka.actor._

class RecepcionActor extends Actor {

  import context._

  def atendido: Receive = {
    case EsAtendido(tiempo: Long) => // hacer algo
    case EsDiagnosticado(diag: String) => // hacer algo
  }

  def receive = {
    case EsAtendido(tiempo: Long) => become(atendido) // hacer algo mas, usando el tiempo
    case EsDiagnosticado(diag: String) => // hacer algo
  }
}

object RecepcionActor {
  def props = Props(classOf[RecepcionActor])

  // Se puede guardar el horario de atencion como Long y luego pasarlo
  case class EsAtendido(tiempo: Long)

  // recibe el diagnostico enviado por el medico
  case class EsDiagnosticado(diag: String)


}