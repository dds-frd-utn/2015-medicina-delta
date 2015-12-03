package controllers

import models.{DatosAdmin, Administrador}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
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
    val datos: DatosAdmin = adminForm.bindFromRequest.get
    val nuevoAdmin = Administrador.fromDatos(datos)
    Administrador.create(nuevoAdmin).map { _ => Redirect(routes.AdminController.list()) }
  }

  def list = Action.async {
    Administrador.listar.map { listaDeAdministradores =>
      Ok(views.html.administradores.index(listaDeAdministradores))
    }
  }

  def getByID(adminID: Long) = Action.async { request =>
    Administrador.getByID(adminID).map { a =>
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
    Administrador.getByID(id).map { a: Option[Administrador] =>
      a.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Ok(views.html.administradores.edit(adminForm.fill(Administrador.toDatos(a.get)), id))
      }
    }
  }

  def update(id: Long) = Action.async { implicit request =>
    val datos: DatosAdmin = adminForm.bindFromRequest.get
    Administrador.getByID(id).map { a: Option[Administrador] =>
      a.fold {
        NotFound(Json.toJson("No encontrado"))
      } { e =>
        Administrador.update(id, a.get, datos)
        Redirect(routes.AdminController.list())
      }
    }
  }

  def delete(id: Long) = Action {
    Administrador.delete(id)
    Redirect(routes.AdminController.list())
  }
}
