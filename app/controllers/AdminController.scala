package controllers

import java.util.UUID

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
    adminCreado.map { _ => Redirect(routes.AdminController.list()) }
  }

  def list = Action.async {
    val respuesta = Administrador.listar.map { listaDeAdministradores =>
      Ok(views.html.administradores.index(listaDeAdministradores))
    }
    respuesta
  }

  def getByID(adminID: Long) = Action.async { request =>
    val admin: Future[Option[Administrador]] = Administrador.getByID(adminID)

    admin.map { a =>
      a.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(Json.toJson(a))
      }
    }
  }

  def menu = Action { implicit request =>
    Ok(views.html.administradores.menu.render())
  }

  def edit(id: Long) = Action.async {
    val admin = Administrador.getByID(id)
    val respuesta = admin.map { a =>
      a.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        val datosAdmin = DatosAdmin(
          nombre = a.get.nombre,
          apellido = a.get.apellido,
          usuario = a.get.usuario,
          password = a.get.password)
        Ok(views.html.administradores.edit(adminForm.fill(datosAdmin), id))
      }
    }
    respuesta
  }

  def update(id: Long) = Action.async { implicit request =>
    val dat: DatosAdmin = adminForm.bindFromRequest.get
    val admin = Administrador.getByID(id)

    val respuesta = admin.map { a =>
      a.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        // pasar esto a la clase del modelo
        val adminActualizado = a.get.copy(
          nombre = dat.nombre,
          apellido = dat.apellido,
          usuario = dat.usuario,
          password = dat.password)
        Administrador.update(id, adminActualizado)
        Redirect(routes.AdminController.list())
      }
    }
    respuesta
  }

  def delete(id: Long) = Action {
    Administrador.delete(id)
    Redirect(routes.AdminController.list())
  }
}
