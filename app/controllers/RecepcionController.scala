package controllers

import models.Recepcion
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class RecepcionController extends Controller {

  def listarRecepciones = Action.async {
    val recepcionesFuture = Recepcion.listar

    val respuesta = recepcionesFuture.map { r =>
      Ok(Json.toJson(r))
    }

    respuesta
  }

  def getByID(recepcionID: Long) = Action.async { request =>
    val recepcionFuture = Recepcion.getByID(recepcionID)

    recepcionFuture.map { r =>
      r.fold {
        NotFound(Json.toJson(r))
      } { e =>
        Ok(Json.toJson(r))
      }
    }
  }

  def getByMedico(medicoID: Long) = Action.async { request =>
    val recepcionesDelMedicoFuture = Recepcion.getByMedico(medicoID)
    val respuesta = recepcionesDelMedicoFuture map { r =>
      Ok(Json.toJson(r))
    }

    respuesta
  }


  def getByPaciente(pacienteID: Long) = Action.async { request =>
    val recepcionesDelPacienteFuture = Recepcion.getByPaciente(pacienteID)
    val respuesta = recepcionesDelPacienteFuture map { r =>
      Ok(Json.toJson(r))
    }
    respuesta
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Recepcion]

    incomingBody.fold(error => {
      Future.successful(BadRequest)
    }, { recepcion: Recepcion =>
      val createdRecepcionFuture = Recepcion.create(recepcion)

      createdRecepcionFuture.map { createdRecepcion =>
        Created(Json.toJson(createdRecepcion))
      }

    })
  }
}
