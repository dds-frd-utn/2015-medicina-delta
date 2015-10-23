package actors

import akka.actor.{Props, Actor, ActorRef}


class Reverser(outChannel: ActorRef) extends Actor {

  def receive = {
    case s: String => outChannel ! s.reverse
  }
}

object Reverser {
  def props(outChannel: ActorRef) = Props(classOf[Reverser], outChannel)
}
