package controllers

import java.util.UUID
import controllers.responses.{SuccessResponse, ErrorResponse}
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
    val medicos: Future[Seq[Medico]] = Medico.listar

    val respuesta = medicos.map { m =>
      Ok(Json.toJson(SuccessResponse(m)))
    }

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
    medicoCreado.map { _ => Redirect(routes.MedicoController.list) }
  }

  def getByID(medicoID: Long) = Action.async { request =>
    val medico = Medico.getByID(medicoID)

    medico.map { m =>
      m.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(m)))
      }
    }
  }

}
