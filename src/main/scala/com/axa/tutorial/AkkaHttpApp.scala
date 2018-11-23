package com.axa.tutorial

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContext

object AkkaHttpApp {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("akka-http")
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()

    /**
      * Routes
      *
      * {{{
      * type Route = RequestContext ⇒ Future[RouteResult]
      * case class RequestContext(request: HttpRequest, responsePromise: Promise[HttpResponse], retriesLeft: Int) { /* ... */ }
      * sealed trait RouteResult
      * object RouteResult {
      *   final case class Complete(response: HttpResponse) extends /* ... */ RouteResult { /* ... */ }
      *   final case class Rejected(rejections: immutable.Seq[Rejection]) extends /* ... */ RouteResult { /* ... */ }
      *   /* ... */
      * }
      * }}}
      */

    val hello1: Route = complete("Hello World!")
    val rejectedHello1: Route = reject(MissingCookieRejection("username"))

    /**
      * Directives
      *
      * {{{
      * abstract class Directive[L](implicit val ev: Tuple[L]) { /* ... */ }
      * type Directive0 = Directive[Unit]
      * type Directive1[T] = Directive[Tuple1[T]]
      * }}}
      */

      {
        val getMethod: Directive0 = get
        val helloPath: Directive0 = path("hello")
        val nameParameter: Directive1[String] = parameter("name")
        val idParameterAsInt: Directive1[Int] = parameter("id".as[Int])
      }
    /**
      * Generating route from a directive
      * Combining a directive and a function evaluating to a route results in a route
      * Arity of directive directly implies arity of function
      */
    val hello2: Route = {
      val nameParameter: Directive1[String] = parameter("name")

      val route: Route = nameParameter { name =>
        complete(s"Hello $name!")
      }

      route
    }

    /**
      * Combining routes in sub-expression
      */
    val hello3: Route = {
      val getMethod: Directive0 = get
      val helloPath: Directive0 = path("hello")
      val nameParameter: Directive1[String] = parameter("name")

      val route: Route = getMethod {
        helloPath {
          nameParameter { name =>
            complete(s"Hello $name!")
          }
        }
      }

      route
    }

    /**
      * Combining directive  with &
      * Arity of resulting directive is the sum of the arities of combined directives
      */

    val hello4: Route = {
      val getMethod: Directive0 = get
      val helloPath: Directive0 = path("hello")
      val nameParameter: Directive1[String] = parameter("name")

      val directive: Directive1[String] = getMethod & helloPath & nameParameter
      // Arity 0 & Arity 0 & Arity 1 => 0 + 0 + 1 = 1
      val route: Route = directive { name =>
        complete(s"Hello $name!")
      }

      route
    }

    /**
      * Everything can be inlined without changing behaviour (referential transparency)
      */
    val hello5: Route = {
      val route: Route = (get & path("hello") & parameter("name")) { name =>
        complete(s"Hello $name!")
      }

      route
    }

    /**
      * Combining directives alternatively with |
      *
      * - Combined directives should have same arity
      * - Resulting directive will preserve arity
      */

    val sum1: Route = {
      val operandsPath: Directive[(Int, Int)] = path(IntNumber / IntNumber)

      val route: Route = (pathPrefix("sum") & operandsPath) { (a, b) =>
        complete(s"$a + $b = ${a + b}")
      }

      route
    }

    val sum2: Route = {
      val operandsParameters: Directive[(Int, Int)] = parameters("a".as[Int], "b".as[Int])

      val route: Route = (pathPrefix("sum") & operandsParameters) { (a, b) =>
        complete(s"$a + $b = ${a + b}")
      }

      route
    }

    val sum3: Route = {
      val operandsPath: Directive[(Int, Int)] = path(IntNumber / IntNumber)
      val operandsParameters: Directive[(Int, Int)] = parameters("a".as[Int], "b".as[Int])

      val extractOperands: Directive[(Int, Int)] = operandsPath | operandsParameters

      val route: Route = (pathPrefix("sum") & extractOperands) { (a, b) =>
        complete(s"$a + $b = ${a + b}")
      }

      route
    }

    /**
      * Combining routes with ~
      */
    val routes: Route = {
      val hello: Route = (get & path("hello") & parameter("name")) { name =>
        complete(s"Hello $name!")
      }

      val sum: Route = (pathPrefix("sum") & path(IntNumber / IntNumber)) { (a, b) =>
        complete(s"$a + $b = ${a + b}")
      }

      val route: Route =  sum ~ hello

      route
    }

    /**
      * {{{
      * Path Matchers
      * Matches a part of a path
      * abstract class PathMatcher[L](implicit val ev: Tuple[L]) { /* ... */ }
      * type PathMatcher0 = PathMatcher[Unit]
      * type PathMatcher1[T] = PathMatcher[Tuple1[T]]
      * }}}
      */
      {
        val string: PathMatcher0 = "CLI"
        val regex: PathMatcher1[String] = """[A-Z]\d{3}""".r
        val segment: PathMatcher1[String] = Segment
        val intNumber: PathMatcher1[Int] = IntNumber
      }

    /**
      * Combining path matchers sequentially separated by a slash
      * Combining path matchers sequentially
      * Combining path matchers alternatively
      */
    val cliOrCust: PathMatcher0 = {
      val cli: PathMatcher0 = PathMatcher("CLI")
      val cust: PathMatcher0 = "CUST"
      val cliOrCust: PathMatcher0 = cli | cust

      cliOrCust
    }

    val intNumberSlashIntNumber: PathMatcher[(Int, Int)] = {
      val intNumber: PathMatcher1[Int] = IntNumber
      val intNumberSlashIntNumber: PathMatcher[(Int, Int)] = intNumber / intNumber

      intNumberSlashIntNumber
    }

    val customerId: PathMatcher1[Int] = {
      val cliOrCust: PathMatcher0 = "CLI" | "CUST"
      val idNumberPathMatcher: PathMatcher1[Int] = IntNumber
      val customerId: PathMatcher1[Int] = cliOrCust ~ idNumberPathMatcher

      customerId
    }

    val customer1: Route = (pathPrefix("customers") & path(customerId)) { id =>
      complete(s"Customer(id=$id)")
    }

    val examples: Route =
      pathPrefix("hello1") {
        hello1
      } ~
      pathPrefix("hello2") {
        hello2
      } ~
      pathPrefix("hello3") {
        hello3
      } ~
      pathPrefix("hello4") {
        hello4
      } ~
      pathPrefix("hello5") {
        hello5
      } ~
      pathPrefix("sum1") {
        sum1
      } ~
      pathPrefix("sum2") {
        sum2
      } ~
      pathPrefix("sum3") {
        sum3
      } ~
      pathPrefix("customer1") {
        customer1
      }

    val route: Route =
      pathPrefix("examples") {
        examples
      }

    val http = Http()
    http.bindAndHandle(route, "localhost", 8080)
  }
}
