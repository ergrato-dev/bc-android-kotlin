# Errores y ciclo de vida de un flujo

> Un flujo puede fallar a mitad de camino. La pregunta no es si capturar el error, sino **dónde**, y esa respuesta decide si la pantalla muestra un mensaje o se cae.

## 🎯 Objetivos

- Capturar errores con `catch` y entender que solo mira **hacia arriba**
- Reaccionar al principio y al final con `onStart` y `onCompletion`
- Reintentar con `retry`
- Respetar la **transparencia de excepciones**
- Convertir un flujo en estados de pantalla (`Loading`, `Success`, `Failure`)

## 1. `catch`: solo lo de arriba

```kotlin
flow {
    emit(1)
    error("boom")
}
    .catch { e -> emit(-1) }                // atrapa lo que ocurrió ARRIBA
    .collect { println(it) }                // 1, -1
```

`catch` es un operador: captura las excepciones del `flow { }` y de los operadores **anteriores**. No captura lo que lance el bloque de `collect` ni los operadores de después. Por eso se coloca justo antes del `collect`.

```kotlin
flow.collect { riesgoso(it) }               // si riesgoso lanza, catch NO lo ve
```

Para eso, un `try/catch` normal alrededor del `collect`.

## 2. `onStart` y `onCompletion`

```kotlin
flow
    .onStart { emit(UiState.Loading) }                       // antes del primer valor; puede emitir
    .onCompletion { cause -> log("fin: ${cause?.message ?: "ok"}") }
```

`onCompletion` se ejecuta al terminar **bien, con error o cancelado**, y recibe la causa (o `null`). **No** captura el error: lo deja seguir. Es el `finally` de los flujos.

## 3. Un flujo de estados de pantalla

Une las tres piezas con el `sealed` de la semana 5:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Failure(val message: String) : UiState<Nothing>
}

fun <T> Flow<T>.asUiState(): Flow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Failure(it.message ?: "error")) }
```

La pantalla recoge un solo flujo y hace un `when` exhaustivo. El error ya no es una excepción que se escapa: es **un dato más**.

## 4. Reintentar

```kotlin
source.retry(3)                                              // hasta 3 reintentos tras un fallo
source.retryWhen { cause, attempt -> cause is IOException && attempt < 3 }
```

`retry` vuelve a **recoger el flujo desde el principio** (es frío). Los valores que ya se emitieron antes del fallo se emiten otra vez. Con `retryWhen` decides por tipo de error y puedes añadir un `delay` antes del siguiente intento. Si agota los reintentos, el último error sale tal cual. Una `CancellationException` nunca se reintenta.

## 5. Transparencia de excepciones

Un flujo no debe **esconder** los errores de quien lo recoge. Este código rompe esa regla:

```kotlin
flow {
    try {
        emit(1)                  // si el colector lanza aquí...
    } catch (e: Exception) {
        emit(-1)                 // ...lo "arreglas" emitiendo otra vez: ahora es IllegalStateException
    }
}
```

Regla práctica: **no pongas `try/catch` alrededor de `emit`**. Captura con `.catch { }` y deja que los errores del colector salgan. (Y, como en la semana 6, nunca te tragues la `CancellationException`.)

## 6. Cancelación

Recoger un flujo es una coroutine: si la cancelan, el `collect` lanza `CancellationException` y el `flow { }` se detiene en su siguiente suspensión. `take(n)` y `first()` cancelan a propósito. `emit` comprueba la cancelación, así que un bucle de `emit` es cancelable sin más; un bucle de CPU sin `emit` no (usa `ensureActive()`).

## 7. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| El `catch` no atrapa un error del `collect` | `catch` solo mira lo de arriba | `try/catch` alrededor del `collect` |
| `Flow exception transparency is violated` | `try/catch` alrededor de `emit` | Quita el `try`; usa `.catch { }` |
| `onCompletion` no evita que el error llegue | Es un `finally`, no un `catch` | Pon un `.catch` después |
| `retry` repite valores ya emitidos | Vuelve a empezar el flujo frío | Diseña el colector para tolerar repetidos |
| El flujo "termina bien" tras un `catch` | `catch` consume el error | Emite un estado de error o relanza con `throw e` |

## ✅ Qué debes saber al terminar

- `catch` atrapa lo de **arriba**; el error del colector sale.
- `onStart` abre, `onCompletion` cierra (siempre), ninguno captura.
- `retry` vuelve a recoger desde el principio.
- Un error puede ser un estado de la pantalla en lugar de una excepción.
