# El cliente y el JSON

> Un cliente de Ktor se configura una vez y se usa en toda la app. Se le dice cómo leer y escribir JSON, a qué servidor hablar y qué cabeceras llevan todas las peticiones. El JSON se lee con clases `@Serializable` que tienen la forma del servidor, no la de la app: con sus nombres, con los campos que pueden faltar, y sabiendo que mañana el servidor puede mandar uno que hoy no existe.

## 🎯 Objetivos

- Configurar un `HttpClient` con `ContentNegotiation` y `defaultRequest`
- Escribir clases `@Serializable` para la respuesta de un servidor: `@SerialName`, campos opcionales y campos desconocidos
- Hacer un GET con parámetros y un POST con un cuerpo JSON
- Saber qué dice Ktor cuando el JSON no encaja

## 1. Configurar el cliente

```kotlin
fun forecastClient(engine: HttpClientEngine, apiKey: String): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    defaultRequest {
        url("https://api.tiempo.example/v2/")
        header("X-Api-Key", apiKey)
    }
}
```

- **El motor llega de fuera.** Así la misma función sirve con OkHttp en la app y con `MockEngine` en los tests.
- **`ContentNegotiation`** convierte el cuerpo en objetos y los objetos en cuerpo, con kotlinx-serialization (el mismo de la semana 28). También pone `Accept: application/json` en cada petición.
- **`defaultRequest`** es lo que llevan todas: la URL base, una cabecera.
- **`expectSuccess`** convierte los 4xx y 5xx en excepciones (teoría 03).

> 🔬 Medido: con `ContentNegotiation`, la petición sale con `Accept: application/json`; sin él, con `Accept: */*`.

**La barra del final importa.** La ruta se resuelve como un enlace relativo, igual que en un navegador:

| URL base | Ruta | Sale |
| --- | --- | --- |
| `https://api.bitacora.example/v1/` | `observations` | `https://api.bitacora.example/v1/observations` |
| `https://api.bitacora.example/v1` | `observations` | `https://api.bitacora.example/observations` |
| `https://api.bitacora.example/v1/` | `/observations` | `https://api.bitacora.example/observations` |

> 🔬 Medido con Ktor 3.6.1 y `MockEngine`: las tres URL de la tabla son las que llegan al motor. Sin la barra final, o con una barra delante de la ruta, el `v1` desaparece y el servidor contesta 404.

**Un cliente para toda la app.** Cada `HttpClient` tiene su motor, y el de OkHttp guarda las conexiones abiertas para reutilizarlas. Crear uno por petición tira esa reserva cada vez. En la Bitácora es un `@Singleton` (teoría 04).

## 2. Las clases del servidor

El servidor del tiempo manda esto:

```json
{
  "place": "Lote 2",
  "generated": "2026-11-11T05:00",
  "hourly": [
    {"time": "06:00", "temp_c": 3.5, "rain_mm": 0.0, "wind_kmh": 12.0},
    {"time": "07:00", "temp_c": 1.0, "rain_mm": 0.8}
  ]
}
```

```kotlin
@Serializable
data class Hour(
    val time: String,
    @SerialName("temp_c") val temperature: Double,
    @SerialName("rain_mm") val rain: Double,
    @SerialName("wind_kmh") val wind: Double? = null,
)

@Serializable
data class Forecast(val place: String, val hourly: List<Hour>)
```

- **`@SerialName`**: el nombre en el JSON es del servidor (`temp_c`); el de la propiedad, tuyo.
- **Un campo que puede no venir** necesita un valor por defecto. `Double?` sin `= null` no basta: kotlinx-serialization pide el campo igual.
- **Un campo que no está en la clase** (`generated`) rompe la lectura, salvo con `ignoreUnknownKeys = true`.

> 🔬 Medido: sin `ignoreUnknownKeys`, `JsonConvertException: Illegal input: Unexpected JSON token at offset 31: Encountered an unknown key 'extra' at path: $[0]`. Con `val wind: Double?` sin el `= null`, y sin `wind` en el JSON, `MissingFieldException: Field 'wind' is required for type with serial name 'N', but it was missing at path: $` (en Ktor, dentro de un `JsonConvertException` que empieza por `Illegal input:`). Con `null` en un campo que no lo admite, `Expected string literal but 'null' literal was found at path: $[0].level`, y el mensaje sugiere `coerceInputValues = true`. Ktor envuelve los tres en `JsonConvertException`.

`ignoreUnknownKeys` no es descuido: es lo que deja al servidor añadir un campo sin romper las apps que ya están instaladas, y que no se van a actualizar todas el mismo día. Lo contrario, quitar o renombrar un campo, sí rompe: por eso las API llevan versión en la URL (`/v1/`).

**Lo que la app no conoce.** Un `enum` en la clase del servidor ata la app a los valores de hoy: si mañana llega `"urgent"`, falla la lista entera. En la Bitácora, el nivel llega como `String` y se traduce a `Level` al pasar al dominio; un nivel desconocido salta esa observación, no las demás (teoría 04).

## 3. GET con parámetros, POST con cuerpo

```kotlin
suspend fun forecast(latitude: Double, longitude: Double, hours: Int): Forecast = client.get("forecast") {
    parameter("lat", latitude)
    parameter("lon", longitude)
    parameter("hours", hours)
}.body()
```

> 🔬 Medido: la petición sale a `https://api.tiempo.example/v2/forecast?lat=4.6&lon=-74.08&hours=3`. `parameter` codifica cada valor; no hace falta montar la cadena a mano.

```kotlin
@Serializable
data class Report(val place: String, @SerialName("min_temp_c") val minTemperature: Double)

suspend fun report(report: Report): Long = client.post("reports") {
    contentType(ContentType.Application.Json)
    setBody(report)
}.body<Created>().id
```

`setBody` con un objeto necesita saber en qué formato mandarlo: eso dice `contentType`.

> 🔬 Medido: sin `contentType`, `IllegalStateException: Fail to prepare request body for sending. The body type is: class dev.bitacora.Report ..., with Content-Type: null`. Con él, el cuerpo es `{"place":"Lote 2","min_temp_c":-1.5}`, con el nombre del servidor.

## 4. La clave del servicio

Muchos servicios piden una clave. Va en una cabecera (`X-Api-Key`, `Authorization`), no en la URL: las URL acaban en registros, en el historial y en los informes de errores. Y no va en el código: se lee de una propiedad que no se sube al repositorio, como la de Maps en la semana 27. Aun así, una clave dentro de un APK se puede sacar del APK. Para lo que de verdad hay que proteger, la clave vive en un servidor propio, y la app habla con ese servidor.

## Para recordar

- Un cliente por app, con el motor de fuera. `ContentNegotiation` para el JSON, `defaultRequest` para la URL base y las cabeceras de todas.
- La URL base acaba en `/` y las rutas no empiezan por `/`.
- Las clases del servidor tienen sus nombres (`@SerialName`), un valor por defecto en lo que puede faltar, e `ignoreUnknownKeys` para lo que pueda llegar.
- `parameter` para la URL; `contentType` y `setBody` para el cuerpo. La clave, en una cabecera y fuera del código.
