package io.gitlab.edrd.kafka.streams.producer

import io.gitlab.edrd.kafka.streams.data.Metric
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.milliseconds

class MetricValueGenerator(
  delay: Duration = 100.milliseconds,
  topicName: String = "service-metrics"
) : Closeable {

  fun start() = thread.start()

  override fun close() {
    shutdown = true
    latch.await()
    producer.close()
  }

  private var shutdown = false

  private val latch = CountDownLatch(1)

  private val thread = Thread {
    val values = sequenceGenerator.iterator()

    while (true) {
      producer.send(values.next())

      if (shutdown) break

      Thread.sleep(delay.toLongMilliseconds())
    }
  }

  private val producer = Producer(topicName = topicName)

  private val serviceNames = listOf("dashboard", "consumer", "gateway")

  private val random = Random(seed = System.currentTimeMillis())

  private val metricTypes = Metric.Type.values()

  private val sequenceGenerator = generateSequence {
    val serviceName = serviceNames.random()
    Metric(
      time = System.currentTimeMillis(),
      serviceName = serviceName,
      serviceId = "$serviceName-${random.nextInt(until = 3)}",
      type = metricTypes.random(),
      value = random.nextInt(until = 101)
    )
  }
}
