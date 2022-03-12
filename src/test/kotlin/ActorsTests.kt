
import actors.AbstractSimpleSearchEngine
import actors.SearchEngine
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import server.configuration.StubActorsConfiguration
import server.module
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ActorsTests {

    private fun String.unify() = replace(Regex("\\s+"), " ").trim()
    private fun textFromResource(filename: String) = ActorsTests::class.java.getResource(filename)!!.readText()

    @Test
    fun integrationTest() {
        val actorsConfiguration = StubActorsConfiguration()
        withTestApplication({ module(actorsConfiguration) }) {
            handleRequest(HttpMethod.Get, "/a").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(textFromResource("TestA.html").unify(), response.content?.unify())
            }
        }
    }

    @Test
    fun delayedTest() {
        val delayTime = 300.milliseconds
        val started = AtomicInteger(0)
        val finished = AtomicInteger(0)
        val startTime = Date().time
        val actorsConfiguration = object : StubActorsConfiguration() {
            override val timeLimitPerEngine: Duration = 120.milliseconds
            override val maxAttempts: Int = 5
            override val engines: Set<SearchEngine>  = super.engines.map(::transformEngine).toSet()

            private fun transformEngine(old: SearchEngine) = object : AbstractSimpleSearchEngine() {
                override val name: String = old.name
                override suspend fun search(request: SearchEngineRequestBody): SearchEngineResponseBody {
                    started.incrementAndGet()
                    println("started $name: ${Date().time - startTime}")
                    delay(delayTime)
                    println("finished $name: ${Date().time - startTime}")
                    return (old as AbstractSimpleSearchEngine).search(request).also { finished.incrementAndGet() }
                }
            }
        }
        withTestApplication({ module(actorsConfiguration) }) {
            handleRequest(HttpMethod.Get, "/a").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(textFromResource("TestA.html").unify(), response.content?.unify())
                assertEquals(actorsConfiguration.engines.size * 3, started.get())
                assertEquals(actorsConfiguration.engines.size, finished.get())
            }
        }
    }

    @Test
    fun noResponseTest() {
        val actorsConfiguration = object : StubActorsConfiguration() {
            override val engines: Set<SearchEngine> = setOf(object : AbstractSimpleSearchEngine() {
                override val name: String = "Not responding engine"
                override suspend fun search(request: SearchEngineRequestBody): SearchEngineResponseBody {
                    suspendCancellableCoroutine<Nothing> {  }
                }
            })
        }
        withTestApplication({ module(actorsConfiguration) }) {
            handleRequest(HttpMethod.Get, "/a").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(textFromResource("NoResponse.html").unify(), response.content?.unify())
            }
        }
    }
}