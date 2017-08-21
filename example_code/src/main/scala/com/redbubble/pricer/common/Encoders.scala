package com.redbubble.pricer.common

import io.circe.syntax._
import io.circe.{Encoder, Json}

object Encoders {
  val baseProductEncoder: Encoder[BaseProduct] = Encoder.instance { p =>
    Json.obj(
      "product-type" -> Json.fromString(p.productType),
      "options" -> p.options.asJson,
      "base-price" -> Json.fromInt(p.basePrice)
    )
  }

  // An encoder showing how to manually encode our options map into JSON.
  // Note. It's a convention that the "accumulator" passed into the fold function is called "acc".
  val manualProductOptionsEncoder: Encoder[Map[String, Seq[String]]] =
    Encoder.instance { o =>
      val entries = o.foldLeft(Seq.empty[(String, Json)]) { (acc, kvs) =>
        kvs match {
          case (k, vs) => acc :+ k -> Json.arr(vs.map(Json.fromString): _*)
        }
      }
      Json.obj(entries: _*)
    }
}
