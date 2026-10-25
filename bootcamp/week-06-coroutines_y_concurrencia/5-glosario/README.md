# Glosario — Semana 06

| Término | Definición |
| --- | --- |
| **async** | Lanza una coroutine que calcula un valor y devuelve un `Deferred` |
| **Cancelación cooperativa** | La coroutine solo se detiene en un punto de suspensión o si ella comprueba que la cancelaron |
| **CancellationException** | La señal de cancelación; no es un error y no se debe tragar |
| **Channel** | Cola con suspensión para pasar valores entre coroutines |
| **Concurrencia estructurada** | Toda coroutine vive en un scope que espera a sus hijas y las cancela con él |
| **Coroutine** | Trabajo que puede pausarse sin ocupar un hilo |
| **CoroutineContext** | Conjunto de elementos (`Job`, dispatcher, nombre, manejador) que se combinan con `+` |
| **CoroutineExceptionHandler** | Red de seguridad para errores no capturados de un `launch` raíz |
| **coroutineScope** | Scope dentro de una función `suspend`; si una hija falla, cancela a las demás |
| **Deferred** | Un `Job` que además tiene un resultado: `await()` lo devuelve |
| **delay** | Pausa suspendiendo, sin bloquear el hilo |
| **Dispatcher** | Decide en qué hilo corre una coroutine: `Default`, `IO`, `Main` |
| **ensureActive** | Lanza `CancellationException` si la coroutine ya fue cancelada |
| **Job** | El manejador de una coroutine: se puede esperar (`join`) y cancelar (`cancel`) |
| **launch** | Lanza una coroutine sin resultado y devuelve un `Job` |
| **main-safe** | Función `suspend` que se puede llamar desde cualquier hilo porque cambia de dispatcher por dentro |
| **Mutex** | Candado que suspende en lugar de bloquear; no es reentrante |
| **NonCancellable** | Contexto para limpiar con suspensión dentro de una coroutine ya cancelada |
| **runBlocking** | Bloquea el hilo hasta que terminan sus coroutines; solo para `main` y pruebas |
| **runTest** | Ejecuta una prueba con un reloj virtual que salta los `delay` |
| **Semaphore** | Limita cuántas coroutines trabajan a la vez |
| **supervisorScope** | Scope donde el fallo de una hija no cancela a las demás |
| **suspend** | Marca una función que puede pausarse; solo se llama desde otra `suspend` o una coroutine |
| **Tiempo virtual** | Reloj de las pruebas (`currentTime`) que avanza sin esperar de verdad |
| **withContext** | Ejecuta un bloque en otro contexto (normalmente otro dispatcher) y vuelve |
| **withTimeoutOrNull** | Devuelve `null` si el bloque no termina a tiempo |
