# Overview

The basic agenda is to complete a simplified version of the pricing calculator homework task, and then expose a simplified version as REST API, then as a GraphQL API.

I'll help get people going, answer questions, but I won't be running it as an interactive follow-along workshop, you'll have to do the work yourself. The goal is to get you to learn by doing.

There is reading material at the end of this document.

# Agenda

1. [Introduction to Scala](#introduction-to-scala)

    We'll build a simplified version of the pricing calculator homework task. You'll learn about the basics of Scala, including:

    * [Setup](./setup.md)
    * Testing
    * ScalaCheck
    * Either & Option
    * Circe
    * Futures

2. [Finch](#finch)

    Finch is the HTTP framework we use. We'll turn the pricing calculator into a HTTP API.

3. [GraphQL](#graphql)

    We'll take the Finch API that we built, and make turn it into GraphQL API.

4. [How the iOS API works](#how-the-ios-api-works)

    We'll then cover the iOS API, what it does, how it works, how it's built. THings like:

    * BFF - Rationale, what it does, etc.
    * Architecture
    * Package structure
    * Metrics
    * Monitoring
    * Deployment

# Reading material

## Learning Scala

Depending on how you like to learn, you should also do some reading on Scala. There's heaps of help.

### Books

There are a bunch of beginner Scala books (google them). The FP oriented books are good if you want that slant.

* https://www.manning.com/books/functional-programming-in-scala

### Course

* [Twitter Scala School](https://twitter.github.io/scala_school/) - Twitter's course is a great free resource (and covers some of the tech we use)
* [Udemy](https://www.udemy.com/courses/search/?q=scala)
* [Coursera](https://www.coursera.org/specializations/scala)

## Tools/Frameworks we use

### Finch

* [Finch best practices](https://github.com/finagle/finch/blob/master/docs/best-practices.md)
* [Finagle 101](http://vkostyukov.net/posts/finagle-101/)
* [Finch 101](http://vkostyukov.ru/slides/finch-101/)
* [Finch workshop (slides)](https://nrinaudo.github.io/workshop-finch/#1)
* [Finch workshop (code)](https://github.com/nrinaudo/workshop-finch)
* [Typed services using Finch](https://www.infoq.com/presentations/finch)

### Finagle

* [Getting started with Finagle](http://andrew-jones.com/blog/getting-started-with-finagle/)
* [An introduction to Finagle](http://twitter.github.io/scala_school/finagle.html)
* [Finagle examples](https://www.codatlas.com/github.com/twitter/finagle/develop)
* [Other information on Finagle](http://dirtysalt.github.io/finagle.html)

### Cats

* [Cats documentation](http://typelevel.org/cats/)

### GraphQL

* [GraphQL Queries](http://graphql.org/docs/queries/)
* [GraphiQL App](https://github.com/skevy/graphiql-app)

### Sangria

* [Learn Sangira](http://sangria-graphql.org/learn/)
