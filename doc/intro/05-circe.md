# Circe

Next, we're going to want to parse our JSON files. We'll walk through parsing the first one, then, we'll let you do the second by yourself.

We're going to use [Circe](https://circe.github.io/circe/) to parse the JSON. Let's add a dependency into our dependencies file:

```scala
import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test

  val circeVersion = "0.8.0"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
}
```

And our build file:

```scala
libraryDependencies ++= Seq(
  circeCore,
  circeGeneric,
  circeParser,
  scalaTest,
  scalaCheck
)
```

OK, let's look at our cart first. The structure looks like:

```json
[
  {
    "product-type": "hoodie",
    "options": {
      "print-location": "front",
      "colour": "white",
      "size": "small"
    },
    "artist-markup": 20,
    "quantity": 0
  }
]
```

So a cart is composed of a list of items. Each item contains a product type, a map of options (key/value pairs), an artist markup and a quantity.

The first thing that we want to do is to model the structure. Then, we'll parse (decode) the JSON into the classes we've just built. This process of parsing JSON into domain models is called decoding. When we go the other way - from classes to JSON - is called encoding. The decoder/encoder pair is called often called a codec.

In general, we want our classes to model the structure of the JSON we're decoding. However, we may wish to clean up the structure in our decoders to something more sane, if we're dealing with JSON from data sources we have no control over.

So, our classes will look something like this.

```scala
final case class CartItem(
    productType: String, options: Map[String, String], artistMarkup: Int, quantity: Int)

final case class Cart(items: Seq[CartItem])
```

Next, we'll want to write a decoder for this cart JSON. Decoding JSON using Circe is stateful, you essentially have a cursor that points to a position in the JSON, and you advance the cursor down or up the JSON tree.

There are very syntacticly simple ways to parse JSON in Circe, however they aren't as flexible, nor are they as explicit. If you want to use them feel free, but we'll use a more explicit way of parsing.

A basic `Decoder` looks like this:

```scala
val cartDecoder = Decoder.instance[Cart] { c =>
  ...
}
```

We essentially pass a function taking a cursor (the current position of the parser), and then from this cursor we descend down the fields we want to get the values of.

Let's parse the product type field in a cart item:

```scala
val cartItemDecoder = Decoder.instance[CartItem] { c =>
  for {
    productType <- c.downField("product-type").as[String]
  } yield CartItem(productType, Map.empty, 0, 0)
}
```

TODO Explain this


We can test this using something like the following:

```scala
{

}
```

We can plug this into our class using something like the following:

```scala

```

Now that we're done with the cart, go ahead & write a decoder for the base products. Note that the JSON isn't quite clean, feel free to change the data such that the product options are always an array.
