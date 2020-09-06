import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.4.0"
}

repositories {
  jcenter()
}

val junitVersion: String by project
val kafkaVersion: String by project

dependencies {
  implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
  implementation("org.apache.kafka:kafka-streams:$kafkaVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

  runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

application.mainClassName = "io.gitlab.edrd.kafka.streams.MainKt"

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.time.ExperimentalTime")
  }
}

tasks.withType<Test> {
  useJUnitPlatform {
    testLogging {
      events("passed", "skipped", "failed")
    }
  }

  reports {
    junitXml.isEnabled = false
  }

  systemProperty("junit.jupiter.execution.parallel.enabled", "true")
  systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

  maxParallelForks = Runtime.getRuntime().availableProcessors()
}
