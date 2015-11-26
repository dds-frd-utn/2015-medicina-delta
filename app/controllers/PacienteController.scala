package controllers

import java.util.UUID

import models.{DatosPaciente, Paciente}
import play.api.data._
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
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
    val respuesta = Paciente.listar.map { listaDePacientes =>
      Ok(views.html.pacientes.index(listaDePacientes))
    }
    respuesta
  }

  def getByID(pacienteID: Long) = Action.async { request =>
    val paciente: Future[Option[Paciente]] = Paciente.getByID(pacienteID)

    paciente.map { p =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(Json.toJson(p))
      }
    }
  }

  def insert = Action.async { implicit request =>
    val datos: DatosPaciente = pacienteForm.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val nuevoPaciente: Paciente = Paciente(idNueva, datos.nombre, datos.apellido, datos.dni, datos.obrasocial)
    val pacienteCreado = Paciente.create(nuevoPaciente)
    pacienteCreado.map { _ => Redirect(routes.PacienteController.list()) }
  }

  def edit(id: Long) = Action.async {
    val paciente = Paciente.getByID(id)
    val respuesta = paciente.map { p =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        val datosPaciente = DatosPaciente(
          nombre = p.get.nombre,
          apellido = p.get.apellido,
          dni = p.get.dni,
          obrasocial = p.get.obraSocial)
        Ok(views.html.pacientes.edit(pacienteForm.fill(datosPaciente), id))
      }
    }
    respuesta
  }

  def update(id: Long) = Action.async { implicit request =>
    val dat: DatosPaciente = pacienteForm.bindFromRequest.get
    val paciente = Paciente.getByID(id)

    val respuesta = paciente.map { p =>
      p.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        val pacienteActualizado = p.get.copy(
          nombre = dat.nombre,
          apellido = dat.apellido,
          dni = dat.dni,
          obraSocial = dat.obrasocial)
        Paciente.update(id, pacienteActualizado)
        Redirect(routes.PacienteController.list())
      }
    }
    respuesta
  }

  def delete(id: Long) = Action {
    Paciente.delete(id)
    Redirect(routes.PacienteController.list())
  }
}
