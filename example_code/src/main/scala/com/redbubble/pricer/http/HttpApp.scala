package com.redbubble.pricer.http

import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._
import io.finch.{Endpoint, _}

object HttpApp {
  val prices: Endpoint[Seq[Int]] = get("prices") {
    Ok(Seq(1, 2, 3, 4, 5))
  }

  def main(args: Array[String]): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
  }
}
