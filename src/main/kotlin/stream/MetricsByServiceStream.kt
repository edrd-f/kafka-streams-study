package io.gitlab.edrd.kafka.streams.stream

import io.gitlab.edrd.kafka.streams.data.Metric
import io.gitlab.edrd.kafka.streams.serialization.MetricListSerde
import io.gitlab.edrd.kafka.streams.serialization.MetricSerde
import io.gitlab.edrd.kafka.streams.util.kafka.aggregate
import io.gitlab.edrd.kafka.streams.util.kafka.startAndWaitUntilReady
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import java.io.Closeable
import java.util.Properties

class MetricsByServiceStream(
  bootstrapServer: String = "localhost:9092",
  topicName: String = "service-metrics"
) : Closeable {

  fun getStore(): ReadOnlyKeyValueStore<String, List<Metric>> {
    streams.startAndWaitUntilReady()
    return streams.store(StoreQueryParameters.fromNameAndType(
      storeName,
      QueryableStoreTypes.keyValueStore()
    ))
  }

  override fun close() = streams.close()

  private val storeName = "MetricsByService"

  private val streamsBuilder = StreamsBuilder().apply {
    stream(
      topicName,
      Consumed.with(Serdes.String(), MetricSerde.instance)
    )
    .groupByKey()
    .aggregate(
      initializer = { emptyList() },
      aggregator = { _, metric, aggregator -> aggregator + metric },
      name = storeName,
      keySerde = Serdes.String(),
      valueSerde = MetricListSerde,
    )
  }

  private val streams = KafkaStreams(streamsBuilder.build(), Properties().apply {
    put("bootstrap.servers", bootstrapServer)
    put("application.id", "metrics-dashboard")
    put("auto.offset.reset", "earliest")
  })
}
