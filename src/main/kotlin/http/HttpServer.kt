package io.gitlab.edrd.kafka.streams.http

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest

class HttpServer(private val port: Int = 8080) {
  fun start(handler: (HttpServerRequest) -> Unit) {
    vertx
      .createHttpServer()
      .requestHandler { handler(it) }
      .listen(port)
  }
  private val vertx = Vertx.vertx()
}
