package controllers

import javax.inject.{Singleton, Inject}
import actors.{RecepcionesActor, WebSocketChannel}
import models.{DatosRecepcion, Recepcion}
import play.api.libs.json.Json
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import akka.actor._

@Singleton
class RecepcionController @Inject()(system: ActorSystem) extends Controller {

  private val recepcionForm: Form[DatosRecepcion] = Form(
    mapping(
      "idMedico" -> longNumber,
      "idPaciente" -> longNumber,
      "diagnostico" -> optional(text),
      "prioridad" -> text
    )(DatosRecepcion.apply)(DatosRecepcion.unapply))

  def list = Action.async {
    val lista = Recepcion.listar.map { lst: Seq[Recepcion] =>
      for {
        r <- lst
      } yield Recepcion.recepcionToTriple(r)
    }

    lista.flatMap { listaTruplas =>
      Future.sequence(listaTruplas).map { lst =>
        Ok(views.html.recepciones.index(lst))
      }
    }
  }

  def add = Action {
    Ok(views.html.recepciones.add(recepcionForm))
  }

  def insert = Action.async { implicit request =>
    val d: DatosRecepcion = recepcionForm.bindFromRequest.get
    val nuevaRecepcion = Recepcion.fromDatos(d)
    Recepcion.create(nuevaRecepcion).map { _ => Redirect(routes.RecepcionController.list()) }
  }

  def getByID(recepcionID: Long) = Action.async { request =>
    Recepcion.getByID(recepcionID).map { r =>
      r.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e => Ok(Json.toJson(r)) }
    }
  }

  def getByMedico(medicoID: Long) = Action.async { request =>
    Recepcion.getByMedico(medicoID).map { r => Ok(Json.toJson(r)) }
  }

  def getByPaciente(pacienteID: Long) = Action.async { request =>
    Recepcion.getByPaciente(pacienteID).map { r => Ok(Json.toJson(r)) }
  }

  val recepciones = system.actorOf(RecepcionesActor.props)

  val recepcionista = WebSocket.acceptWithActor[String, String] { request => out =>
    WebSocketChannel.props(out, recepciones)}
}
