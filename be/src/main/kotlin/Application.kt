import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    routing {
        webSocket("/chat") {
            for(frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                println(receivedText)
                send(receivedText)
            }
        }
    }
}
