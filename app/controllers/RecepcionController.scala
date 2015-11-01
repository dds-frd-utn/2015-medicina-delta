package controllers

import java.time.LocalDateTime
import controllers.responses.{ErrorResponse, SuccessResponse}
import models.{DatosRecepcion, Recepcion}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import java.util.UUID


class RecepcionController extends Controller {

  private val recepcionForm: Form[DatosRecepcion] = Form(
    mapping(
      "idMedico" -> longNumber,
      "idPaciente" -> longNumber,
      "diagnostico" -> optional(text),
      "prioridad" -> text
    )(DatosRecepcion.apply)(DatosRecepcion.unapply))

  def list = Action.async {
    val recepciones: Future[Seq[Recepcion]] = Recepcion.listar
    val respuesta = recepciones.map { r =>
      Ok(Json.toJson(SuccessResponse(r)))
    }
    respuesta
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
    recepcionCreada.map { _ => Redirect(routes.RecepcionController.list) }
  }

  def getByID(recepcionID: Long) = Action.async { request =>
    val recepcion: Future[Option[Recepcion]] = Recepcion.getByID(recepcionID)

    recepcion.map { r =>
      r.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e => Ok(Json.toJson(SuccessResponse(r))) }
    }
  }

  def getByMedico(medicoID: Long) = Action.async { request =>
    val recepcionesDelMedico: Future[Seq[Recepcion]] = Recepcion.getByMedico(medicoID)
    val respuesta = recepcionesDelMedico map { r => Ok(Json.toJson(r)) }
    respuesta
  }

  def getByPaciente(pacienteID: Long) = Action.async { request =>
    val recepcionesDelPaciente: Future[Seq[Recepcion]] = Recepcion.getByPaciente(pacienteID)
    val respuesta = recepcionesDelPaciente map { r => Ok(Json.toJson(SuccessResponse(r))) }
    respuesta
  }

}
