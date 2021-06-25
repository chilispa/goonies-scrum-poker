import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.netty.*
import io.ktor.util.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*

import org.junit.Test


class IntegrationTest {
    @Test
    fun `client server conversation`() {
        GlobalScope.async {EngineMain.main(emptyArray())}

        Thread.sleep(10000)
        val client = HttpClient {
            install(WebSockets)
        }
        runBlocking {
            client.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/chat") {
                val received = arrayListOf<String>()
                val textMessages = listOf("HELLO", "WORLD")
                for (msg in textMessages) {
                    outgoing.send(Frame.Text(msg))
                    received.add((incoming.receive() as Frame.Text).readText())
                }
                assertEquals(listOf("HELLO", "WORLD"), received)
            }
        }
        client.close()
    }
}