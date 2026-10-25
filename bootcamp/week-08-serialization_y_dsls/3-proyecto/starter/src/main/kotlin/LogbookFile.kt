import kotlinx.serialization.json.Json

const val FORMAT_VERSION = 1

// El archivo que se guarda y se comparte. `counts` es un resumen para quien lo lee a ojo: al importar se ignora.
// Paso 1: @Serializable. Formato exacto de una bitácora con una observación:
// {"version":1,"domain":"agro","observations":[{"code":"A-1","description":"Riego","quantity":3,"measure":12.5}],"counts":{"low":0,"medium":1,"high":0}}
data class LogbookFile(
    val version: Int = FORMAT_VERSION,
    val domain: String,
    val observations: List<Observation>,
    val counts: Map<Level, Int> = emptyMap(),
)

// Paso 2: una sola instancia de Json. Ignora claves desconocidas y ESCRIBE los valores por defecto (así `version` y
// `counts` siempre salen en el archivo). Compacto: sin prettyPrint.
val logbookJson: Json = Json {
    encodeDefaults = false
}

// Para mostrar por pantalla: la misma configuración, con sangría.
val prettyJson: Json = Json(logbookJson) { prettyPrint = true }

// Paso 3: el archivo como texto JSON: compacto con logbookJson, o con sangría (prettyJson) si pretty es true.
fun encodeFile(file: LogbookFile, pretty: Boolean = false): String = TODO("Paso 3")
