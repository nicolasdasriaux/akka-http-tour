package akkahttptour

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContext

object JsonHttpApp {
  case class Cart(orderLines: Seq[OrderLine])
  case class OrderLine(item: Item, quantity: Int)
  case class Item(id: Int, name: String)

  object EcommerceProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    implicit lazy val cartFormat: RootJsonFormat[Cart] = jsonFormat1(Cart.apply)
    implicit lazy val orderFormat: RootJsonFormat[OrderLine] = jsonFormat2(OrderLine.apply)
    implicit lazy val itemFormat: RootJsonFormat[Item] = jsonFormat2(Item.apply)
  }

  import EcommerceProtocol._

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("akka-http-tour")
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()

    val cart = Cart(
      orderLines = Seq(
        OrderLine(item = Item(id = 1, name = "Ball"), quantity = 2),
        OrderLine(item = Item(id = 2, name = "Pen"), quantity = 1),
        OrderLine(item = Item(id = 3, name = "Fork"), quantity = 3)
      )
    )

    val getCart = (get & path("cart")) {
      complete(cart)
    }

    val items = Seq(
      OrderLine(item = Item(id = 1, name = "Ball"), quantity = 2),
      OrderLine(item = Item(id = 2, name = "Pen"), quantity = 1),
      OrderLine(item = Item(id = 3, name = "Fork"), quantity = 3)
    )

    val getItems = (get & path("items")) {
      complete(items)
    }

    val postItem = (post & path("items")) {
      entity(as[Item]) { item =>
        complete(s"item=$item")
      }
    }

    val route = getCart ~ getItems ~ postItem

    val http = Http()
    http.bindAndHandle(route, "localhost", 8080)
  }
}
