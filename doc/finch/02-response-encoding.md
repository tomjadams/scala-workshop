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

Note that we're using `p.options.asJson`. This is purely for ease of use, in order to encode our map correctly, we'd need to `map` across it's entries, and turn them into JSON. While I encourage you to try to do this, we'll use the simpler version for now.

Let's write a test for that. In the same vein as our decoder test, we're going to create a generator for our sample data.

```scala

```





**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* Type classes - Circe's encoders & decoders are called type classes, this is a common pattern in Scala code.
