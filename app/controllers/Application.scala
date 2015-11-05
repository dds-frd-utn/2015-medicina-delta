package controllers

import play.api.libs.json.JsValue
import play.api.mvc._
import akka.actor._
import javax.inject._
import actors.DBActor
import actors._
import play.api.Play.current

@Singleton
class Application @Inject()(system: ActorSystem) extends Controller {

  def index = Action {
    Ok(views.html.index.render())
  }

  def mostrarContador = Action {
    Ok(views.html.websocketCounter())
  }

  val dbActor = system.actorOf(DBActor.props, "db-actor")

  def sendJSON = WebSocket.acceptWithActor[JsValue, String] { req => out => WebSocketChannel.props(out)}

  val contador = WebSocket.acceptWithActor[String,String] { request => out => WebSocketContador.props(out) }

  def jsonManager = WebSocket.acceptWithActor[String, JsValue] { request => out => JsonManager.props(out, dbActor)}
}