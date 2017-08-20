# Finch

## Setup

Next, we're going to expose our pricing engine via a HTTP REST-ful endpoint. To do this, we're going to use [Finch](http://finagle.github.io/finch/), which is a small library built on top of [Finagle](https://twitter.github.io/finagle/).

If at any point you get into trouble, there's some examples & documentation that might help.

* [Finch Documentation](http://finagle.github.io/finch/)
* [Cookbook](http://finagle.github.io/finch/cookbook.html) - Some commone how-tos;
* [Best Practices](http://finagle.github.io/finch/best-practices.html) - Best practices for building Finch services;

The first thing that we're going to do is bring Finch into our dependencies:

```scala
val finchVersion = "0.15.1"
lazy val finchCore = "com.github.finagle" %% "finch-core" % finchVersion
lazy val finchCirce = "com.github.finagle" %% "finch-circe" % finchVersion

...

libraryDependencies ++= Seq(
  circeCore,
  circeGeneric,
  circeParser,
  finchCore,
  finchCirce,
  scalaTest,
  scalaCheck
)
```

I'd also strongly recommend that you install [sbt-revolver](https://github.com/spray/sbt-revolver), this is an sbt plugin that will watch for code changes & restart your server when it does.

Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")
```

And restart your sbt REPL.

## A Simple Server

Let's start by exposing our base prices over a HTTP `GET`. Then we'll expose a `POST` endpoint to post carts to, where we'll make use of the decoders that wrote earlier.

The first thing we need to do is to create another main class to run our HTTP server. We're going to expose our list of base prices over a `GET`.

To start off, let's expose a sequence of numbers on a `/prices` URL, over port `80`:

```scala
package com.redbubble.pricer.http

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._
import io.finch.{Endpoint, _}

object HttpApp {
  val prices: Endpoint[Seq[Int]] = get("prices") {
    Ok(Seq(1, 2, 3, 4, 5))
  }

  def main(args: Array[String]): Unit = {
    val server = Http.server.serve(":8081", prices.toService)
    Await.ready(server)
  }
}
```

Let's start the server using the same `run` sbt task we used earlier:

```shell
> run
[info] Compiling 1 Scala source to /Users/tom/Projects/Personal/scala-workshop/example_code/target/scala-2.12/classes...
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list

Multiple main classes detected, select one to run:

 [1] com.redbubble.pricer.http.HttpApp
 [2] com.redbubble.pricer.shell.CommandLineApp

Enter number: 
```

OK! We've now got two main classes (you may have just replaced your other main class, that's fine), and we need to select which one to run. We've got a few options here.

1. We can just select the class we want to run every time;
1. We can remove/rename/replace our command line main class;
1. We can update our build file to specify a default main class.

Let's do the last one. We want to add a `mainClass` entry into our settings. If no main class is specified, sbt will use this one by default.

```scala
import Dependencies._
import sbt.Keys.mainClass

lazy val root = (project in file(".")).
    settings(
      inThisBuild(List(
        organization := "com.redbubble",
        scalaVersion := "2.12.1",
        version := "0.1.0-SNAPSHOT"
      )),
      name := "Pricer",
      mainClass in Compile := Some("com.redbubble.pricer.http.HttpApp"),
      libraryDependencies ++= Seq(
        circeCore,
        circeGeneric,
        circeParser,
        finchCore,
        finchCirce,
        scalaTest,
        scalaCheck
      )
    )
```

And let's re-run our server:

```shell
> reload
> run
[info] Running com.redbubble.pricer.http.HttpApp 
Aug 20, 2017 2:40:35 PM com.twitter.finagle.Init$ $anonfun$once$1
INFO: Finagle version 6.45.0 (rev=fadc80cdd804f2885ebc213964542d5568a4f485) built at 20170609-103217
```

We can now hit it with a `GET` request:

```shell
$ curl -i "http://localhost:8081/prices"
HTTP/1.1 200 OK
Content-Type: application/json
Date: Sun, 20 Aug 2017 04:41:22 GMT
Content-Length: 11

[1,2,3,4,5]
```

Awesome! We've just created a simple server to expose some numbers. 

Remember that sbt-revolver plugin we added earlier? We can make use of that to dynamically restart our server whenever we make a code change. Instead of using `run` in the sbt REPL, you can use `~re-start` to start the server using revolver, and, watch for changes continuously (`~` does this for most sbt commands, e.g. `~test`):

```shell
> ~re-start
```

You can stop it then by pressing enter, and then `re-stop`.
