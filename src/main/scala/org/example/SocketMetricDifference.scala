package org.example

import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

case class Metric(id: String, key: String, value: Int)

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

    // Parse raw text into a stream of Metrics, keyed by Metric id
    val metrics = text.flatMap { _.split("\\s+") }
      .flatMap { x => stringToMetric(x) }
      .keyBy(_.id)

    // Build a pattern to match start -> end events
    val pattern = Pattern.begin[Metric]("start").where(_.key == "start")
      .followedBy("end").where(_.key == "end")
      .within(Time.seconds(45))

    val patternStream = CEP.pattern(metrics, pattern)

    val alerts = patternStream.select(x => selectStartFn(x))

    alerts.print

    env.execute("Scala SocketMetricDifference")
  }

  def stringToMetric(str: String): Option[Metric] = str.split("-") match {
    case Array(a: String, b: String, c: String) => Some(Metric(a, b, c.toInt))
    case _ => None
  }

  def selectStartFn(pattern: scala.collection.mutable.Map[String, Metric]) = {
    val startEvent = pattern("start")
    val endEvent = pattern("end")

    Metric(startEvent.id, "timeTaken", endEvent.value - startEvent.value)
  }

}
