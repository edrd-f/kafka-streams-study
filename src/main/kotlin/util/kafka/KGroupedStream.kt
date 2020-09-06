package io.gitlab.edrd.kafka.streams.util.kafka

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore

internal fun <K, V, VR> KGroupedStream<K, V>.aggregate(
  initializer: () -> VR,
  aggregator: Aggregator<K, V, VR>,
  name: String,
  keySerde: Serde<K>,
  valueSerde: Serde<VR>
) = aggregate(
  initializer,
  aggregator,
  Materialized
    .`as`<K, VR, KeyValueStore<Bytes, ByteArray>>(name)
    .withKeySerde(keySerde)
    .withValueSerde(valueSerde)
    .withLoggingDisabled()
)
