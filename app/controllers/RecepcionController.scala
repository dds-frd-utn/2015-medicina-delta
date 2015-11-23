package controllers

import java.time.LocalDateTime
import javax.inject.Inject
import actors.{RecepcionistaActor, RecepcionActor}
import models.{Paciente, Medico, DatosRecepcion, Recepcion}
import play.api.libs.json.Json
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import java.util.UUID
import akka.actor._

class RecepcionController @Inject()(system: ActorSystem) extends Controller {


  private val recepcionForm: Form[DatosRecepcion] = Form(
    mapping(
      "idMedico" -> longNumber,
      "idPaciente" -> longNumber,
      "diagnostico" -> optional(text),
      "prioridad" -> text
    )(DatosRecepcion.apply)(DatosRecepcion.unapply))

  def list = Action.async {
    val recepciones: Future[Seq[Recepcion]] = Recepcion.listar
    println(recepciones.toString)
    recepciones.map { receps: Seq[Recepcion] =>
      receps.foreach { r: Recepcion =>
        val a = system.actorOf(RecepcionActor.props, "recepcion" + r.id.toString)
        val actor: (Long, ActorRef) = (r.id, a)
      }
    }

    val lista: Future[Seq[Future[(Recepcion, Paciente, Medico)]]] = recepciones.map { lst: Seq[Recepcion] =>
      for {
        r <- lst
      } yield Recepcion.recepcionToTriple(r)
    }

    val respuesta = lista.flatMap { listaTruplas =>
      val flip: Future[Seq[(Recepcion, Paciente, Medico)]] = Future.sequence(listaTruplas)
      flip.map { lst =>
        Ok(views.html.recepciones.index(lst))
      }
    }

    respuesta
    /*
    val respuesta = recepciones.map { listaRecepciones =>
      Ok(views.html.recepciones.index(listaRecepciones))
    }
    respuesta

    */
  }

  def add = Action {
    Ok(views.html.recepciones.add(recepcionForm))
  }

  def insert = Action.async { implicit request =>
    val datosDeLaRecepcion: DatosRecepcion = recepcionForm.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val fechaNueva = LocalDateTime.now
    val nuevaRecepcion = Recepcion(
      idNueva,
      datosDeLaRecepcion.idPaciente,
      datosDeLaRecepcion.idMedico,
      fechaNueva,
      datosDeLaRecepcion.diagnostico,
      datosDeLaRecepcion.prioridad
    )

    val recepcionCreada = Recepcion.create(nuevaRecepcion)
    recepcionCreada.map { _ => Redirect(routes.RecepcionController.list()) }
  }

  def getByID(recepcionID: Long) = Action.async { request =>
    val recepcion: Future[Option[Recepcion]] = Recepcion.getByID(recepcionID)

    recepcion.map { r =>
      r.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e => Ok(Json.toJson(r)) }
    }
  }

  def getByMedico(medicoID: Long) = Action.async { request =>
    val recepcionesDelMedico: Future[Seq[Recepcion]] = Recepcion.getByMedico(medicoID)
    val respuesta = recepcionesDelMedico map { r => Ok(Json.toJson(r)) }
    respuesta
  }

  def getByPaciente(pacienteID: Long) = Action.async { request =>
    val recepcionesDelPaciente: Future[Seq[Recepcion]] = Recepcion.getByPaciente(pacienteID)
    val respuesta = recepcionesDelPaciente map { r => Ok(Json.toJson(r)) }
    respuesta
  }

  val recepcionista = WebSocket.acceptWithActor[String, String] { request => out => RecepcionistaActor.props(out) }
}
