package com.redbubble.pricer.http

import com.redbubble.pricer.common.BaseProduct
import com.redbubble.pricer.common.Encoders.baseProductsEncoder
import com.redbubble.pricer.http.ResponseEncoders.dataJsonEncode
import com.twitter.finagle.Http
import com.twitter.io.Buf
import com.twitter.util.Await
import io.circe.{Encoder, Json, Printer}
import io.finch.{Endpoint, _}

object ResponseEncoders {
  private val printer: Printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}

object HttpApp {
  val prices: Endpoint[Seq[BaseProduct]] = get("prices") {
    Ok(Seq(BaseProduct("hoodie", Map.empty, 0)))
  }
  private implicit val productsResponseEncode = dataJsonEncode(baseProductsEncoder)

  def main(args: Array[String]): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
  }
}
