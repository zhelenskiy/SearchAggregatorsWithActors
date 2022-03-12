package server.configuration

import actors.SearchEngine
import kotlin.time.Duration

interface ActorsConfiguration {
    val maxTotalSize: Int
    val maxSitesCount: Int
    val timeLimitPerEngine: Duration
    val maxAttempts: Int
    val engines: Set<SearchEngine>
    val waitFactor: Double
}