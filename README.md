# Flink Telemetry

An investigation into using [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.2/) for telemetry monitoring.

For a while now I've been levelling up on my DevOps skills, and have developed a keen interest in service monitoring. Telemetry (to me) is the collection of metrics and other data, the processing of that data into valuable information, and the triggering of actionable alerts.

## Getting Started

To run and test your application use SBT invoke: `sbt run`

In order to run your application from within IntelliJ, you have to select the classpath of the 'mainRunner' module in the run/debug configurations.

Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" select box.

## Complex Event Processing Example

[Complex Event Processing](https://en.wikipedia.org/wiki/Complex_event_processing) can be super useful for service monitoring. Imagine that we have a stream of incoming metrics from our application, giving timestamps for when each event occurred.

| session_id                           | metric               | timestamp  |
|--------------------------------------|----------------------|------------|
| 07d14d17-d3e3-48f6-9539-e6ac5c3f9149 | app_start            | 1489962162 |
| 07d14d17-d3e3-48f6-9539-e6ac5c3f9149 | products_fetch_start | 1489962194 |
| 07d14d17-d3e3-48f6-9539-e6ac5c3f9149 | products_fetch_end   | 1489962210 |

We can configure processing patterns that watch for certain events to happen, and then act on them.

Lets say we want to monitor how long our hypothetical Products API is taking to load:

```scala
Pattern.begin[Metric]("start")
  .where(_.metric == "products_fetch_start")
  .followedBy("end").where(_.metric == "products_fetch_end")
```

Then we can form new higher-order events, and push them onto another stream:

```scala
def calculateLoadTime(pattern: scala.collection.mutable.Map[String, Metric]) = {
  val startEvent = pattern("start")
  val endEvent = pattern("end")

  Metric(startEvent.session_id, "productsLoadTime", endEvent.timestamp - startEvent.timestamp)
}
```

### Benefits

There's a few benefits of this approach:

* The client is dumb. It sends simple key/value events, with little processing or overhead.
* CEP offers huge opportunities for highly-sophisticated processing server-side.
* The CEP rules are declarative, rather than procedural. Meaning they're easy to understand and reason about.
* It's realtime, actions can be triggered within seconds of a matching event pattern.

### Time

We can also incorporate time into the processing rules. We could detect abandoned shopping carts with CEP:

Start with an `APP_START` event.
Then match any number of `ADD_PRODUCT_TO_BASKET` events.
If there _hasn't_ been a `SUCCESSFUL_CHECKOUT` event within 24 hoursm, trigger an `ABANDONED_CART_EMAIL`
