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

## Generators

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

First, let's build a generator for our options `Map`:

```scala
private val genCartOptions = Gen.mapOf(genStringTuple)
```

`Gen.mapOf` comes from ScalaCheck, and uses the `genStringTuple` to generate the key/value pairs to put into the map.

Now we've got all the helper functions set up, we're going to build the generator for our cart item.

```scala
private val cartItemGenerator = for {
  productType <- Gen.alphaLowerStr
  options <- genCartOptions
  markup <- Gen.posNum[Int]
  quantity <- Gen.posNum[Int]
} yield CartItem(productType, options, markup, quantity)
```

We've used a for comprehension again to generate the individual field values, then pulled them together in the `yield`.

OK, so now we've got a generator for `CartItem` instances, we want to build a ScalaCheck property that takes this item, encodes it into JSON, then uses our `Decoder` to turn that JSON into a cart item again. Our test will check that the decoded cart item is the same as the one we fed into the decoder originally (this is sometimes called "roundtrip", which you will see a lot with ScalaCheck).

This process looks something like:

```
CartItem -> JSON version of CartItem -> Decoder -> CartItem
```

We're going to manually generate the JSON, we could build a Circe encoder to do this, but this means also writing the encoder, even if we're not going to make use of it in our production code! Also, I find the JSON string more readable & explicit, even if it suffers from some issues (it can get complex).

Here's how we might write that JSON generator:

```scala
private def cartItemJson(cartItem: CartItem): String =
  s"""
     |{
     |  "product-type": "${cartItem.productType}",
     |  "options": ${cartItemOptionsJson(cartItem.options)},
     |  "artist-markup": ${cartItem.artistMarkup},
     |  "quantity": ${cartItem.quantity}
     |}
   """.stripMargin

private def cartItemOptionsJson(options: Map[String, String]): String = {
  val kvsJson = options.map {
    case (k, v) => s""" "$k": "$v" """.trim
  }
  kvsJson.mkString("{", ",", "}")
}
```

Now that we've got our strings, we're going to want to decode them into our objects. Circe will do this for us, but it requires using Jawn (the actual parser underlying Circe), and since we're going to be doing it a lot, let's create a class for it:

```scala
package com.redbubble.pricer

import io.circe.{Decoder, Error, Json, ParsingFailure}

trait JsonOps {
  final def decodeJson[A](input: String)(implicit decoder: Decoder[A]): Either[Error, A] =
    parseJson(input).flatMap(decoder.decodeJson)

  private def parseJson(input: String): Either[ParsingFailure, Json] = io.circe.jawn.parse(input)
}

object JsonOps extends JsonOps
```

These two functions should be fairly self-explanatory, but it helps to know that "decoding" is actually two steps:

1. Parse: Take a string, and turn it into a `Json` instance;
1. Decode: Take a `Json` instance, and turn it into our class (the type paramater `A` in the above).

We're using `Either` to represent the notion of failure. Whenever we are parsing things that might fail (or making network requests, etc.), we want to respresent the notion of failure in the type system. Either has two "sides", a left and a right, by convention, errors are always on the left, and successes are always on the right.

Either is called a "disjoint union"; meaning that it is either one, or the other, but never both. By contrast, a tuple is a "product", meaning it has both values at the same time.

# Testing

Let's plug all this together & see how our test looks:

```scala
package com.redbubble.pricer

import com.redbubble.pricer.Decoders.cartItemDecoder
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{EitherValues, FlatSpec, Matchers}

final class DecodersSpec extends FlatSpec with Matchers with PropertyChecks with Generators with EitherValues {
  private val genCartOptions = Gen.mapOf(genStringTuple)

  private val cartItemGenerator = for {
    productType <- Gen.alphaLowerStr
    options <- genCartOptions
    markup <- Gen.posNum[Int]
    quantity <- Gen.posNum[Int]
  } yield CartItem(productType, options, markup, quantity)

  "A cart item" should "can be decoded from their JSON representation" in {
    forAll(cartItemGenerator) { (item: CartItem) =>
      val json = cartItemJson(item)
      val decodedCart = JsonOps.decodeJson(json)(cartItemDecoder)
      decodedCart.right.value shouldEqual item

    }
  }

  private def cartItemJson(cartItem: CartItem): String =
    s"""
       |{
       |  "product-type": "${cartItem.productType}",
       |  "options": ${cartItemOptionsJson(cartItem.options)},
       |  "artist-markup": ${cartItem.artistMarkup},
       |  "quantity": ${cartItem.quantity}
       |}
     """.stripMargin

  private def cartItemOptionsJson(options: Map[String, String]): String = {
    val kvsJson = options.map {
      case (k, v) => s""" "$k": "$v" """.trim
    }
    kvsJson.mkString("{", ",", "}")
  }
}
```

Hopefully, your test will fail now! This is because we've not finished the decoder, let's do that now.

```scala
val cartItemDecoder = Decoder.instance[CartItem] { c =>
  for {
    productType <- c.downField("product-type").as[String]
    options <- c.downField("options").as[Map[String, String]]
    markup <- c.downField("artist-markup").as[Int]
    quantity <- c.downField("quantity").as[Int]
  } yield CartItem(productType, options, markup, quantity)
}
```

Your tests should now pass! We're done right?

Well, no. If you were following along closely, you may have noticed that we decoded a single cart item, whereas our JSON file contains an array of them. We need to write a decoder for them. Luckily this is pretty easy to do with Circe:

```scala
val cartDecoder = Decoder.instance[Cart] { c =>
  c.as[List[CartItem]](Decoder.decodeList(cartItemDecoder)).map(is => Cart(is))
}
```

What we've done is decode the JSON array of cart items as a list, and then mapped the `Cart` constructor over this list. By convention, if we have a list of thing, we often abbreviate it as the first initial of the thing followed by an "s", so for example `items` becomes `is` (the singular would be `i`).

We can clean this up a bit by removing the need for the variable inside the function using the `_` identifier:

```scala
val cartDecoder = Decoder.instance[Cart] { c =>
  c.as[List[CartItem]](Decoder.decodeList(cartItemDecoder)).map(Cart(_))
}
```

We can go further still and remove the variable altogether:

```scala
val cartDecoder = Decoder.instance[Cart] { c =>
  c.as[List[CartItem]](Decoder.decodeList(cartItemDecoder)).map(Cart)
}
```

All of these are equivalent. Note that we are passing the decoder to the `as` function explicitly. You may have noticed that the `as` function takes an `implicit` decoder, and wondered what this meant. Basically, if you declare a variable as `implicit`, and a function takes an `implicit` parameter, you don't need to pass that value explicitly.

Circe lets us simplfy our list decoder even further too:

```scala
val cartDecoder = Decoder[List[CartItem]].map(Cart)
```

Note that we're (again) making use of the `apply` method on our decoder, what you see above is really the following, simplified:

```scala
val cartDecoder = Decoder.apply[List[CartItem]].map(Cart)
```

Note that you may also need to flag your `cartItemDecoder` with an `implicit` keyword:

```scala
implicit val cartItemDecoder = ...
```

This is because the `apply` method takes an implicit decoder for a `List[CartItem]`. Circe provides an implicit `Decoder[List[A]]` given a `Decoder[A]`, so what we need to provide is a decoder for the `A` (in this case a `Decoder[CartItem]`).

Now, we can test this by firstly adding a generator for the cart:

```scala
final def nonEmptyListOfN[A](n: Int, gen: Gen[A]): Gen[Seq[A]] =
  Gen.listOfN(n, gen).suchThat(as => as.nonEmpty)

private val cartGenerator = for {
  item <- nonEmptyListOfN(5, cartItemGenerator)
} yield Cart(item)
```

And then a test:

```scala
"A cart" should "be decoded from its JSON representation" in {
  forAll(cartGenerator) { (cart: Cart) =>
    val json = cartJson(cart)
    val decodedCart = JsonOps.decodeJson(json)(cartDecoder)
    decodedCart.right.value shouldEqual cart
  }
}

private def cartJson(cart: Cart): String = cart.items.map(cartItemJson).mkString("[", ",", "]")
```

## Decoder Integration

So, now we've written all the bits, let's glue it together in our app's main class. You'll need to read the JSON from a file, and make use of the JSON parsing & decoders you built earlier:

```scala
package com.redbubble.pricer

import java.io.File

import com.redbubble.Pricer
import com.redbubble.pricer.Decoders.cartDecoder
import io.circe.Decoder

import scala.io.Source

object App {
  def main(args: Array[String]): Unit = args match {
    case Array(cartFile: String, basePricesFile: String) =>
      val priceResult = computePrice(cartFile, basePricesFile)
      priceResult match {
        case Right(price) => println(price)
        case Left(error) => sys.error(s"Unable to calculate cart price: ${error.getMessage}")
      }
    case _ => println("Usage: pricer.App <cart.json> <base-prices.json>")
  }

  private def computePrice(cartFilename: String, basePricesFilename: String) =
    for {
      cart <- decodeJsonFile(cartFilename, cartDecoder)
      price <- new Pricer().priceFor(cart)
    } yield price

  private def decodeJsonFile[A](filename: String, decoder: Decoder[A]) = {
    val contents = readJson(filename)
    JsonOps.decodeJson(contents)(decoder)
  }

  private def readJson(filename: String) = Source.fromFile(new File(filename)).mkString
}
```

Now that we're done with the cart, go ahead & write a decoder for the base products. Note that the JSON isn't quite clean, feel free to change the data such that the product options are always an array.

**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* Tuples;
* `Either`;
* Implicits;
* `for` comprehensions.
