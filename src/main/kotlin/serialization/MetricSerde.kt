package io.gitlab.edrd.kafka.streams.serialization

import com.fasterxml.jackson.module.kotlin.readValue
import io.gitlab.edrd.kafka.streams.data.Metric
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class MetricSerde : Serde<Metric>, Serializer<Metric>, Deserializer<Metric> {
  override fun serialize(topic: String, metric: Metric): ByteArray {
    return KotlinObjectMapper.writeValueAsBytes(metric)
  }

  override fun deserialize(topic: String, data: ByteArray): Metric {
    return KotlinObjectMapper.readValue(data)
  }

  override fun serializer() = this

  override fun deserializer() = this

  override fun close() {}
  override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

  companion object {
    val instance = MetricSerde()
  }
}
