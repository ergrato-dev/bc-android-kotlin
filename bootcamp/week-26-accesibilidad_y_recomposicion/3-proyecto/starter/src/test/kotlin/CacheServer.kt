import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// Ya está hecho: un servidor de caché HTTP mínimo, con el servidor que trae el JDK (com.sun.net.httpserver).
// Guarda las entradas en memoria: PUT /cache/<clave> sube, GET /cache/<clave> baja (404 si no existe).
// Con usuario y contraseña, responde 401 a quien no los mande. Cuenta lo que pasa para que los tests lo comprueben.
class CacheServer(user: String? = null, password: String? = null) : AutoCloseable {
    private val entries = ConcurrentHashMap<String, ByteArray>()
    private val expected = if (user == null) null else "Basic " + Base64.getEncoder().encodeToString("$user:$password".toByteArray())
    val stores = AtomicInteger()
    val hits = AtomicInteger()
    val misses = AtomicInteger()
    val rejected = AtomicInteger()

    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/cache/") { exchange ->
            exchange.use {
                val key = it.requestURI.path.removePrefix("/cache/")
                if (expected != null && it.requestHeaders.getFirst("Authorization") != expected) {
                    rejected.incrementAndGet()
                    it.responseHeaders.add("WWW-Authenticate", "Basic realm=\"cache\"")
                    it.sendResponseHeaders(401, -1)
                    return@use
                }
                when (it.requestMethod) {
                    "PUT" -> {
                        entries[key] = it.requestBody.readBytes()
                        stores.incrementAndGet()
                        it.sendResponseHeaders(200, -1)
                    }
                    "GET" -> {
                        val body = entries[key]
                        if (body == null) {
                            misses.incrementAndGet()
                            it.sendResponseHeaders(404, -1)
                        } else {
                            hits.incrementAndGet()
                            it.sendResponseHeaders(200, body.size.toLong())
                            it.responseBody.write(body)
                        }
                    }
                    else -> it.sendResponseHeaders(405, -1)
                }
            }
        }
        start()
    }

    val url = "http://127.0.0.1:${server.address.port}/cache/"

    override fun close() = server.stop(0)
}
