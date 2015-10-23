package controllers

import controllers.responses.{ErrorResponse, SuccessResponse}
import models.Recepcion
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class RecepcionController extends Controller {

  def listarRecepciones = Action.async {
    val recepciones: Future[Seq[Recepcion]] = Recepcion.listar
    val respuesta = recepciones.map { r =>
      Ok(Json.toJson(SuccessResponse(r)))
    }

    respuesta
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

  def create = Action.async(parse.json) { request =>
    val incomingBody: JsResult[Recepcion] = request.body.validate[Recepcion]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(1, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { r: Recepcion =>
      val createdRecepcion: Future[Recepcion] = Recepcion.create(r)

      createdRecepcion.map { createdRecepcion =>
        Created(Json.toJson(SuccessResponse(createdRecepcion)))
      }
    })
  }
}
