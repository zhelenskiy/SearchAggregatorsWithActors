package actors

import SearchEngineRequestBody
import SearchEngineResponseBody
import SearchEngineResponseBody.Item

class ListBasedSimpleSearchEngine(override val name: String, private val everything: Map<String, String>) : AbstractSimpleSearchEngine() {

    override suspend fun search(request: SearchEngineRequestBody): SearchEngineResponseBody = everything
        .mapValues { (_, source) -> source.split(request.text) }
        .toList()
        .filter { (_, parts) -> parts.size > 1 }
        .take(request.maxSitesCount)
        .sortedByDescending { it.second.size }
        .map { (site, parts) -> makeItem(parts, request, site) }
        .let(::SearchEngineResponseBody)

    private fun makeItem(
        parts: List<String>,
        request: SearchEngineRequestBody,
        site: String
    ): Item {
        var summaryLength = minOf(parts.first().length, request.maxTotalSize)
        var isTruncated = summaryLength < parts.first().length
        val limitedParts = listOf(parts.first().take(request.maxTotalSize)) + parts.drop(1).takeWhile {
            if (summaryLength + request.text.length + it.length <= request.maxTotalSize) {
                summaryLength += request.text.length + it.length
                true
            } else false.also { isTruncated = true }
        }
        return Item(site, parts.size - 1, limitedParts, isTruncated)
    }
}