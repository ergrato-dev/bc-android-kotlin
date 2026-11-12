# Ejercicio 01 — El parte del tiempo

⏱️ 2 h · Teoría previa: 01, 02 y 05

## Objetivo

Antes de fumigar hay que saber si va a llover, y antes de una helada hay que avisar. Un servicio del tiempo (`https://api.tiempo.example/v2/`) da el parte por horas de un punto y recibe avisos. Al terminar, el cliente lee su JSON con sus nombres, tolera los campos que faltan y los que sobran, manda la clave en una cabecera y los parámetros en la URL, y publica un aviso en JSON. Todo en la JVM: `MockEngine` hace de servidor.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con Ktor client 3.6.1 (`ktor-client-core`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`), kotlinx-serialization 1.12.0 y, en los tests, `ktor-client-mock` y `kotlinx-coroutines-test` 1.11.0. No hay motor en `main`: el cliente lo recibe de fuera. En `Tiempo.kt` están las clases del servidor, el cliente y `ForecastApi`, con comentarios `Paso N` donde falta algo. Compila.

Los tests (`src/test/kotlin/dev/bitacora/TiempoTest.kt`, no los toques) crean el cliente con un `MockEngine` que guarda cada petición y contesta un parte de tres horas. Al empezar, fallan 8 de 8.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. Todos fallan con el mismo error: ¿cuál, y qué le falta al cliente?
2. **Los nombres del servidor**: en `Hour`, `@SerialName` para `temp_c`, `rain_mm` y `wind_kmh`; `wind` puede no venir (`= null`). En `Report`, `min_temp_c` (teoría 02, apartado 2).
3. **El cliente** (`forecastClient`): `expectSuccess = true`, `ContentNegotiation` con `json(Json { ignoreUnknownKeys = true })`, y `defaultRequest` con `BASE_URL` y la cabecera `X-Api-Key` (apartado 1 y 4).
4. **El parte**: en `forecast`, `parameter("lat", ...)`, `parameter("lon", ...)` y `parameter("hours", ...)` (apartado 3).
5. **El aviso**: en `report`, `contentType(ContentType.Application.Json)` y `setBody(report)`.
6. **Lo que dice el parte**: `rainyHours` (las horas con al menos `threshold` mm) y `minTemperature` (la mínima, o `null` sin horas).
7. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
8. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `ignoreUnknownKeys`. ¿Qué tests fallan, con qué excepción y qué mensaje? ¿Qué campo la provoca? Restáuralo.
   - (b) Quita la `/` final de `BASE_URL`. ¿Qué URL sale en `theQueryGoesInTheUrl`? ¿Y con `BASE_URL` bien y `get("/forecast")`? Restáuralo.
   - (c) Quita `contentType(...)` de `report`. ¿Qué excepción da `aReportIsPostedAsJson`, y qué te sugiere el mensaje? Restáuralo.
   - (d) ¿Por qué la clave va en una cabecera y no en la URL (`?key=...`)? ¿Por qué no está escrita en `Tiempo.kt`? ¿Basta eso para que nadie la saque del APK?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `NoTransformationFoundException: Expected response body of the type 'class dev.bitacora.Forecast ...` | Falta `ContentNegotiation` con `json(...)` en el cliente (paso 3) |
| `Field 'temperature' is required ... but it was missing` | La propiedad se llama `temperature` y el JSON, `temp_c`: `@SerialName("temp_c")` |
| `Field 'wind_kmh' is required` | `Double?` no basta: `= null` |
| `Encountered an unknown key 'generated'` | `ignoreUnknownKeys = true` |
| `theKeyGoesInAHeader` da `null` | La cabecera va en `defaultRequest { header("X-Api-Key", apiKey) }` |
| La URL sale sin `/v2/` | La URL base sin `/` final, o una ruta con `/` delante |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 8.
