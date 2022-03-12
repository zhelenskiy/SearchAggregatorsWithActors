import actors.SearchEngine
import actors.makeMainActor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration

suspend fun aggregateResponses(
    request: SearchEngineRequestBody,
    timeLimitPerEngine: Duration,
    maxAttempts: Int,
    engines: Set<SearchEngine>,
    waitFactor: Double,
): Map<SearchEngine, SearchEngineResponseBody?> = coroutineScope {
    require(maxAttempts > 0)
    val mainActor = makeMainActor(maxAttempts, engines, timeLimitPerEngine, waitFactor)
    val requestObject = AggregatingRequest(request, CompletableDeferred())
    mainActor.send(requestObject)
    return@coroutineScope requestObject.deferred.await().also { mainActor.close() }
}