# Cart POST API

## Setup

The next step in building out our API is to accept a `POST` request, containing the contents of a cart, and compute it's price. We've already got all the pieces of this, we just need to hook them together.

But firstly, let's do some minor rework to get things ready. We're going to put all our encoding into a trait, make a trait for our decoding (which we'll do soon), and mix those both into a `Codecs` class, so that we can import these into scope easily. Here's what it looks like now:

```scala
trait ResponseEncoders {
  implicit val productsResponseEncode: Encode.Json[NonEmptyList[BaseProduct]] = dataJsonEncode(baseProductsEncoder)

  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}

trait RequestDecoders {
  // decoders go here
}

trait Codecs extends RequestDecoders with ResponseEncoders

final class PricingServer(pricer: Pricer) extends Codecs {
  val prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  def start(): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
    ()
  }
}
```

OK, the next thing we want to do is to build a basic `POST` endpoint that takes a cart in the request payload, and returns the price as an integer. Let's mock this out by hard coding the price. We'll also do a little reorganisation, and add a version prefix to the URLs, because we like versioning things.

The first thing we're going to do is pull out all our endpoints into a single class. This class needs access to our `Pricer` so that it can do the work we need it to do.

```scala
abstract class Endpoints(pricer: Pricer) extends Codecs {
  val api = "v1" :: (prices :+: price)

  private def prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  private def price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
    Ok(42)
  }
}
```

We've made our individual endpoints private, and only exposed them through a composed `api` endpoint, onto which we've also layered versioning. Our endpoints will now be accessible via URLs such as `/v1/prices` and `/v1/price`.

If you were paying very close attention you'd have noticed we swapped our `val`s `def`s. There's no real reason to do this apart from stylistically & lexicographically, our endpoints are referentially transparent, and we've memoised them in the `api` field.

We can then bring this into our server class:

```scala
final class PricingServer(pricer: Pricer) extends Endpoints(pricer) {
  def start(): Unit = {
    val server = Http.server.serve(":8081", api.toService)
    Await.ready(server)
    ()
  }
}
```

And, because we've added a new endpoint with a different return type, we need to add an encoder for it to our `ResponseEncoders` class:

```scala
implicit val priceResponseEncode: Encode.Json[Int] = dataJsonEncode(Encoder.encodeInt)
```

## Request Decoding

Notice how we skipped straight over the details of what the `POST` endpoint does. Looking back at our code, we have:

```scala
private def price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
  Ok(42)
}
```

Let's call each piece out one by one:

* `price: Endpoint[Int]` says we have an endpoint that returns an `Int`;
* `post` tells Finch to expose this endpoint over HTTP `POST` only;
* `"price" :: jsonBody[Cart]` says that the URL this endpoint responds to is `/post`, and it accepts a JSON value within the body of the `POST` request (we've not seen how we can parse this yet);
* `(cart: Cart) =>` receives the decoded `Cart` instance in the function body (which we've decoded in `jsonBody[Cart]`), note that the types match!

So how do we decode that request body? Well, we need to build a `Decode.Json` instance for our `Cart` type. In the same way that we handle encoding (`A -> Encoder[A] -> Encode.Json[A]`), we handle decoding the same way (`Decode.Json[A] -> Decoder [A] -> A`).

Again, we can let the default Finch/Circe machinery handle the decoding for us (by using those two inputs we showed earlier), or, we can customise the process. Because we care about sending a `data` element as the top level, and because we're doing a workshop on how to build these things, .

Finch deals with binary data rather than strings, but our decoding helpers (`JsonOps`) all work on strings. We can extend these to deal with binary data also:

```scala
final def decode[A](input: Buf)(implicit decoder: Decoder[A]): Either[Error, A] =
  parseJson(input).flatMap(decoder.decodeJson)

final def parseJson(input: Buf): Either[ParsingFailure, Json] =
  io.circe.jawn.parseByteBuffer(Owned.extract(input).toByteBuffer())
```

Notice that we need to deal with the Finch/Finagle representation of binary data, which is a `Buf`, which means we need to do some conversion between `Buf`s and `ByteBuffer`s.

So let's get onto the actual decoding bits. The first thing we're going to need, is a Circe decoder that understands our top level `data` element:

```scala
private def dataFieldObjectDecoder[A](implicit decoder: Decoder[A]): Decoder[A] =
  Decoder.instance(c => c.downField("data").as[A](decoder))
```

By now, this should look fairly standard, we traverse a JSON object, descending down the "data" field and decode the result using the `decoder` we've been passed.

The next thing we want to do is to 


```scala
private def decodePayload[A](payload: Buf, decoder: Decoder[A]): Try[A] =
  JsonOps.decode(payload)(decoder) match {
    case Left(error) => Throw(new Exception(s"Unable to decode JSON payload: ${error.getMessage}", error))
    case Right(value) => Return(value)
  }
```

Our `Decode.Json` instance takes expects a `Try`, but we have an `Either`, so we need to convert between these two types. There are `Either` -> `Try` conversions in the standard library, but we are using Finch, which uses Twitter's versions of a lot of now standard library classes. Most of Twitter's use of them predates the standard library versions, and they are now slightly incompatible so can't be wholesale replaced.

If you want to look it up, we can do this conversion because our two types are "isomporphic" to each other. If you can convert back and forwards between types without data loss, this is called a "bijection". In fact, [Twitter has a library by this name](https://github.com/twitter/bijection), for just this purpose.

Now, we just need our `Decode.Json` instance to plug all these together:

```scala
final def decodeDataJson[A](implicit decoder: Decoder[A]): Decode.Json[A] =
  Decode.json { (payload, _) =>
    val requestDecoder = dataFieldObjectDecoder(decoder)
    decodePayload(payload, requestDecoder)
  }
```

Let's try posting a sample cart to our API:

```shell
$ curl -i -d '{"data":[{"product-type":"hoodie","options":{"print-location":"front","colour":"white","size":"small"},"artist-markup":20,"quantity":0}]}' http://localhost:8081/v1/price
HTTP/1.1 200 OK
Content-Type: application/json
Date: Tue, 22 Aug 2017 06:25:40 GMT
Content-Length: 11

{"data":42}
```

Success!!!

And now we can actually hook up our pricer:

```scala
private def price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
  Ok(pricer.priceFor(cart))
}
```

And re-run our request:

```shell
$ curl -i -d '{"data":[{"product-type":"hoodie","options":{"print-location":"front","colour":"white","size":"small"},"artist-markup":20,"quantity":0}]}' http://localhost:8081/v1/price
HTTP/1.1 200 OK
Content-Type: application/json
Date: Tue, 22 Aug 2017 06:28:20 GMT
Content-Length: 10

{"data":0}
```

So an empty cart costs us nothing! Bargain!

# Wrapping Up

For the sake of this workshop, we're going to skip writing the tests for the decoding machinery, but you should feel free to write tests for the encoders & decoders we've created for Finch.

We've now completed a simple Finch service that both returns base prices, as well as calculates the cost of carts against these base prices.

**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* `Try`;
* Isomorphism.
