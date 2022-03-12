package actors

import AbstractUserMessage
import AggregatingRequest
import SearchEngineRequestBody
import SearchEngineResponseBody
import SingleRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.selects.select
import kotlin.time.Duration
import kotlin.time.times

@OptIn(ObsoleteCoroutinesApi::class)
fun CoroutineScope.makeMainActor(
    maxAttempts: Int,
    engines: Set<SearchEngine>,
    timeLimitPerEngine: Duration,
    waitFactor: Double,
) = actor<AbstractUserMessage> {
    val results: MutableMap<SearchEngine, SearchEngineResponseBody?> = mutableMapOf()

    val request = receiveInitialRequest()
    val requestBody = request.body

    val jobs = engines.map { engine ->
        launchEngineJob(
            engine,
            results,
            maxAttempts,
            requestBody,
            timeLimitPerEngine,
            waitFactor
        )
    }

    jobs.joinAll()

    request.deferred.complete(results)
}

@OptIn(ObsoleteCoroutinesApi::class)
private fun ActorScope<AbstractUserMessage>.launchEngineJob(
    engine: SearchEngine,
    results: MutableMap<SearchEngine, SearchEngineResponseBody?>,
    maxAttempts: Int,
    requestBody: SearchEngineRequestBody,
    timeLimitPerEngine: Duration,
    waitFactor: Double,
): Job {
    results[engine] = null
    return launch {
        val attempts = (0 until maxAttempts).map { index ->
            async {
                delay(index * timeLimitPerEngine * waitFactor)
                val engineActor = engine.createActorInScope(this)
                val deferred = CompletableDeferred<SearchEngineResponseBody>()
                val engineRequest = SingleRequest(requestBody, deferred)
                engineActor.send(engineRequest)
                deferred.await().also { engineActor.close() }
            }
        } + async {
            delay(((maxAttempts - 1) * waitFactor + 1) * timeLimitPerEngine)
            null
        }
        results[engine] = select {
            attempts.forEach { deferred -> deferred.onAwait { it } }
        }
        cancel()
    }
}

@OptIn(ObsoleteCoroutinesApi::class)
private suspend fun ActorScope<AbstractUserMessage>.receiveInitialRequest(): AggregatingRequest {
    for (message in channel) {
        when (message) {
            is SingleRequest -> error("The main actor does not support this kind of tasks")
            is AggregatingRequest -> return message
        }
    }
    error("No task received")
}