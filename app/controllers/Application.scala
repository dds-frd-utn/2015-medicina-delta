package controllers

import actors.HelloActor.SayHello
import models.Paciente
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import akka.actor._
import javax.inject._

import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import actors.HelloActor

@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {
  implicit val timeout: akka.util.Timeout = 5.seconds
  val helloActor = system.actorOf(HelloActor.props, "hello-actor")



  def sayHello(name: String) = Action.async {
    (helloActor ? SayHello(name)).mapTo[String].map { message =>
      Ok(message)
    }
  }




}