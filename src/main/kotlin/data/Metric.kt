package io.gitlab.edrd.kafka.streams.data

data class Metric(
  val time: Long,
  val serviceName: String,
  val serviceId: String,
  val type: Type,
  val value: Int
) {
  enum class Type {
    Cpu {
      override fun toString() = "CPU"
    },
    Memory
  }
}
