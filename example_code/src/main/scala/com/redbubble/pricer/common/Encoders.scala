package com.redbubble.pricer.common

import io.circe.syntax._
import io.circe.{Encoder, Json}

object Encoders {
  private val productOptionsEncoder: Encoder[Map[String, Seq[String]]] =
    Encoder.instance { o =>
      val entries: Map[String, Json] = o.foldLeft(Seq.empty) { (acc, kvs) =>
        // replace this map with a foldleft
        case (k, v: Seq[String]) => k -> Json.arr(v.map(Json.fromString): _*)
      }
      Json.obj()
    }

  val baseProductEncoder: Encoder[BaseProduct] = Encoder.instance { p =>
    Json.obj(
      "product-type" -> Json.fromString(p.productType),
      "options" -> p.options.asJson,
      "basePrice" -> Json.fromInt(p.basePrice)
    )
  }
}
