package io.gitlab.edrd.kafka.streams.stream

import io.gitlab.edrd.kafka.streams.data.CountAndSum
import io.gitlab.edrd.kafka.streams.data.Metric
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
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.state.QueryableStoreTypes
import java.io.Closeable
import java.util.Properties

class MetricsByServiceStream(
  bootstrapServer: String = "localhost:9092",
  private val topicName: String = "service-metrics"
) : Closeable {
  fun start() = streams.startAndWaitUntilReady()

  fun getAveragesForService(serviceName: String): Map<Metric.Type, Int> {
    val storeType = QueryableStoreTypes.keyValueStore<String, Int>()
    val averageCpu = streams.store(StoreQueryParameters.fromNameAndType("AverageCpu", storeType))
    val averageMem = streams.store(StoreQueryParameters.fromNameAndType("AverageMem", storeType))
    return mapOf(
      Metric.Type.Cpu to averageCpu.get(serviceName),
      Metric.Type.Memory to averageMem.get(serviceName)
    )
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
      .groupByKey()
      .aggregate(
        initializer = { CountAndSum(0, 0) },
        aggregator = { _, metric, countAndSum ->
          CountAndSum(countAndSum.count + 1, countAndSum.sum + metric.value)
        },
        keySerde = Serdes.String(),
        valueSerde = serde(),
      ).mapValues(
        mapper = { (it.sum / it.count).toInt() },
        storeName = storeName,
        keySerde = Serdes.String(),
        valueSerde = Serdes.Integer()
      )
  }

  private val streams = KafkaStreams(streamsBuilder.build(), Properties().apply {
    put("bootstrap.servers", bootstrapServer)
    put("application.id", "metrics-dashboard")
    put("auto.offset.reset", "earliest")
  })
}
