# Circe

## Setup

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

## Decoding

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

Let's break this down a little:

1. We use the `Decoder.instance` method to create a new `Decoder`. This isn't magic, under the hood it's creating an anonymous `Decoder` and calling the passed function in the `apply` method (again, `apply` is a common pattern in Scala):

    ```scala
    final def instance[A](f: HCursor => Result[A]): Decoder[A] = new Decoder[A] {
      final def apply(c: HCursor): Result[A] = f(c)
    }
    ```

1. The function that does the actual work starts with a cursor, this points to the current position of the parser. Obviously this means that the decoder is relative, you must invoke it at the right point in the parsing, pointing at the right piece. This can mean that sometimes your decoder does odd things, and it's important to have adequate tests.

1. Given a cursor position, the next thing it does is go down the field called `product-type`, and parse it's value as a `String`. This operation could fail for a couple of reasons. Firstly, the `product-type` field may not exist in the JSON, and secondly it's value may not be a string (or convertible to a string).

1. We wrap all this up `for` comprehension. You will likely build your decoders using `for` comprehensions, as this allows us to handle the potential decoding failures in a syntactically clean way (the failures we mentioned above). Essentially, the for comprehensions hides the error handling from us in a clean way.

## Testing

So now we've got our decoder, let's build a test for it. We'll use the same pattern as we've used before to write the test.

Firstly, we need to write a ScalaCheck generator for our `CartItem` class. Before we do that, we're going to build some pieces of helper code, that will allow us to reuse some of our more common

```scala
package com.redbubble.pricer

import org.scalacheck.Gen

trait Generators {
  final val genStringValue: Gen[String] = Gen.alphaStr.map(_.take(20))

  final val genNotEmptyString: Gen[String] = Gen.alphaStr.suchThat(s => !s.isEmpty)

  final val genStringTuple: Gen[(String, String)] = for {
    k <- Gen.identifier
    v <- genStringValue
  } yield (k, v)
}

object Generators extends Generators

```

What we've done is create a couple of generic generators (which we will reuse), these are the things that "generate" values for us to use in our tests. We've created three generic generators:

* `genStringValue` - this generates alpha strings (no numbers), and then trucates them at 20 characters;
* `genNotEmptyString` - this generates non-empty, alpha strings;
* `genStringTuple` - this generates tuples (key/value pairs), where both elements are `String`s. We will use this to generate values for `Map`s (maps are really just lists of key/value pairs).

Now that we have some basic helpers, let's build a generator for our `CartItem` class.

First, let's 

```scala
private val genCartOptions = Gen.mapOf(genStringTuple)
```



```scala
```


We can plug this into our class using something like the following:

```scala

```

Now that we're done with the cart, go ahead & write a decoder for the base products. Note that the JSON isn't quite clean, feel free to change the data such that the product options are always an array.


**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* Tuples;
* `for` comprehensions.
