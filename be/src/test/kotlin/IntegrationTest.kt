import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.netty.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test


class IntegrationTest {
    @Test
    fun `client server conversation`() {
        GlobalScope.launch { EngineMain.main(emptyArray()) }

        Thread.sleep(10000)
        val client1 = HttpClient {
            install(WebSockets)
        }
        val client2 = HttpClient {
            install(WebSockets)
        }
        runBlocking {
            launch {
                client1.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/chat") {
                    val received = arrayListOf<String>()
                    val textMessages = listOf("HELLO", "WORLD")
                    for (msg in textMessages) {
                        outgoing.send(Frame.Text(msg))
                        received.add((incoming.receive() as Frame.Text).readText())
                    }
                    assertEquals(listOf("HELLO", "WORLD"), received)
                }
            }
            launch {
                client2.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/chat") {
                    val received = arrayListOf<String>()
                    while (received != listOf("HELLO", "WORLD")) {
                        received.add((incoming.receive() as Frame.Text).readText())
                    }
                }
            }
        }
        client1.close()
        client2.close()
    }
}