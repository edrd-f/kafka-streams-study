package io.gitlab.edrd.kafka.streams.producer

import io.gitlab.edrd.kafka.streams.data.Metric
import io.gitlab.edrd.kafka.streams.serialization.MetricSerde
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.io.Closeable
import java.util.Properties

class Producer(
  private val bootstrapServer: String = "localhost:9092",
  private val topicName: String = "service-metrics"
) : Closeable {
  fun send(metric: Metric) {
    ProducerRecord(topicName, metric.service.name, metric).let(kafkaProducer::send)
  }

  override fun close() {
    kafkaProducer.close()
  }

  private val kafkaProducer = KafkaProducer<String, Metric>(Properties().apply {
    put("bootstrap.servers", bootstrapServer)
    put("key.serializer", StringSerializer::class.qualifiedName)
    put("value.serializer", MetricSerde::class.qualifiedName)
  })
}
