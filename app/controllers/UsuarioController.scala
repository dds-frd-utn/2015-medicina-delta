package controllers

import models.{Medico, Administrador}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class UsuarioController extends Controller {

  def listarMedicos = Action.async {
    val medicosFuture = Medico.listar

    val respuesta = medicosFuture.map { m =>
      Ok(Json.toJson(m))
    }

    respuesta
  }

  def getMedicoByID(medicoID: Long) = Action.async { request =>
    val medicoFuture = Medico.getByID(medicoID)

    medicoFuture.map { medico =>
      medico.fold {
        NotFound(Json.toJson(medico))
      } { e =>
        Ok(Json.toJson(medico))
      }
    }
  }

  def createMedico = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Medico]

    incomingBody.fold(error => {
      Future.successful(BadRequest)
    }, { medico: Medico =>
      val createdMedicoFuture = Medico.create(medico)

      createdMedicoFuture.map { createdMedico =>
        Created(Json.toJson(createdMedico))
      }

    })
  }

  def listarAdministradores = Action.async {
    val adminFuture = Administrador.listar

    val respuesta = adminFuture.map { a =>
      Ok(Json.toJson(a))
    }

    respuesta
  }

  def getAdminByID(adminID: Long) = Action.async { request =>
    val adminFuture = Administrador.getByID(adminID)

    adminFuture.map { admin =>
      admin.fold {
        NotFound(Json.toJson(admin))
      } { e =>
        Ok(Json.toJson(admin))
      }
    }
  }

  def createAdmin = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Administrador]

    incomingBody.fold(error => {
      Future.successful(BadRequest)
    }, { administrador: Administrador =>
      val createdAdminFuture = Administrador.create(administrador)

      createdAdminFuture.map { createdAdmin =>
        Created(Json.toJson(createdAdmin))
      }

    })
  }

}
