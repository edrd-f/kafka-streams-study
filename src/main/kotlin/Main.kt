package io.gitlab.edrd.kafka.streams

import io.gitlab.edrd.kafka.streams.producer.MetricValueGenerator
import io.gitlab.edrd.kafka.streams.stream.MetricsByServiceStream

fun main() {
  val valueGenerator = MetricValueGenerator().apply { start() }
  val metricsByServiceStream = MetricsByServiceStream()

  metricsByServiceStream.getStore().all().forEach { item ->
    val averageByMetricType = item.value.groupBy { it.type }.mapValues { (_, metrics) ->
      metrics.sumBy { it.value } / metrics.size
    }

    println("[${item.key}]")
    averageByMetricType.forEach { (type, average) ->
      println("${type.name}: $average%")
    }
    println()
  }

  Runtime.getRuntime().addShutdownHook(Thread {
    valueGenerator.close()
    metricsByServiceStream.close()
  })
}
