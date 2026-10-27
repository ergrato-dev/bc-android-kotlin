import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Visit(val code: String, val observations: Int)

fun main() = runBlocking {
    println(Json.encodeToString(Visit("A-1", 3)))
}
