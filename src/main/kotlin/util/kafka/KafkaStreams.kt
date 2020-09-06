package io.gitlab.edrd.kafka.streams.util.kafka

import org.apache.kafka.streams.KafkaStreams

internal fun KafkaStreams.startAndWaitUntilReady() {
  this.start()
  @Suppress("ControlFlowWithEmptyBody")
  while (this.state() != KafkaStreams.State.RUNNING) {}
}
