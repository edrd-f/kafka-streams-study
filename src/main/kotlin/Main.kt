package io.gitlab.edrd.kafka.streams

import io.gitlab.edrd.kafka.streams.http.HttpServer
import io.gitlab.edrd.kafka.streams.producer.MetricValueGenerator
import io.gitlab.edrd.kafka.streams.stream.MetricsByServiceStream
import kotlin.time.measureTimedValue

fun main() {
  Runtime.getRuntime().addShutdownHook(Thread {
    valueGenerator.close()
    metricsByServiceStream.close()
  })

  valueGenerator.start()
  metricsByServiceStream.start()

  HttpServer().start { request ->
    val averagesResponse = measureTimedValue { buildResourceUsageAveragesResponse() }

    request.response()
      .end(averagesResponse.value +
        "Took ${averagesResponse.duration.inMilliseconds}ms to calculate")
  }
}

private val valueGenerator = MetricValueGenerator()
private val metricsByServiceStream = MetricsByServiceStream()

private fun buildResourceUsageAveragesResponse(): String = buildString {
  metricsByServiceStream.getStore().all().forEach { item ->
    val averageByMetricType = item.value.groupBy { it.type }.mapValues { (_, metrics) ->
      metrics.sumBy { it.value } / metrics.size
    }
    appendLine("[${item.key}]")
    averageByMetricType.forEach { (type, average) ->
      appendLine("${type.name}: $average%")
    }
    appendLine()
  }
}
