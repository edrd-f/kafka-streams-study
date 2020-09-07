package io.gitlab.edrd.kafka.streams

import io.gitlab.edrd.kafka.streams.http.HttpServer
import io.gitlab.edrd.kafka.streams.producer.MetricValueGenerator
import io.gitlab.edrd.kafka.streams.stream.MetricsByServiceStream
import kotlin.time.measureTimedValue

val serviceNames = listOf("dashboard", "consumer", "gateway")

fun main() {
  Runtime.getRuntime().addShutdownHook(Thread {
    valueGenerator.close()
    metricsByServiceStream.close()
  })

  valueGenerator.start()
  metricsByServiceStream.start()

  HttpServer().start { request ->
    val serviceId = request.getParam("serviceId")?.toIntOrNull()
    val serviceName = request.getParam("serviceName")

    val averagesResponse = measureTimedValue {
      buildResourceUsageAveragesResponse(serviceId, serviceName)
    }

    request.response()
      .end(averagesResponse.value +
        "Took ${averagesResponse.duration.inMilliseconds}ms to calculate")
  }
}

private val valueGenerator = MetricValueGenerator()
private val metricsByServiceStream = MetricsByServiceStream()

private fun buildResourceUsageAveragesResponse(
  serviceId: Int?,
  serviceName: String?
): String = buildString {
  if (serviceId != null && serviceName != null) {
    metricsByServiceStream.getAveragesForServiceId(serviceName, serviceId)
      .forEach { (metricType, average) ->
        appendLine("${metricType.abbreviation}: $average%")
      }
  } else {
    serviceNames.forEach { serviceName ->
      appendLine("[$serviceName]")
      metricsByServiceStream.getAveragesForService(serviceName)
        .forEach { (metricType, average) ->
          appendLine("${metricType.abbreviation}: $average%")
        }
      appendLine()
    }
  }
}
