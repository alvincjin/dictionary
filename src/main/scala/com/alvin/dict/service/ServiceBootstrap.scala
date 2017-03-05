package com.alvin.dict.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Created by alvin.jin on 3/3/2017.
  */
object ServiceBootstrap extends App with Routes {

  implicit val actorSystem = ActorSystem("Dictionary-Service")
  implicit val materializer = ActorMaterializer()

  import actorSystem.dispatcher //ExecutionContext

  createTables(keyspace, table)

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done

}
