# Ejercicio 01 — El tiempo virtual

⏱️ 1 h 45 min · Teoría previa: 04 y 05

## Objetivo

Hay código que vive del reloj: reintentar una petición esperando cada vez el doble, buscar cuando la persona deja de escribir, pedir datos cada segundo. Probarlo con esperas de verdad haría la suite lenta y frágil. Al terminar, tres piezas así están escritas y sus 12 tests pasan en milisegundos, con el reloj virtual de `runTest` y Turbine.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM, sin Android. Tiene kotlinx-coroutines 1.11.0, kotlinx-coroutines-test y Turbine 1.2.1.

- `src/main/kotlin/dev/bitacora/time/Time.kt`: `retrying`, `searches` y `Poller`, con lo que tienen que hacer escrito en los comentarios y el cuerpo en `TODO()`.
- `src/test/kotlin/dev/bitacora/time/TimeTest.kt`: 12 tests. No los toques, son el enunciado.

Compila, y al empezar fallan los 12.

## Pasos

1. **Lee `TimeTest` y ejecuta** `./gradlew test`. En `theWaitDoubles`, ¿por qué `currentTime` vale 3000 y no 7000? ¿Cuánto dura el test de verdad? (Mira el `time` de `build/test-results/test/*.xml`.)
2. **`retrying`**: un bucle con `try`/`catch (e: IOException)`, `delay(wait)` y `wait *= 2`. El último intento va fuera del `try`, para que su excepción salga. ¿Por qué no hace falta tratar la cancelación aparte? (Teoría 04, apartado 1: ¿qué lanza `delay` al cancelar?)
3. **`searches`**: con los operadores de `Flow`, `debounce`, `map`, `filter` y `distinctUntilChanged`. El orden importa. ¿Qué falla en `theSameSearchIsNotRepeated` si `distinctUntilChanged` va antes de `map { it.trim() }`?
4. **`Poller`**: `start()` lanza en `scope` un bucle que llama a `fetch`, deja el resultado en `_latest` y espera `every`. Guarda el `Job`: `stop()` lo cancela, y `start()` no lanza otro si el que hay sigue activo.
5. Ejecuta `./gradlew test` (12 de 12) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) En `itFetchesEverySecond`, cambia `backgroundScope` por `this` (el `TestScope`). ¿Qué pasa? Copia la primera línea del error y explica por qué. Restáuralo.
   - (b) En el mismo test, quita el `runCurrent()`. ¿Sigue pasando? ¿Por qué `latest` está en `null` justo después de `start()`?
   - (c) Escribe un test tuyo, `aTimerOutsideTheClock`, en el que `retrying` espere en `withContext(Dispatchers.Default) { ... }`. ¿Qué vale `currentTime` al terminar, y cuánto tarda el test? ¿Qué cambiarías en `retrying` para poder probarlo en tiempo virtual? (Teoría 04, apartado 4.)
   - (d) En `latestShowsEachResult`, cambia `assertEquals(20, awaitItem())` por `expectNoEvents()`. ¿Pasa `expectNoEvents()`? ¿Qué falla después, y por qué?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `This declaration needs opt-in` en tu test | `currentTime`, `advanceTimeBy`, `runCurrent` y `UnconfinedTestDispatcher` son experimentales: `@OptIn(ExperimentalCoroutinesApi::class)` en la clase, como en `TimeTest` |
| `theLastErrorComesOut` da 7000 | Esperas también después del último intento: el último va fuera del bucle |
| `cancellingStopsTheRetries` cuenta 5 llamadas | Atrapas `Exception` (o `Throwable`) y te tragas la `CancellationException`: atrapa solo `IOException` |
| `UncompletedCoroutinesError: After waiting for 1m` | Un bucle que no acaba fuera de `backgroundScope`, o `Poller` con un `scope` propio en vez del que recibe |
| `startingTwiceIsOneLoop` cuenta 6 | `start()` lanza siempre: mira si el `Job` guardado sigue activo |
| `No value produced in 3s` en `latestShowsEachResult` | El bucle no escribe en `_latest`, o espera antes de la primera petición |

## Entrega

Los 12 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 6 y el test de (c).
