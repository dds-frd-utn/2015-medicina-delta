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

  private val formularioPaciente: Form[DatosPaciente] = Form(
    mapping(
      "nombre" -> text,
      "apellido" -> text,
      "dni" -> longNumber,
      "obrasocial" -> number
    )(DatosPaciente.apply)(DatosPaciente.unapply)
  )

  def add = Action {
    Ok(views.html.pacientes.add(formularioPaciente))
  }

  def list = Action.async {
    val pacientes: Future[Seq[Paciente]] = Paciente.listar

    val respuesta = pacientes.map { p =>
      Ok(Json.toJson(p))
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
    val datos: DatosPaciente = formularioPaciente.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val nuevoPaciente: Paciente = Paciente(idNueva, datos.nombre, datos.apellido, datos.dni, datos.obrasocial)
    val pacienteCreado = Paciente.create(nuevoPaciente)
    pacienteCreado.map { _ => Redirect(routes.PacienteController.list()) }
  }

}
