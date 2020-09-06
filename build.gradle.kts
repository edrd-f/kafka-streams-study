import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version ("1.4.0")
}

repositories {
  jcenter()
}

val junitVersion: String by project

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

application.mainClassName = "io.gitlab.edrd.kafka.streams.MainKt"

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjsr305=strict")
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
