package server

import SearchEngineRequestBody
import aggregateResponses
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.configuration.StubActorsConfiguration

fun main() {
    val actorsConfiguration = StubActorsConfiguration()
    embeddedServer(Netty, port = 8080) {
        module(actorsConfiguration)
    }.start(wait = true)
}

fun Application.module(actorsConfiguration: StubActorsConfiguration) {
    with(actorsConfiguration) {
        routing {
            get("/{text}") {
                val searchText = call.parameters["text"]
                if (searchText.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Empty request")
                    return@get
                }
                val request = SearchEngineRequestBody(searchText, maxTotalSize, maxSitesCount)
                val result = aggregateResponses(request, timeLimitPerEngine, maxAttempts, engines, waitFactor)
                call.respondHtml { renderAggregation(request, result) }
            }
        }
    }
}