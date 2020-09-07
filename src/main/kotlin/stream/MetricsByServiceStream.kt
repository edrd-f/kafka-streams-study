package io.gitlab.edrd.kafka.streams.stream

import io.gitlab.edrd.kafka.streams.data.CountAndSum
import io.gitlab.edrd.kafka.streams.data.Metric
import io.gitlab.edrd.kafka.streams.data.Service
import io.gitlab.edrd.kafka.streams.serialization.MetricSerde
import io.gitlab.edrd.kafka.streams.serialization.serde
import io.gitlab.edrd.kafka.streams.util.kafka.aggregate
import io.gitlab.edrd.kafka.streams.util.kafka.mapValues
import io.gitlab.edrd.kafka.streams.util.kafka.startAndWaitUntilReady
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import java.io.Closeable
import java.util.Properties

class MetricsByServiceStream(
  bootstrapServer: String = "localhost:9092",
  private val topicName: String = "service-metrics"
) : Closeable {
  fun start() = streams.startAndWaitUntilReady()

  fun getAveragesForService(serviceName: String): Map<Metric.Type, Long> {
    return mapWithValueForEachMetricType { averages ->
      calculateAverageForAllServiceIds(averages, serviceName)
    }
  }

  fun getAveragesForServiceId(name: String, id: Int): Map<Metric.Type, Long> {
    return mapWithValueForEachMetricType { averages ->
      averages.get(Service(id, name)).toLong()
    }
  }

  private fun mapWithValueForEachMetricType(
    valueSupplier: (store: ReadOnlyKeyValueStore<Service, Int>) -> Long
  ): Map<Metric.Type, Long> {
    val storeType = QueryableStoreTypes.keyValueStore<Service, Int>()

    return Metric.Type.values().associate { type ->
      val averages = streams.store(
        StoreQueryParameters.fromNameAndType("Average${type.abbreviation}", storeType)
      )
      type to valueSupplier(averages)
    }
  }

  private fun calculateAverageForAllServiceIds(
    store: ReadOnlyKeyValueStore<Service, Int>,
    serviceName: String
  ): Long {
    return store.all()
      .asSequence()
      .filter { it.key.name == serviceName }
      .fold(CountAndSum(0, 0)) { accumulator, item ->
        CountAndSum(accumulator.count + 1, accumulator.sum + item.value)
      }
      .run { sum / count.coerceAtLeast(1) }
  }

  override fun close() = streams.close()

  private val streamsBuilder = StreamsBuilder().apply {
    val stream = stream(topicName, Consumed.with(Serdes.String(), MetricSerde.instance))
    Metric.Type.values().forEach { type ->
      setupMaterializedAggregationForMetricType(stream, type, "Average${type.abbreviation}")
    }
  }

  private fun setupMaterializedAggregationForMetricType(
    stream: KStream<String, Metric>,
    metricType: Metric.Type,
    storeName: String
  ) {
    stream.filter { _, value -> value.type == metricType }
      .groupBy({ _, metric -> metric.service }, Grouped.with(serde<Service>(), serde<Metric>()))
      .aggregate(
        initializer = { CountAndSum(0, 0) },
        aggregator = { _, metric, countAndSum ->
          CountAndSum(countAndSum.count + 1, countAndSum.sum + metric.value)
        },
        keySerde = serde<Service>(),
        valueSerde = serde(),
      ).mapValues(
        mapper = { (it.sum / it.count).toInt() },
        storeName = storeName,
        keySerde = serde<Service>(),
        valueSerde = Serdes.Integer()
      )
  }

  private val streams = KafkaStreams(streamsBuilder.build(), Properties().apply {
    put("bootstrap.servers", bootstrapServer)
    put("application.id", "metrics-dashboard")
    put("auto.offset.reset", "earliest")
  })
}
