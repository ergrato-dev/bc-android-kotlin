import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

// Una sola instancia de Json para todo el archivo: crearla en cada llamada es lento y el compilador lo avisa
// ("Redundant creation of Json format"). Paso 2: añade la opción para IGNORAR las claves que no conoce.
private val json = Json {
    encodeDefaults = false
}

// Paso 1: hazla @Serializable. encodeReading escribe JSON compacto y OMITE los valores por defecto:
//   Reading("s1", 21.5)      -> {"sensor":"s1","value":21.5}
//   Reading("s1", 21.5, "F") -> {"sensor":"s1","value":21.5,"unit":"F"}
data class Reading(val sensor: String, val value: Double, val unit: String = "C") {
    init {
        require(sensor.isNotBlank()) { "sensor en blanco" }
    }
}

fun encodeReading(r: Reading): String = TODO("Paso 1")

// Paso 2: decodeReading acepta claves de más ({"sensor":"s1","value":3,"extra":true}) y rellena lo que falta con el
// valor por defecto.
fun decodeReading(text: String): Reading = TODO("Paso 2")

// Paso 3: en el JSON la clave de id se llama "sensor_id" (@SerialName) y cache NO se guarda (@Transient; necesita
// valor por defecto):   Sensor("s1", "norte") -> {"sensor_id":"s1","location":"norte"}
data class Sensor(val id: String, val location: String, val cache: String = "")

fun encodeSensor(s: Sensor): String = TODO("Paso 3")

fun decodeSensor(text: String): Sensor = TODO("Paso 3")

// Paso 4: un enum con nombres en minúsculas en el JSON (@SerialName en cada constante) y una value class que se
// escribe como un String suelto, no como un objeto:   Task(Tag("riego"), Priority.HIGH) -> {"tag":"riego","priority":"high"}
enum class Priority { LOW, MEDIUM, HIGH }

@JvmInline
value class Tag(val value: String)

data class Task(val tag: Tag, val priority: Priority)

fun encodeTask(t: Task): String = TODO("Paso 4")

fun decodeTask(text: String): Task = TODO("Paso 4")

// Paso 5: una jerarquía sellada. El JSON lleva un campo "type" con el nombre de cada subtipo (@SerialName):
//   Created(Code "A-1") -> {"type":"created","code":"A-1"}        Cleared -> {"type":"cleared"}
// Pista: para que se escriba el discriminador hay que pasar el tipo base: json.encodeToString<Event>(e).
sealed interface Event {
    data class Created(val code: String) : Event

    data object Cleared : Event
}

fun encodeEvent(e: Event): String = TODO("Paso 5")

fun decodeEvent(text: String): Event = TODO("Paso 5")

// Paso 6: decodeSafely no lanza nunca: Ok(reading) o Invalid(motivo). Los motivos, exactos:
//   falta un campo obligatorio (MissingFieldException)      -> "campo obligatorio ausente"
//   el init de Reading lanza (viene como causa del error)   -> el mensaje de esa causa, p. ej. "sensor en blanco"
//   cualquier otro SerializationException (JSON roto, tipo) -> "JSON inválido"
// MissingFieldException está marcada como experimental: con -Werror pon @OptIn(ExperimentalSerializationApi::class)
// en la función. Ojo: captura primero la más específica.
sealed interface Decoded {
    data class Ok(val reading: Reading) : Decoded

    data class Invalid(val reason: String) : Decoded
}

fun decodeSafely(text: String): Decoded = TODO("Paso 6")

// Paso 7: un serializador propio. La medida viaja como TEXTO con 2 decimales ("12.50") y se lee como Double.
// Usa String.format(Locale.ROOT, "%.2f", value) para escribir y toDouble() para leer.
object Fixed2Serializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Fixed2", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Double): Unit = TODO("Paso 7")

    override fun deserialize(decoder: Decoder): Double = TODO("Paso 7")
}

// Úsalo en la propiedad con @Serializable(with = Fixed2Serializer::class).
data class Measurement(val value: Double)

fun encodeMeasurement(m: Measurement): String = TODO("Paso 7")

fun decodeMeasurement(text: String): Measurement = TODO("Paso 7")

fun main() {
    println(encodeReading(Reading("s1", 21.5)))
    println(encodeEvent(Event.Cleared))
}
