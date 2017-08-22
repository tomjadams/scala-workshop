package com.redbubble.pricer.http

import cats.data.NonEmptyList
import com.redbubble.pricer.common.{Pricer, _}
import com.twitter.finagle.Http
import com.twitter.util.Await

final class PricingServer(pricer: Pricer) extends Endpoints(pricer) {
  def start(): Unit = {
    val server = Http.server.serve(":8081", api.toService)
    Await.ready(server)
    ()
  }
}

object HttpApp {
  def main(args: Array[String]): Unit = {
    val pricer = new Pricer(NonEmptyList.of(BaseProduct("hoodie", Map.empty, 0)))
    new PricingServer(pricer).start()
  }
}
