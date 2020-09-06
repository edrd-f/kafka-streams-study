package io.gitlab.edrd.kafka.streams.serialization

import com.fasterxml.jackson.module.kotlin.readValue
import io.gitlab.edrd.kafka.streams.data.Metric
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

object MetricListSerde : Serde<List<Metric>>,
                         Serializer<List<Metric>>,
                         Deserializer<List<Metric>> {
  override fun serializer() = this

  override fun deserializer() = this

  override fun close() {}

  override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

  override fun serialize(topic: String, data: List<Metric>): ByteArray {
    return KotlinObjectMapper.writeValueAsBytes(data)
  }

  override fun deserialize(topic: String, data: ByteArray): List<Metric> {
    return KotlinObjectMapper.readValue(data)
  }
}
