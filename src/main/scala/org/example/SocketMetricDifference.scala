package org.example

import org.apache.flink.streaming.api.scala._

case class Metric(id: String, key: String, value: String)

object SocketMetricDifference {

  def main(args: Array[String]) {
    if (args.length != 2) {
      System.err.println("USAGE:\nSocketMetricDifference <hostname> <port>")
      return
    }

    val hostName = args(0)
    val port = args(1).toInt

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text = env.socketTextStream(hostName, port)

    // Parse raw text into a stream of Metrics
    val metrics = text.flatMap { _.toLowerCase.split("\\s+") }
      .map (stringToMetric _)
      .filter (_.nonEmpty)

    val counts = metrics
      .map { (_, 1) }

    counts print

    env.execute("Scala SocketMetricDifference")
  }

  def stringToMetric(str: String): Option[Metric] = str.split("-") match {
    case Array(a: String, b: String, c: String) => Some(Metric(a, b, c))
    case _ => None
  }

}
