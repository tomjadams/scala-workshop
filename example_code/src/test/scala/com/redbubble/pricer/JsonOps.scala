package com.redbubble.pricer

import io.circe.{Decoder, Error, Json, ParsingFailure}

trait JsonOps {
  final def decodeJson[A](input: String)(implicit decoder: Decoder[A]): Either[Error, A] =
    parseJson(input).flatMap(decoder.decodeJson)

  private def parseJson(input: String): Either[ParsingFailure, Json] = io.circe.jawn.parse(input)
}

object JsonOps extends JsonOps
