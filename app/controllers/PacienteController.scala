package controllers

import models.{DatosPaciente, Paciente}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class PacienteController extends Controller {

  private val pacienteForm: Form[DatosPaciente] = Form(
    mapping(
      "nombre" -> text,
      "apellido" -> text,
      "dni" -> longNumber,
      "obrasocial" -> number
    )(DatosPaciente.apply)(DatosPaciente.unapply)
  )

  def add = Action {
    Ok(views.html.pacientes.add(pacienteForm))
  }

  def list = Action.async {
    Paciente.listar.map { listaDePacientes =>
      Ok(views.html.pacientes.index(listaDePacientes))
    }
  }

  def getByID(pacienteID: Long) = Action.async { request =>
    Paciente.getByID(pacienteID).map { p =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(Json.toJson(p))
      }
    }
  }

  def insert = Action.async { implicit request =>
    val d: DatosPaciente = pacienteForm.bindFromRequest.get
    val nuevoPaciente = Paciente.fromDatos(d)
    Paciente.create(nuevoPaciente).map { _ => Redirect(routes.PacienteController.list()) }
  }

  def edit(id: Long) = Action.async {
    Paciente.getByID(id).map { p: Option[Paciente] =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(views.html.pacientes.edit(pacienteForm.fill(Paciente.toDatos(p.get)), id))
      }
    }
  }

  def update(id: Long) = Action.async { implicit request =>
    val d: DatosPaciente = pacienteForm.bindFromRequest.get
    Paciente.getByID(id).map { p: Option[Paciente] =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Paciente.update(id, p.get, d)
        Redirect(routes.PacienteController.list())
      }
    }
  }

  def delete(id: Long) = Action {
    Paciente.delete(id)
    Redirect(routes.PacienteController.list())
  }
}
