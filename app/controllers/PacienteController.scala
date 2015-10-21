package controllers

import models.Paciente
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class PacienteController extends Controller {

  def listarPacientes = Action.async {
    val pacientesFuture = Paciente.listar

    val respuesta = pacientesFuture.map { p =>
      Ok(Json.toJson(p))
    }

    respuesta
  }

  def getByID(pacienteID: Long) = Action.async { request =>
    val pacienteFuture = Paciente.getByID(pacienteID)

    pacienteFuture.map { paciente =>
      paciente.fold {
        NotFound(Json.toJson(paciente))
      } { e =>
        Ok(Json.toJson(paciente))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Paciente]

    incomingBody.fold(error => {
      Future.successful(BadRequest)
    }, { paciente: Paciente =>
      val createdPacienteFuture = Paciente.create(paciente)

      createdPacienteFuture.map { createdPaciente =>
        Created(Json.toJson(createdPaciente))
      }

    })
  }
}
