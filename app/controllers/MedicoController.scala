package controllers

import java.util.UUID
import models.{Medico, DatosMedico}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
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
    val respuesta = Medico.listar.map { listaDeMedicos => Ok(views.html.medicos.index(listaDeMedicos)) }
    respuesta
  }

  def add = Action {
    Ok(views.html.medicos.add(medicoForm))
  }

  def insert = Action.async { implicit request =>
    val datosDeMedico: DatosMedico = medicoForm.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val nuevoMedico: Medico = Medico(
      idNueva,
      datosDeMedico.nombre,
      datosDeMedico.apellido,
      datosDeMedico.matricula,
      datosDeMedico.usuario,
      datosDeMedico.password)
    val medicoCreado = Medico.create(nuevoMedico)
    medicoCreado.map { _ => Redirect(routes.MedicoController.list()) }
  }

  def edit(id: Long) = Action.async {
    val medico = Medico.getByID(id)
    val respuesta = medico.map { m =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        val datosMedico = DatosMedico(
          nombre = m.get.nombre,
          apellido = m.get.apellido,
          matricula = m.get.matricula,
          usuario = m.get.usuario,
          password = m.get.password)
        Ok(views.html.medicos.edit(medicoForm.fill(datosMedico), id))
      }
    }
    respuesta
  }

  def update(id: Long) = Action.async { implicit request =>
    val dat: DatosMedico = medicoForm.bindFromRequest.get
    val medico = Medico.getByID(id)

    val respuesta = medico.map { m =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        // pasar esto a la clase del modelo
        val medicoActualizado = m.get.copy(
          nombre = dat.nombre,
          apellido = dat.apellido,
          matricula = dat.matricula,
          usuario = dat.usuario,
          password = dat.password)
        Medico.update(id, medicoActualizado)
        Redirect(routes.MedicoController.list())
      }
    }
    respuesta
  }

  def delete(id: Long) = Action {
    Medico.delete(id)
    Redirect(routes.MedicoController.list())
  }

  def getByID(medicoID: Long) = Action.async { request =>
    val medico = Medico.getByID(medicoID)

    medico.map { m =>
      m.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(Json.toJson(m))
      }
    }
  }

}
