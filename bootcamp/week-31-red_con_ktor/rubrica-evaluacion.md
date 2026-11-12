# Rúbrica de Evaluación — Semana 31: Red con Ktor client

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: las observaciones del equipo |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué tres cosas pide Android antes de la primera petición HTTP? ¿Por qué la Bitácora declara `INTERNET` si el APK ya lo tenía? | 10 |
| 2 | ¿Qué es un motor de Ktor y por qué `bitacoraClient` lo recibe de fuera? ¿Qué motor usa la app y cuál los tests? ¿Qué cambia entre Ktor client y Retrofit, y qué no cambia? | 10 |
| 3 | ¿Qué hacen `ContentNegotiation`, `defaultRequest` y `expectSuccess`? ¿Qué pasa con la URL si la base no acaba en `/`, o si la ruta empieza por `/`? | 10 |
| 4 | En una clase `@Serializable` del servidor, ¿para qué sirve `@SerialName`? ¿Por qué `Double?` no basta para un campo que puede faltar? ¿Por qué `ignoreUnknownKeys = true`, y por qué el nivel llega como `String` y no como `Level`? | 10 |
| 5 | ¿Qué excepción da cada fallo: sin red, tiempo agotado, un 404, un 503, un JSON que no encaja? ¿Cuáles son `IOException`, y qué consecuencia tiene eso en el orden del `when`? | 10 |
| 6 | ¿Qué mide cada uno de los tres tiempos de `HttpTimeout`? ¿Cuál es el que ve la persona? ¿Qué pasa sin `HttpTimeout`? | 10 |
| 7 | ¿Por qué no se captura la cancelación? ¿Qué pasaría en la Bitácora si `refresh()` la convirtiese en `OFFLINE`? ¿Por qué un test con `async` y `await()` no lo detecta, y cómo se prueba? | 10 |
| 8 | ¿Qué se reintenta y qué no? ¿Para qué sirven la espera exponencial y la parte al azar? ¿Por qué la Bitácora no reintenta en el cliente? | 10 |
| 9 | ¿Por qué `RemoteObservation`, `ObservationEntity` y `Observation` son tres clases? ¿Por qué `refresh()` escribe en Room y devuelve un `RefreshResult` en lugar de la lista? ¿Qué regla sigue con los códigos repetidos, y qué techo tiene? | 10 |
| 10 | ¿Cómo hace `MockEngine` que un servidor de 16 s tarde milisegundos en el test? ¿Qué pasa sin el dispatcher del test en el motor? ¿Cómo prueba la pantalla «Actualizar» sin cliente? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. El permiso `INTERNET` en el manifiesto (normal: no se pide a la persona), no bloquear el hilo principal (`NetworkOnMainThreadException`; con Ktor, `suspend`), y HTTPS: el texto en claro está cerrado desde Android 9 salvo con un `network_security_config` para un dominio. El APK lo tenía porque lo mete `play-services-maps`; la red es de la app, y si se quita el mapa, el permiso no puede irse con él. El manifiesto unido no cambia.
2. El motor abre las conexiones; el `HttpClient` y los plugins son los mismos con cualquiera. Recibirlo de fuera deja usar OkHttp en la app (`ktor-client-okhttp`, en `app`) y `MockEngine` en los tests, con el mismo cliente; `data` solo depende de `ktor-client-core`. Retrofit describe la API con una interfaz anotada que implementa al ejecutar, usa siempre OkHttp y solo corre en Android y la JVM; Ktor la describe con código y es multiplataforma. No cambian las decisiones de HTTP: códigos, tiempos, reintentos, errores.
3. `ContentNegotiation` convierte entre JSON y objetos (y pone `Accept: application/json`); `defaultRequest`, lo que llevan todas las peticiones (URL base, cabeceras); `expectSuccess`, que un código que no es 2xx lance una `ResponseException`. La ruta se resuelve como un enlace relativo: sin `/` al final de la base, o con `/` delante de la ruta, se pierde el `v1/` (`https://api.bitacora.example/observations`), y el servidor contestaría 404.
4. `@SerialName` separa el nombre del JSON (`lat`, `temp_c`) del de la propiedad. `Double?` dice que puede ser `null`, pero kotlinx-serialization pide el campo igual (`MissingFieldException: Field 'wind' is required`); hace falta `= null`. `ignoreUnknownKeys` deja que el servidor añada un campo sin romper las apps instaladas. Un `enum` en la clase del servidor rompería la lista entera con un nivel nuevo (`"urgent"`); como `String`, la traducción salta solo esa observación.
5. Sin red: `ConnectException` u otra `IOException`. Tiempo agotado: `HttpRequestTimeoutException`, `ConnectTimeoutException` o `SocketTimeoutException`. 404: `ClientRequestException`; 503: `ServerResponseException` (con `expectSuccess`; las dos son `ResponseException`). JSON que no encaja: `JsonConvertException` (`ContentConvertException`), y sin `Content-Type` JSON, `NoTransformationFoundException`. Los de red y los tiempos agotados son `IOException`: los tiempos van antes en el `when`, o salen como `OFFLINE`, sin aviso del compilador.
6. `connectTimeoutMillis`: hasta que el servidor acepta la conexión. `socketTimeoutMillis`: el silencio máximo entre dos trozos. `requestTimeoutMillis`: la petición entera, que es lo que espera la persona. Ktor no pone ninguno: sin `HttpTimeout`, lo que decida el motor; con `MockEngine`, para siempre (el test acaba en `UncompletedCoroutinesError` al minuto).
7. La cancelación es como termina una corrutina: tiene que seguir subiendo. Si `refresh()` la convirtiese en `OFFLINE`, el ViewModel seguiría con `onIntent(Refreshed(...))` en una sesión cerrada, como si nada. `await()` sobre un `Deferred` cancelado lanza `CancellationException` aunque el código de dentro la haya tragado: el test pasa igual. Se prueba con `launch`, una variable que se pone después de `refresh()`, `runCurrent()`, `cancel()`, `advanceUntilIdle()` y `assertFalse`.
8. Los 5xx y los cortes, que pueden salir bien después; los 4xx no (la petición está mal), y en Ktor tampoco los tiempos agotados por defecto. La espera exponencial no machaca a un servidor que ya va mal; la parte al azar evita que muchos móviles reintenten a la vez. La Bitácora no reintenta: la persona ve el error al momento y puede volver a tocar; reintentar sin nadie delante, cuando vuelva la red, es de WorkManager (semana 32).
9. Cambian por motivos distintos: el servidor, la tabla y el dominio. Separadas, el servidor puede renombrar un campo sin migración, y la tabla añadir una columna sin tocar la API. Room es la fuente de verdad: lo que llega entra por `upsert` y la pantalla lo ve por el `Flow`, como todo; `RefreshResult` dice qué ha pasado, para el aviso. El servidor gana: la A-1 del servidor pisa a la de aquí, y la que solo está aquí se queda. El techo: un cambio local aún no subido se pierde (semana 32).
10. Con `dispatcher = StandardTestDispatcher(testScheduler)` en `MockEngine.create`, el `delay` del *handler* y el tiempo máximo de `HttpTimeout` usan el reloj virtual de `runTest`: el tiempo agotado salta a los 15 000 ms virtuales, en milisegundos de reloj, y `currentTime` lo comprueba. Sin él, el `delay` es real: 13 s de reloj para un tiempo máximo de 10 s. La pantalla se prueba con `FakeModule`, que sustituye a `AppModule` entero: el `FakeRepository` contesta lo que diga `answer`, y con `null` la petición se queda esperando.

**Ejercicio 01, paso 8**: (a) Los seis que leen el parte (`theForecastIsRead`, `aMissingFieldIsNull`, `unknownFieldsAreIgnored`, `theQueryGoesInTheUrl`, `theKeyGoesInAHeader`, `theRainyHoursAreTheWetOnes`), con `JsonConvertException: Illegal input: Unexpected JSON token at offset 51: Encountered an unknown key 'generated'`. Los dos del aviso pasan: `{"id": 41}` no tiene campos de más. (b) `https://api.tiempo.example/forecast?lat=4.6&lon=-74.08&hours=3`, sin `v2`, en los dos casos. (c) `IllegalStateException: Fail to prepare request body for sending. The body type is: class dev.bitacora.Report ..., with Content-Type: null`, y sugiere instalar `ContentNegotiation` y poner `Content-Type`. (d) Las URL acaban en registros, historiales e informes de errores; la clave se lee de una propiedad fuera del repositorio. No basta: lo que va en un APK se puede sacar del APK; lo que hay que proteger de verdad se queda en un servidor propio.

**Ejercicio 02, paso 6**: (a) `aSlowServerIsATimeout`: `expected: <Timeout> but was: <Offline>`. El compilador no avisa. (b) Tres: el plugin trae de serie el reintento por excepciones, y `retryOnServerErrors` solo cambia el de las respuestas. Un servidor lento no: `retryOnTimeout` es `false` por defecto. (c) `eachRetryWaitsLonger`, con instantes distintos cada vez (por ejemplo `[0, 1367, 3940]`). En la app, mil móviles que perdieron la red a la vez no reintentan en el mismo milisegundo. (d) `cancellingIsNotAnAnswer`: el código de después de `station(...)` se ejecuta en una corrutina cancelada. Una pantalla cerrada seguiría pintando o guardando. Con `await()`, un `Deferred` cancelado lanza `CancellationException` aunque `safely` la haya tragado, y el test pasaría.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El parte del tiempo (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `@SerialName` en los nombres del servidor, `= null` en lo que puede faltar, la clave en `defaultRequest` y no en el código, `parameter` y `setBody` con `contentType`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 8 explicado, con los mensajes de (a) y (c) y las URL de (b) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Cuando la red falla (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| `safely` con un `catch` por problema, en el orden correcto y sin capturar `Exception` ni usar `runCatching`; `HttpTimeout` y `HttpRequestRetry` solo para los 5xx (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con lo medido en (a), (b) y (c) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Las observaciones del equipo

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 30 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: sin posición, `text/plain`, sin red no toca la base de datos, un 503 es una llamada, `Refreshed` con error, el botón desactivado mientras espera | 25 |
| Red | En `data`, sin motor ni Android: clases del servidor con sus nombres, `ignoreUnknownKeys`, `expectSuccess`, `HttpTimeout` y la URL base; `toNetworkError` en el orden correcto; `refresh()` que no se traga la cancelación y escribe en Room | 25 |
| App | El `HttpClient` `@Singleton` con OkHttp en `AppModule` y `BuildConfig.API_URL` por flavor; `INTERNET` en el manifiesto; `Refresh` y `Refreshed` por el reducer; el aviso como efecto, con un mensaje por error | 15 |
| Adaptación al dominio | Lo que manda el servidor de tu dominio y sus mensajes; README con lo que pide el paso 11 | 15 |
| Entrega | Rama `week-31`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `core` o `data` dependen de Android o de un motor de Ktor | -10 |
| Un `HttpClient` nuevo por petición, o creado fuera del grafo | -10 |
| La red le pasa la lista a la pantalla sin pasar por Room | -10 |
| `catch (e: Exception)` sin relanzar lo que no es de la red, o `runCatching` alrededor de la petición | -10 |
| Una URL de servidor, una clave o un secreto escritos en el código | -10 |
| Un test que sale a la red de verdad | -10 |
| Un test de la semana 30 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-31` → `main` en el repositorio del aprendiz.
