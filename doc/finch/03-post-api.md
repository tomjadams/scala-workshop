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
abstract class Endpoints(pricer: Pricer) {
  private val prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  private val price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
    Ok(1)
  }

  val api = "v1" :: (prices :+: price)
}
```

We've made our individual endpoints private, and only exposed them through a composed `api` endpoint, onto which we've also layered versioning. Our endpoints will now be accessible via URLs such as `/v1/prices` and `/v1/price`.

We can then bring this into our server class:

```scala
final class PricingServer(pricer: Pricer) extends Endpoints(pricer) with Codecs {
  def start(): Unit = {
    val server = Http.server.serve(":8081", api.toService)
    Await.ready(server)
    ()
  }
}
```

## Request Decoding

Notice how we skipped straight over the details of what the `POST` endpoint does. Looking back at our code, we have:

```scala
private val price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
  Ok(1)
}
```

Let's call each piece out one by one:

* `price: Endpoint[Int]` says we have an endpoint that returns an `Int`;
* `post` tells Finch to expose this endpoint over HTTP `POST` only;
* `"price" :: jsonBody[Cart]` says that the URL this endpoint responds to is `/post`, and it accepts a JSON value within the body of the `POST` request (we've not seen how we can parse this yet);
* `(cart: Cart) =>` receives the decoded `Cart` instance in the function body (which we've decoded in `jsonBody[Cart]`), note that the types match!




