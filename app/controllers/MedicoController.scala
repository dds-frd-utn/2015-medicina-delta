package controllers

import models.{Medico, DatosMedico}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class MedicoController extends Controller {

  private val medicoForm: Form[DatosMedico] = Form(
    mapping(
      "nombre" -> text,
      "apellido" -> text,
      "matricula" -> longNumber,
      "usuario" -> text,
      "password" -> text
    )(DatosMedico.apply)(DatosMedico.unapply))

  def list = Action.async {
    Medico.listar.map { listaDeMedicos => Ok(views.html.medicos.index(listaDeMedicos)) }
  }

  def add = Action {
    Ok(views.html.medicos.add(medicoForm))
  }

  def insert = Action.async { implicit request =>
    val datos: DatosMedico = medicoForm.bindFromRequest.get
    val nuevoMedico: Medico = Medico.fromDatos(datos)

    Medico.create(nuevoMedico).map { _ => Redirect(routes.MedicoController.list()) }
  }

  def edit(id: Long) = Action.async {
    Medico.getByID(id).map { m: Option[Medico] =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(views.html.medicos.edit(medicoForm.fill(Medico.toDatos(m.get)), id))
      }
    }

  }

  def update(id: Long) = Action.async { implicit request =>
    val dat: DatosMedico = medicoForm.bindFromRequest.get
    Medico.getByID(id).map { m =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Medico.update(id, m.get, dat)
        Redirect(routes.MedicoController.list())
      }
    }

  }

  def delete(id: Long) = Action {
    Medico.delete(id)
    Redirect(routes.MedicoController.list())
  }

  def getByID(medicoID: Long) = Action.async { request =>
    Medico.getByID(medicoID).map { m =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e => Ok(Json.toJson(m)) }
    }
  }
}
