# JSON Encoding

## Automatic Encoding

If you were paying attention, you may have noticed that we didn't define how we were encoding our list of integers into JSON. This is because we made use of some built in features in Finch & Circe, to do this for us.

In order to return JSON over our HTTP endpoint, we need a way to:

1. Turn our `Endpoint`'s return type (`Seq[Int]`) into JSON;
1. Turn JSON into a HTTP response.

These are two discrete steps in Finch, the first we achieve using Circe `Encoder`s (remember the decoder's we wrote?), and the second with Finch's `Encode`.

The following two lines bring in Circe's automatic derivation (automatically figures out how to map types to JSON), as well as Finch's support for turning Circe JSON encoders into response encoders.

```scala
import io.circe.generic.auto._
import io.finch.circe._
```

You can dig into this by looking inside the `io.finch.circe.Encoders` class:

```scala
/**
 * Maps Circe's [[Encoder]] to Finch's [[Encode]].
 */
implicit def encodeCirce[A](implicit e: Encoder[A]): Encode.Json[A] =
  Encode.json((a, cs) => print(e(a), cs))
```

The `e(a)` is the bit where the encoder is used (the function is applied) to return the JSON into the response.

## Custom Encoders

Automatic encoders are fine for simple types, but where you want control over the encoding process (e.g. adding a top-level `data` element), it is usually a good idea to explicitly build your own response encoder.

We'll build one up piece by piece, firstly, we'll build a Circe encoder for our base product class, and then we'll write a Finch response encoder that adds a top level `data` element.

If you want to read up more on encoders, the [Finch user guide](http://finagle.github.io/finch/user-guide.html#json) has more details.

A basic encoder looks a lot like our decoder from earlier:

```scala
val baseProductEncoder = Encoder.instance { p =>
  ...
}
```

Again, this isn't magic, just a shorthand way of creating an encoder instance by passing a function that takes the class we want to encode, and returns a `Json` instance. We can write this as: `A => Json` (where `A` is our `BaseProduct` class). 

Circe contains several primatives for building `Json` instances from types.

* Manual - e.g. `Json.fromString("I'm a string")`. You can manually create `Json` instances using various static contructors on the `Json` class.
* Automatic - e.g. `"I'm a string".asJson`. If you have an encoder for the type in scope (note that `asJson` takes an `implicit` decoder), you can simply call `asJson` on the instance of the type. This is handy, but relies on having encoders in scope for all of the types within the object graph of your type.

Which you use is up to you. I find the manual constructor approach more flexible though, so will (mostly) use that here.

```scala
val baseProductEncoder: Encoder[BaseProduct] = Encoder.instance { p =>
  Json.obj(
    "product-type" -> Json.fromString(p.productType),
    "options" -> p.options.asJson,
    "base-price" -> Json.fromInt(p.basePrice)
  )
}
```

Note that we're using `p.options.asJson`. This is purely for ease of use, in order to encode our map correctly, we'd need to `map` across it's entries, and turn them into JSON. While I encourage you to try to do this, we'll use the simpler version for now (there's a [manual version](https://github.com/tomjadams/scala-workshop/blob/e44be3f42b65fbdbde503dd9df2d4f7677c363db/example_code/src/main/scala/com/redbubble/pricer/common/Encoders.scala#L20) in the example code).

Let's write a test for that. In the same vein as our decoder test, we're going to create a generator for our sample data.

```scala
final val genNestedStringTuple: Gen[(String, Seq[String])] = for {
  k <- Gen.identifier
  v <- Gen.oneOf(Gen.const(Seq.empty), Gen.listOfN(5, genStringValue))
} yield (k, v)

private val genCartOptions = Gen.mapOf(genNestedStringTuple)

private val baseProductGenerator = for {
  productType <- Gen.alphaLowerStr
  options <- genCartOptions
  basePrice <- Gen.posNum[Int]
} yield BaseProduct(productType, options, basePrice)
```

And we'll generate some sample JSON to ensure our encoder is doing the right thing:

```scala
private def baseProductJson(product: BaseProduct) =
  s"""
     |{
     |  "product-type": "${product.productType}",
     |  "options": ${cartItemOptionsJson(product.options)},
     |  "base-price": ${product.basePrice}
     |}
   """.stripMargin

private def cartItemOptionsJson(options: Map[String, Seq[String]]) = {
  val kvsJson = options.map {
    case (k, v) => s""" "$k": ${v.map(valueJson).mkString("[", ",", "]")} """.trim
  }
  kvsJson.mkString("{", ",", "}")
}

private def valueJson(v: String) = s""" "$v" """.trim
```

Now, let's write the test that hooks all this together:

```scala
"A base product" should "be encoded to its JSON representation" in {
  forAll(baseProductGenerator) { (product: BaseProduct) =>
    val expected = JsonOps.parseJson(baseProductJson(product))
    val actual = JsonOps.parseJson(JsonOps.encode(product)(baseProductEncoder).noSpaces)
    expected shouldEqual actual
  }
}
```

What we've done is created both some example hand-crafted JSON, and used our encoder to create the real JSON. We then parse both strings - so we compare the JSON representation, not the string - and compare the parsed `Json` instances.

Notice that our encode method takes two parameters: `JsonOps.encode(product)(baseProductEncoder)`. If we look at the definition you can see two parameter lists:

```scala
final def encode[A](a: A)(implicit encoder: Encoder[A]): Json = encoder(a)
```

The first parameter here takes the object that we want to encode, the second the encoder that does that encoding. In Scala, you can have multiple paramater lists, these are used for both partial application ([currying](https://alvinalexander.com/scala/fp-book/partially-applied-functions-currying-in-scala)), as well as providing implicit values (to make code simpler, provide defaults, etc.).

In the second parameter list the `implicit` keyword, we can either flag a variable (of the same type) within scope as being `implicit` also, or, pass the value explicitly. In our test, we've pased the value explicitly. Implicit arguments always go on the last parameter list, and apply to all parameters within that list, not just the one that is flagged.

## Response Encoders

Now we want to plug our encoder into the Finch response machinery. Let's go back to our HTTP main class & modify it to return a list of base products, we'll hard code a product for now & remove our Circe & Finch automatic machinery. If you've already built the decoder for the base products JSON, feel free to plug this in instead.

```scala
//import io.circe.generic.auto._
//import io.finch.circe._

val prices: Endpoint[Seq[BaseProduct]] = get("prices") {
  Ok(Seq(BaseProduct("hoodie", Map.empty, 0)))
}
```

If we compile this, we should get an error:

```shell
> compile
[info] Compiling 1 Scala source to /Users/tom/Projects/Personal/scala-workshop/example_code/target/scala-2.12/classes...
[error] /Users/tom/Projects/Personal/scala-workshop/example_code/src/main/scala/com/redbubble/pricer/http/HttpApp.scala:16: An Endpoint you're trying to convert into a Finagle service is missing one or more encoders.
[error] 
[error]   Make sure Seq[com.redbubble.pricer.common.BaseProduct] is one of the following:
[error] 
[error]   * A com.twitter.finagle.http.Response
[error]   * A value of a type with an io.finch.Encode instance (with the corresponding content-type)
[error]   * A coproduct made up of some combination of the above
[error] 
[error]   See https://github.com/finagle/finch/blob/master/docs/cookbook.md#fixing-the-toservice-compile-error
[error]     val server = Http.server.serve(":8081", prices.toService)
[error]                                                    ^
[error] one error found
[error] (compile:compileIncremental) Compilation failed
[error] Total time: 4 s, completed 21/08/2017 9:03:02 PM
```

What this is saying is that we need to provide a way encode a list of base products into a response (this is what "A value of a type with an io.finch.Encode instance" means).

Let's go ahead and bring back the automatic machinery & hook our new encoder in (as an implicit so that it works with the machinery):

```scala
import io.finch.circe._
import io.circe.Encoder._

implicit val bpe = Encoders.baseProductEncoder

val prices: Endpoint[Seq[BaseProduct]] = get("prices") {
  Ok(Seq(BaseProduct("hoodie", Map.empty, 0)))
}
```

Let's hit the endpoint again:

```shell
$ curl -i "http://localhost:8081/products"
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 21 Aug 2017 21:24:49 GMT
Content-Length: 55

[{"product-type":"hoodie","options":{},"base-price":0}]
```

So now we have our base products being returned as JSON. But what if we want to go & change the response format? It's always a good idea to return a top-level object in JSON responses, so let's go ahead and do this.

What this means however, is we now have to write our own response encoder. While this is pretty easy, there are a couple of moving parts. A response encoder is an instance of `Encode.Json[A]`, which is just a `io.finch.Encode[A]` with a JSON content type.

Here is our response encoder:

```scala
object ResponseEncoders {
  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}
```

Let's plug it into our app. We'll need to make use of some implicits in order to simplify the code as Circe expects type classes for things we probably don't want to explicitly pass.

```scala
object HttpApp {
  private val baseProductsEncoder = Encoder.encodeTraversableOnce[BaseProduct, Seq]
  private implicit val productsResponseEncode = dataJsonEncode(baseProductsEncoder)

  val prices: Endpoint[Seq[BaseProduct]] = get("prices") {
    Ok(Seq(BaseProduct("hoodie", Map.empty, 0)))
  }

  def main(args: Array[String]): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
  }
}
```

Let's hit the endpoint again:

```shell
$ curl -i "http://localhost:8081/products"
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 21 Aug 2017 11:33:51 GMT
Content-Length: 64

{"data":[{"product-type":"hoodie","options":{},"base-price":0}]}
```

Success! Now we can go ahead & hook the pricer into the endpoint.

## Hooking in the Pricer

If you've gotten this far, you should now have a simple endpoint, serving base products. Let's take some time to do a small refactor of our code, to clean up our API. There's obviously a lot of ways that we can do this, so this is just one example of how it might be done. There's an assumption here that you've completed your pricer, and that it actually calculates prices, however in the examples we've hard coded the pricer to a price of `0`.

Firstly, we need to expose the `baseProducts` in our `Pricer` class. We can do this by simply adding a `val` before the variable definition (case classes do this for us automatically):

```scala
final class Pricer(val baseProducts: NonEmptyList[BaseProduct]) {
  def priceFor(cart: Cart): Int = {
    0
  }
}
```

And because we want to expose our products as a `NonEmptyList`, we need to tweak the encoder (Circe supports `NEL` out of the box, and again, we can do this automagically via implicits if we choose):

```scala
val baseProductsEncoder = Encoder.encodeNonEmptyList(baseProductEncoder)
```

Now, we're ready to rework our service:

```scala
final class PricingServer(pricer: Pricer) {
  private implicit val productsResponseEncode = dataJsonEncode(baseProductsEncoder)

  val prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  def start(): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
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
```

And to make syre that we've not changed anything, we can run the tests & hit the endpoint:

```shell
$ curl -i "http://localhost:8081/products"
HTTP/1.1 200 OK
Content-Type: application/json
Date: Tue, 22 Aug 2017 01:33:16 GMT
Content-Length: 64

{"data":[{"product-type":"hoodie","options":{},"base-price":0}]}
```

Awesome. We've refactored things & haven't broken anything!

Note that we have hard coded our list of base products in the example above, in reality you'd hook in your base pricing parser from the [first section of the workshop](https://github.com/tomjadams/scala-workshop/blob/master/doc/intro/05-circe.md#decoder-integration).

## Summary

OK, so by now, we've built a simple RESTful API that exposes our base products over a HTTP `GET`. In the next section we're going to see how to accept `POST`s so that we can complete our cart pricing API.

We've also built a custom response encoder to customise the responses that we send to clients. Now all this might seem like a lot of work, just to define a way to encode things to JSON, and to be fair, it is. But once we've done it, we don't need to do it again. In fact, the [Finch template](https://github.com/redbubble/finch-template/blob/master/common/src/main/scala/com/redbubble/util/http/ResponseOps.scala) does just that.

**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* Implicit parameters;
* Partial function application;
* Type classes - Circe's encoders & decoders are called type classes, this is a common pattern in Scala code.
