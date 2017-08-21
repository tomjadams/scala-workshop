package com.redbubble.pricer.common

import io.circe.{Encoder, Json}

object Encoders {
  val productOptionsEncoder: Encoder[Map[String, Seq[String]]] =
    Encoder.instance { o =>
      val entries = o.foldLeft(Seq.empty[(String, Json)]) { (acc, kvs) =>
        kvs match {
          case (k, vs) => acc :+ k -> Json.arr(vs.map(Json.fromString): _*)
        }
      }
      Json.obj(entries: _*)
    }

  val baseProductEncoder: Encoder[BaseProduct] = Encoder.instance { p =>
    Json.obj(
      "product-type" -> Json.fromString(p.productType),
      "options" -> productOptionsEncoder(p.options),
      "basePrice" -> Json.fromInt(p.basePrice)
    )
  }
}
