package controllers

import play.api.data._
import play.api.mvc.{Security, Action, Controller}
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class Auth extends Controller {

  val loginForm = Form(
    tuple(
      "usuario" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (usuario, password) => check(usuario, password)
    })
  )

  def check(username: String, password: String) = {
    username == "admin" && password == "1234"
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Application.index()).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.login()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}
