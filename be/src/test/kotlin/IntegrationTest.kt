import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.junit.Test

class IntegrationTest {
    @Test
    fun testConversation() {
        withTestApplication {
            application.install(WebSockets)

            val received = arrayListOf<String>()
            application.routing {
                webSocket("/echo") {
                    try {
                        while (true) {
                            val text = (incoming.receive() as Frame.Text).readText()
                            received += text
                            outgoing.send(Frame.Text(text))
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // Do nothing!
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }

            handleWebSocketConversation("/echo") { incoming, outgoing ->
                val textMessages = listOf("HELLO", "WORLD")
                for (msg in textMessages) {
                    outgoing.send(Frame.Text(msg))
                    assertEquals(msg, (incoming.receive() as Frame.Text).readText())
                }
                assertEquals(textMessages, received)
            }
        }
    }
}