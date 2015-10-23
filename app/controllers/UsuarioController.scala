package controllers

import controllers.responses.{SuccessResponse, ErrorResponse}
import models.{Medico, Administrador}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class UsuarioController extends Controller {

  def listarMedicos = Action.async {
    val medicos: Future[Seq[Medico]] = Medico.listar

    val respuesta = medicos.map { m =>
      Ok(Json.toJson(SuccessResponse(m)))
    }

    respuesta
  }

  def getMedicoByID(medicoID: Long) = Action.async { request =>
    val medico = Medico.getByID(medicoID)

    medico.map { m =>
      m.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(m)))
      }
    }
  }

  def createMedico = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Medico]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(1, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { medico: Medico =>
      val createdMedico: Future[Medico] = Medico.create(medico)

      createdMedico.map { createdMedico =>
        Created(Json.toJson(SuccessResponse(createdMedico)))
      }

    })
  }

  def listarAdministradores = Action.async {
    val administradores: Future[Seq[Administrador]] = Administrador.listar

    val respuesta = administradores.map { a =>
      Ok(Json.toJson(SuccessResponse(a)))
    }

    respuesta
  }

  def getAdminByID(adminID: Long) = Action.async { request =>
    val admin: Future[Option[Administrador]] = Administrador.getByID(adminID)

    admin.map { a =>
      a.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(a)))
      }
    }
  }

  def createAdmin = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Administrador]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(1, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { administrador: Administrador =>
      val createdAdmin = Administrador.create(administrador)

      createdAdmin.map { createdAdmin =>
        Created(Json.toJson(createdAdmin))
      }

    })
  }

}
