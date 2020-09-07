package io.gitlab.edrd.kafka.streams.serialization

import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

inline fun <reified T> serde(): Serde<T> = object : Serde<T>, Serializer<T>, Deserializer<T> {
  override fun serializer(): Serializer<T> = this

  override fun deserializer(): Deserializer<T> = this

  override fun serialize(topic: String?, data: T): ByteArray {
    return KotlinObjectMapper.writeValueAsBytes(data)
  }

  override fun deserialize(topic: String?, data: ByteArray): T {
    return KotlinObjectMapper.readValue(data)
  }

  override fun close() {}

  override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}
