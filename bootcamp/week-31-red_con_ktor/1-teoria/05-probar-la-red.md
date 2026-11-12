# Probar la red

> Un test que sale a internet es lento, falla cuando falla la wifi del aula, y no sabe provocar un 503 o un servidor que tarda 16 segundos. `MockEngine` es un motor de Ktor que no abre ninguna conexión: contesta lo que el test le diga, desde el mismo proceso. Con el dispatcher del test, además, los tiempos agotados y las esperas de los reintentos pasan en tiempo virtual. Arriba, el ViewModel y la pantalla se prueban con un repositorio de mentira, sin cliente.

## 🎯 Objetivos

- Contestar con `MockEngine`: JSON, códigos de error, excepciones y respuestas lentas
- Mirar la petición que hizo el cliente: URL, método, cabeceras y cuerpo
- Probar tiempos agotados y reintentos en tiempo virtual
- Probar que la cancelación no se traga
- Probar el ViewModel y la pantalla sin red

## 1. `MockEngine`

```kotlin
val requests = mutableListOf<HttpRequestData>()

val client = bitacoraClient(
    MockEngine { request ->
        requests += request
        respond(
            """{"observations": [{"code": "A-1", "level": "high", "description": "roya"}]}""",
            HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, "application/json"),
        )
    },
    "https://api.bitacora.example/v1/",
)
```

- **El cliente es el de la app**: `bitacoraClient`, con sus plugins. Solo cambia el motor. Por eso la función recibe el motor de fuera (teoría 02).
- **El `Content-Type` de la respuesta** hace falta: `ContentNegotiation` decide por él si sabe leer el cuerpo.

> 🔬 Medido: con `respond("[]")`, sin cabecera, `body<List<Dto>>()` lanza `NoTransformationFoundException`, igual que con `text/plain`. En los tests, una función `json(text)` que pone la cabecera evita el despiste.

Lo que puede contestar:

| Para probar | En el *handler* |
| --- | --- |
| Una respuesta | `respond(texto, HttpStatusCode.OK, headersOf(...))` |
| Un código de error | `respondError(HttpStatusCode.ServiceUnavailable)` |
| Sin red | `throw IOException("sin red")` |
| Un servidor lento | `delay(16_000)` y después `respond(...)` |
| Una respuesta distinta cada vez | Un contador: `if (calls < 3) respondError(...) else respond(...)` |

## 2. Mirar la petición

`HttpRequestData` es la petición tal como llega al motor, con los plugins ya aplicados:

```kotlin
assertEquals("https://api.bitacora.example/v1/observations", requests.single().url.toString())
assertEquals("application/json", requests.single().headers[HttpHeaders.Accept])
assertEquals(HttpMethod.Post, request.method)
assertEquals("""{"place":"Lote 2","min_temp_c":-1.5}""", request.body.toByteArray().decodeToString())
```

Así se prueba lo que no se ve en la respuesta: la URL base y su barra, los parámetros, la clave en la cabecera, los nombres del cuerpo. `toByteArray()` es una extensión de `io.ktor.client.engine.mock` para leer el cuerpo.

## 3. El tiempo virtual

Por defecto, `MockEngine` corre en su propio dispatcher, fuera del control de `runTest`. Un `delay` en el *handler* es un `delay` de verdad.

> 🔬 Medido: con `requestTimeoutMillis = 10_000` y un `delay(20_000)` en el *handler*, el test tarda 13 043 ms de reloj, y el reloj virtual de `runTest` no se mueve.

Con el dispatcher del test en el motor, el `delay` del *handler* y el tiempo máximo de `HttpTimeout` pasan en el reloj virtual:

```kotlin
private fun TestScope.server(answer: suspend MockRequestHandleScope.() -> HttpResponseData) = bitacoraClient(
    MockEngine.create {
        dispatcher = StandardTestDispatcher(testScheduler)
        addHandler { answer() }
    },
    "https://api.bitacora.example/v1/",
)

@Test
fun aSlowServerTimesOut() = runTest {
    assertEquals(RefreshResult.Done(2), repository { delay(14_000); json(two) }.refresh())
    val start = currentTime
    assertEquals(RefreshResult.Failed(NetworkError.TIMEOUT), repository { delay(16_000); json(two) }.refresh())
    assertEquals(REQUEST_TIMEOUT, currentTime - start)
}
```

> 🔬 Medido: con `dispatcher = StandardTestDispatcher(testScheduler)`, el tiempo agotado salta a los 10 000 ms virtuales y el test tarda 106 ms de reloj (6 ms con `UnconfinedTestDispatcher`). Un `delay(9_000)` llega a tiempo, a los 9 000 ms virtuales. Las esperas de `HttpRequestRetry` ya eran virtuales sin tocar el motor: tres intentos con espera exponencial, 3 899 ms virtuales en 16 ms de reloj.

`currentTime` (de `kotlinx.coroutines.test`, con `@OptIn(ExperimentalCoroutinesApi::class)`) dice cuándo ha pasado algo sin esperarlo: que el tiempo agotado es el de la constante, que el segundo reintento llega a los 3 000 ms.

## 4. Probar que la cancelación sigue subiendo

El test obvio no sirve:

```kotlin
val refresh = async { repository.refresh() }
runCurrent()
refresh.cancel()
assertTrue(runCatching { refresh.await() }.exceptionOrNull() is CancellationException)  // pasa siempre
```

> 🔬 Medido en el ejercicio 02: un test así pasa también con un `safely` que convierte la cancelación en `Offline` (`catch (_: Exception)`). `await()` sobre un `Deferred` cancelado lanza `CancellationException` diga lo que diga el código de dentro.

Lo que se rompe al tragarse la cancelación es lo de después. Eso es lo que se mira:

```kotlin
@Test
fun cancellingIsNotAnError() = runTest {
    val repository = repository { delay(5_000); json(two) }
    var after = false
    val refresh = launch {
        repository.refresh()
        after = true
    }
    runCurrent()
    refresh.cancel()
    advanceUntilIdle()
    assertFalse(after)
}
```

`runCurrent()` deja la petición esperando en el `delay` del servidor; `cancel()` la corta, y `advanceUntilIdle()` deja terminar lo que quede. Si `refresh()` se traga la cancelación, devuelve y `after` pasa a `true`.

## 5. El ViewModel y la pantalla, sin cliente

Por encima del repositorio no hace falta ni `MockEngine`. El `FakeRepository` de la semana 28 contesta lo que diga el test:

```kotlin
val answer = MutableStateFlow<RefreshResult?>(RefreshResult.Done(0))

override suspend fun refresh(): RefreshResult {
    refreshes++
    return answer.filterNotNull().first()
}
```

Con `answer.value = null`, la petición se queda esperando: así se prueba lo que pasa *mientras*, que el botón no pide dos veces. Al darle un valor, contesta.

> 🔬 Medido: con el ViewModel creado en una propiedad del test (`private val notes = BitacoraViewModel(...)`), se crea antes del `@Before` que pone `Dispatchers.setMain`, y nada de lo que lanza corre en el momento: `oneRequestAtATime` falla con `expected:<1> but was:<0>`. Con `by lazy`, se crea en el primer uso, ya con el Main de pruebas.

Esperar un efecto que no llega cuelga el test hasta que `runTest` se rinde:

> 🔬 Medido: en el starter, `notes.effect.first()` tarda 65,9 s en fallar (`UncompletedCoroutinesError: After waiting for 1m`). Con `withTimeoutOrNull(1_000) { notes.effect.first() }`, el segundo es virtual: da `null` al momento y el `assertEquals` dice qué faltaba.

En Robolectric, los tests de `testField` con `@HiltAndroidTest` usan `FakeModule` (semana 29), que sustituye a `AppModule` entero: el repositorio es el de mentira, el cliente no existe, y «Actualizar» enseña lo que el test haya puesto en `answer`.

## 6. Y la red de verdad

Con todo esto, ningún test sale del proceso. Lo que no prueban es que el servidor de verdad conteste lo que crees. Eso es un *contract test*, o una prueba a mano con el *Network Inspector* de Android Studio contra el servidor de pruebas. Retrofit, por su parte, se prueba con `MockWebServer` de OkHttp, que sí abre un puerto local: más cerca de la realidad y más lento.

## Para recordar

- `MockEngine` con el cliente de la app: solo cambia el motor. La respuesta lleva su `Content-Type`.
- Guarda cada `HttpRequestData` y mira URL, método, cabeceras y cuerpo.
- `dispatcher = StandardTestDispatcher(testScheduler)` en el motor: los tiempos agotados en tiempo virtual, y `currentTime` para saber cuándo.
- La cancelación se prueba mirando si el código de después se ejecutó, no con `await()`.
- Arriba del repositorio, un `FakeRepository`: un `MutableStateFlow` con `null` es una petición que aún no ha vuelto.
