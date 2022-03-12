package actors

import AbstractUserMessage
import AggregatingRequest
import SearchEngineRequestBody
import SearchEngineResponseBody
import SingleRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

interface SearchEngine {
    val name: String
    fun createActorInScope(scope: CoroutineScope): SendChannel<AbstractUserMessage>
}

abstract class AbstractSimpleSearchEngine : SearchEngine {
    abstract suspend fun search(request: SearchEngineRequestBody): SearchEngineResponseBody

    @OptIn(ObsoleteCoroutinesApi::class)
    final override fun createActorInScope(scope: CoroutineScope): SendChannel<AbstractUserMessage> = with(scope) {
        actor {
            for (message in channel) {
                when (message) {
                    is AggregatingRequest -> error("Bad actor type")
                    is SingleRequest -> message.deferred.complete(search(message.body))
                }
            }
        }
    }
}
