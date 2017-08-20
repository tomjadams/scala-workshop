package com.redbubble.pricer.http

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import io.finch.{Endpoint, _}

object HttpApp {
  val prices: Endpoint[Seq[Int]] = get("prices") {
      Ok(Seq.empty[Int])
    }

  def main(args: Array[String]): Unit = {
    val server: ListeningServer = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
  }
}
