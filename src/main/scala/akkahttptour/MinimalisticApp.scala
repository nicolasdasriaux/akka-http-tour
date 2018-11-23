package akkahttptour

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContext

object MinimalisticApp {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("akka-http-tour")
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()

    val route = (get & path("hello") & parameter("name")) { name =>
      complete(s"Hello $name!")
    }

    val http = Http()
    http.bindAndHandle(route, "localhost", 8080)
  }
}
