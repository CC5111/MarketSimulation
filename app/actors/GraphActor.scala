package actors

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.json.Json

/**
 * Created by Nicolas on 19-08-2016.
 */
object GraphActor{
  def props(out: ActorRef) =
    Props(classOf[GraphActor], out)
}

class GraphActor(out: ActorRef) extends Actor{


  println(self.path)

  def receive = {
    case msg: Double =>
      println("hey ok")
      out! Json.obj("status"-> "OK", "content"-> msg)
    case _=>
      println("hey yo")
  }
}
