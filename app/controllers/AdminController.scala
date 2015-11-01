package controllers

import java.util.UUID

import controllers.responses.{ErrorResponse, SuccessResponse}
import models.{DatosAdmin, Administrador}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class AdminController extends Controller {

  private val adminForm: Form[DatosAdmin] = Form(
    mapping(
      "nombre" -> text,
      "apellido" -> text,
      "usuario" -> text,
      "password" -> text
    )(DatosAdmin.apply)(DatosAdmin.unapply))

  def add = Action {
    Ok(views.html.administradores.add(adminForm))
  }

  def insert = Action.async { implicit request =>
    val datosDeAdmin: DatosAdmin = adminForm.bindFromRequest.get
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    val nuevoAdmin = Administrador(
      idNueva,
      datosDeAdmin.nombre,
      datosDeAdmin.apellido,
      datosDeAdmin.usuario,
      datosDeAdmin.password)
    val adminCreado = Administrador.create(nuevoAdmin)
    adminCreado.map { _ => Redirect(routes.AdminController.list) }
  }

  def list = Action.async {
    val administradores: Future[Seq[Administrador]] = Administrador.listar
    val respuesta = administradores.map { a =>
      Ok(Json.toJson(SuccessResponse(a)))
    }
    respuesta
  }

  def getByID(adminID: Long) = Action.async { request =>
    val admin: Future[Option[Administrador]] = Administrador.getByID(adminID)

    admin.map { a =>
      a.fold {
        NotFound(Json.toJson(ErrorResponse(2, "No encontrado")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(a)))
      }
    }
  }

}
