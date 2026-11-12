# Glosario — Semana 31

| Término | Definición |
| --- | --- |
| **Accept** | Cabecera con los formatos que el cliente sabe leer; `ContentNegotiation` pone `application/json` |
| **BuildConfig** | Clase que AGP genera por variante con constantes del build, como la URL del servidor de cada flavor. En AGP 9 hay que encenderla con `buildFeatures.buildConfig` |
| **Cancelación** | Cómo termina una corrutina: una `CancellationException` que tiene que seguir subiendo. Capturarla deja seguir un código que ya no debería ejecutarse |
| **ClientRequestException** | La `ResponseException` de un 4xx, con `expectSuccess`: la petición está mal y repetirla no lo arregla |
| **ConnectTimeoutException** | El servidor no aceptó la conexión a tiempo (`connectTimeoutMillis`). Es una `IOException` |
| **Content-Type** | Cabecera con el formato del cuerpo. En una respuesta, decide si `ContentNegotiation` la sabe leer; en una petición, cómo se manda `setBody` |
| **ContentConvertException** | El cuerpo no se puede convertir; `JsonConvertException` cuando el JSON no encaja con la clase |
| **ContentNegotiation** | Plugin de Ktor que convierte entre el cuerpo y los objetos, con kotlinx-serialization |
| **defaultRequest** | Plugin de Ktor con lo que llevan todas las peticiones: la URL base, cabeceras |
| **expectSuccess** | Opción del cliente que convierte cualquier código que no sea 2xx en una `ResponseException` |
| **Espera exponencial** | Esperar cada vez el doble entre reintentos, con algo de azar para que muchos clientes no reintenten a la vez |
| **HttpClient** | El cliente de Ktor: el mismo en todas las plataformas, con el motor y los plugins que se le den. Uno por app |
| **HttpRequestData** | La petición tal como llega al motor, con los plugins aplicados: URL, método, cabeceras y cuerpo. En los tests se guarda para mirarla |
| **HttpRequestRetry** | Plugin de Ktor que repite una petición: qué casos, cuántas veces y cuánto esperar |
| **HttpRequestTimeoutException** | La petición entera tardó más que `requestTimeoutMillis`. Es una `IOException` |
| **HttpTimeout** | Plugin de Ktor con los tres tiempos máximos: conexión, silencio entre trozos y petición entera |
| **ignoreUnknownKeys** | Opción de `Json { }` que salta los campos que la clase no tiene: deja al servidor añadir campos sin romper las apps instaladas |
| **MockEngine** | Motor de Ktor para los tests: no abre conexiones y contesta lo que diga el test. Con el dispatcher del test, en tiempo virtual |
| **Motor** | La parte de Ktor que abre las conexiones: OkHttp, Android, CIO, Darwin o `MockEngine` |
| **NetworkError** | En la Bitácora, los errores de la red con nombre de la app: `OFFLINE`, `TIMEOUT`, `SERVER` y `BAD_RESPONSE` |
| **NoTransformationFoundException** | Ktor no sabe convertir el cuerpo en el tipo pedido: falta `ContentNegotiation`, o la respuesta no dice que es JSON |
| **OkHttp** | Cliente HTTP de Square para Android y la JVM; el motor de Ktor en la Bitácora y la base de Retrofit |
| **RefreshResult** | Lo que devuelve `refresh()`: `Done` con cuántas han llegado, o `Failed` con un `NetworkError` |
| **RemoteObservation** | La observación con la forma del JSON del servidor (`lat`, `lng`, el nivel como texto); se traduce a `Observation` en `data` |
| **ResponseException** | Madre de `ClientRequestException` y `ServerResponseException`; lleva la respuesta (`e.response.status`) |
| **Retrofit** | Cliente HTTP de Square que describe la API con una interfaz anotada (`@GET`, `@Query`) y la implementa al ejecutar |
| **@SerialName** | El nombre de un campo en el JSON, cuando no es el de la propiedad |
| **ServerResponseException** | La `ResponseException` de un 5xx, con `expectSuccess`: un fallo del servidor que puede ser pasajero |
| **setBody** | Pone el cuerpo de una petición; con un objeto, necesita `contentType` para saber cómo convertirlo |
| **Texto en claro** | HTTP sin cifrar (`http://`). Cerrado por defecto desde Android 9; se abre por dominio con `network_security_config` |
| **Tiempo virtual** | El reloj de `runTest`: los `delay` y los tiempos máximos pasan sin esperar, y `currentTime` dice cuánto ha pasado |
