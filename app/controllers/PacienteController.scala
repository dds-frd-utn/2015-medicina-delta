package controllers

import java.util.UUID

import controllers.responses._
import models.{DatosPaciente, Paciente}
import play.api.data._
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._


class PacienteController extends Controller {

  private val formularioPaciente: Form[DatosPaciente] = Form(
    mapping(
      "nombre" -> text,
      "apellido" -> text,
      "dni" -> longNumber,
      "obrasocial" -> number
    )(DatosPaciente.apply)(DatosPaciente.unapply)
  )



  def agregar = Action {
    Ok(views.html.pacientes.nuevoPaciente(formularioPaciente))
  }
  def listarPacientes = Action.async {
    val pacientes: Future[Seq[Paciente]] = Paciente.listar

    val respuesta = pacientes.map { p =>
      Ok(Json.toJson(SuccessResponse(p).response))
    }
    respuesta
  }

  def getByID(pacienteID: Long) = Action.async { request =>
    val paciente: Future[Option[Paciente]] = Paciente.getByID(pacienteID)

    paciente.map { p =>
      p.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(p)))
      }
    }
  }

  def insert = Action.async { implicit request =>
    val datos: DatosPaciente = formularioPaciente.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val nuevoPaciente: Paciente = Paciente(idNueva, datos.nombre,datos.apellido,datos.dni,datos.obrasocial)
    val pacienteCreado = Paciente.create(nuevoPaciente)
    pacienteCreado.map {_ => Redirect(routes.PacienteController.listarPacientes)}
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[Paciente]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: ${error}"
      val response = ErrorResponse(1, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { paciente: Paciente =>
      val createdPaciente: Future[Paciente] = Paciente.create(paciente)

      createdPaciente.map { createdPaciente =>
        Created(Json.toJson(SuccessResponse(createdPaciente)))
      }

    })
  }
}
