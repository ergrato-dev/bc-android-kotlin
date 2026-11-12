# Ejercicio 02 — Cuando la red falla

⏱️ 2 h · Teoría previa: 03 y 05

## Objetivo

En el campo, la estación del tiempo de la finca contesta cuando quiere. Al terminar, cada llamada a `StationApi` devuelve un `Answer`: el valor, o uno de los problemas que la app sabe tratar (sin red, tiempo agotado, un código de error, una respuesta ilegible). Ninguna petición espera más de `TIMEOUT`, un servidor ocupado (5xx) se reintenta `RETRIES` veces con espera exponencial, un 404 no se reintenta, y cerrar la pantalla cancela sin convertirse en un error. Todo en tiempo virtual: el test de un servidor que tarda 11 s dura milisegundos.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con las mismas dependencias que el ejercicio 01. En `Red.kt` están `Station`, `Answer` (ya hecho), `stationClient`, `safely` y `StationApi`, con comentarios `Paso N` donde falta algo. Compila.

Los tests (`src/test/kotlin/dev/bitacora/RedTest.kt`, no los toques) crean el `MockEngine` con el dispatcher del test (`dispatcher = StandardTestDispatcher(testScheduler)`) y apuntan en `calls` el instante virtual de cada llamada. Al empezar, fallan 7 de 9.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué `aSlowServerIsATimeout` falla rápido, si el servidor tarda 11 s? ¿Qué devuelve `station` ahora con un 404?
2. **Errores con nombre** (`safely`): un `catch` por problema. `HttpRequestTimeoutException` da `Timeout`; `IOException`, `Offline`; `ResponseException`, `Http(e.response.status.value)`; `ContentConvertException`, `Unreadable`. Nada más: lo demás, y la cancelación, siguen subiendo (teoría 03, apartados 3 y 4).
3. **Un tiempo máximo**: `HttpTimeout` con `requestTimeoutMillis = TIMEOUT` (apartado 2).
4. **Reintentos**: `HttpRequestRetry` con `retryOnServerErrors(maxRetries = RETRIES)` y `exponentialDelay(randomizationMs = 0)` (apartado 5).
5. Ejecuta `./gradlew test` (9 de 9) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Pon el `catch (IOException)` antes del de `HttpRequestTimeoutException`. ¿Qué test falla y qué devuelve? ¿Avisa el compilador? Restáuralo.
   - (b) En `withoutNetworkItIsOffline`, ¿cuántas llamadas hace el cliente? (Imprime `calls.size`.) ¿Por qué, si solo has pedido `retryOnServerErrors`? ¿Y un servidor lento, se reintenta?
   - (c) Quita `randomizationMs = 0`. ¿Qué test falla, y por qué en la app de verdad sí conviene esa parte al azar?
   - (d) Cambia el último `catch` por `catch (_: Exception) { Answer.Unreadable }`. ¿Qué test falla? ¿Qué le pasaría a una pantalla cerrada a mitad de petición? ¿Por qué el test no usa `async` y `await()` para comprobarlo? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `aSlowServerIsATimeout` da `Offline` | El `catch (IOException)` va antes que el de `HttpRequestTimeoutException`: los tiempos agotados también son `IOException` |
| `eachRetryWaitsLonger` da instantes distintos en cada ejecución | Falta `randomizationMs = 0` |
| `notFoundIsNotRetried` lanza `ClientRequestException` | `safely` no tiene el `catch (e: ResponseException)` |
| Un test tarda 60 s y acaba en `UncompletedCoroutinesError` | Algo espera de verdad: ¿has creado otro `MockEngine` sin el dispatcher del test? |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.
