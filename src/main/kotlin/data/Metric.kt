package io.gitlab.edrd.kafka.streams.data

data class Metric(
  val time: Long,
  val serviceName: String,
  val serviceId: String,
  val type: Type,
  val value: Int
) {
  enum class Type(val abbreviation: String) {
    Cpu(abbreviation = "Cpu"),
    Memory(abbreviation = "Mem")
  }
}
