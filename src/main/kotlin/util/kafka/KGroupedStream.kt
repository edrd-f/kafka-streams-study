package io.gitlab.edrd.kafka.streams.util.kafka

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore

internal fun <K, V, VR> KGroupedStream<K, V>.aggregate(
  initializer: () -> VR,
  aggregator: Aggregator<K, V, VR>,
  keySerde: Serde<K>,
  valueSerde: Serde<VR>
) = this.aggregate(
  initializer,
  aggregator,
  Materialized
    .with<K, VR, KeyValueStore<Bytes, ByteArray>>(keySerde, valueSerde)
    .withLoggingDisabled()
)

internal fun <K, V, VR> KTable<K, V>.mapValues(
  mapper: (V) -> VR,
  storeName: String,
  keySerde: Serde<K>,
  valueSerde: Serde<VR>
): KTable<K, VR> {
  return this.mapValues(
    mapper,
    Materialized
      .`as`<K, VR, KeyValueStore<Bytes, ByteArray>>(storeName)
      .withKeySerde(keySerde)
      .withValueSerde(valueSerde)
  )
}
