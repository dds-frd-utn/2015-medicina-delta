package controllers

import actors.HelloActor.SayHello
import models.Paciente
import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import akka.actor._
import javax.inject._
import actors.DBActor
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import actors._
import play.api.Play.current

@Singleton
class Application @Inject()(system: ActorSystem) extends Controller {
  implicit val timeout: akka.util.Timeout = 5.seconds
  val helloActor = system.actorOf(HelloActor.props, "hello-actor")

  def index = Action {
    Ok(views.html.index.render())
  }



  def sayHello(name: String) = Action.async {
    (helloActor ? SayHello(name)).mapTo[String].map { message =>
      Ok(message)
    }
  }

  def mostrarContador = Action {
    Ok(views.html.websocketCounter())
  }

  val dbActor = system.actorOf(DBActor.props, "db-actor")

  def reverser = WebSocket.acceptWithActor[String,String] { request => out => Reverser.props(out) }

  def sendJSON = WebSocket.acceptWithActor[JsValue, String] { req => out => WebSocketChannel.props(out)}

  val contador = WebSocket.acceptWithActor[String,String] { request => out => WebSocketContador.props(out) }

  def jsonManager = WebSocket.acceptWithActor[String, JsValue] { request => out => JsonManager.props(out, dbActor)}
}