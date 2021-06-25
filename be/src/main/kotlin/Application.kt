import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    Repository()
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            val thisConnection = Connection(this)
            connections += thisConnection
            for(frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                println(receivedText)
                connections.forEach {
                    it.session.send(receivedText)
                }
            }
        }
    }
}

class Repository {
    companion object {
        val messages = mutableListOf<String>()
    }
}

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
}