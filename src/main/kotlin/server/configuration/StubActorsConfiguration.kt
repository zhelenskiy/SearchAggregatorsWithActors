package server.configuration

import actors.ListBasedSimpleSearchEngine
import actors.SearchEngine
import kotlin.time.Duration.Companion.milliseconds

open class StubActorsConfiguration : ActorsConfiguration {
    override val maxTotalSize = 100
    override val maxSitesCount = 5
    override val timeLimitPerEngine = 500.milliseconds
    override val maxAttempts = 5
    override val engines = stubEngines
    override val waitFactor: Double = 0.9
}

private val stubEngines: Set<SearchEngine> = setOf(
    ListBasedSimpleSearchEngine("Alphabet",
        (1..6).associate { count -> "repeat$count.com" to ('a'..'z').joinToString("").repeat(count) }),
    ListBasedSimpleSearchEngine("Scream", mapOf("screamer.ru" to "a".repeat(10))),
    ListBasedSimpleSearchEngine("Digits", mapOf("digits.gov" to (0..9).joinToString(""))),
)