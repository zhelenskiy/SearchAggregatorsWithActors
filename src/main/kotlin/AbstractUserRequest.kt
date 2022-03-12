import actors.SearchEngine
import kotlinx.coroutines.CompletableDeferred

sealed interface AbstractUserMessage

data class SearchEngineRequestBody(val text: String, val maxTotalSize: Int, val maxSitesCount: Int)
data class SearchEngineResponseBody(val accuracies: List<Item>) {
    data class Item(val url: String, val found: Int, val splittedSource: List<String>, val isTruncated: Boolean)
}

class SingleRequest(val body: SearchEngineRequestBody, val deferred: CompletableDeferred<SearchEngineResponseBody>) :
    AbstractUserMessage

class AggregatingRequest(
    val body: SearchEngineRequestBody,
    val deferred: CompletableDeferred<Map<SearchEngine, SearchEngineResponseBody?>>
) : AbstractUserMessage