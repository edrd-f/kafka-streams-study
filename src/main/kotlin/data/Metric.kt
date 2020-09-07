package io.gitlab.edrd.kafka.streams.data

data class Metric(
  val time: Long,
  val service: Service,
  val type: Type,
  val value: Int
) {
  @Suppress("unused")
  enum class Type(val abbreviation: String) {
    Cpu(abbreviation = "Cpu"),
    Memory(abbreviation = "Mem")
  }
}
