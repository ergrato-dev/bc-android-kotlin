# Ejercicio 01 — Flujos fríos

⏱️ 2 h · Teoría previa: 01, 02, 03 y 05 (sección de pruebas)

## Objetivo

Crear y transformar flujos fríos, convertir un error en un estado de pantalla y comprobar con tiempo virtual que cada operador hace lo que dice.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`. Los tests ya están escritos en `src/test/kotlin/MainTest.kt`: léelos, dicen qué se espera.

1. **`ticker`**: un `flow { }` con `delay` antes de cada valor. El test comprueba que crear el flujo no tarda nada (`currentTime` 0) y que dos recogidas tardan 300 ms cada una.
2. **`runningSum`**: la suma acumulada con un operador intermedio (`scan`).
3. **`firstAbove`**: un terminal que **cancela** el origen en cuanto encuentra el valor. El test cuenta cuántos valores emitió el origen.
4. **`asUiState`**: `Loading`, un `Success` por valor y un `Failure` si el origen falla. Prueba a colocar `catch` **antes** de `onStart` y mira qué cambia.
5. **`retryTimes`**: `times = 2` son 3 intentos en total; observa en el test que los valores de los intentos fallidos se vuelven a emitir.
6. **`logged`**: `onStart`, `onEach` y `onCompletion`, sin capturar el error.
7. **`workerThreadName`**: `flowOn(Dispatchers.Default)`. Mueve el `flowOn` antes de un `map` y comprueba en qué hilo corre cada parte.
8. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `currentTime` no es 0 tras crear el flujo | Estás haciendo trabajo fuera del `flow { }`; el cuerpo debe estar dentro |
| `runningSum` devuelve un valor de más | `scan` emite el valor inicial: quítalo con `drop(1)` |
| `firstAbove` emite hasta 100 | Usas `filter { }.first()` sobre una colección, o `toList()`; usa `firstOrNull { }` sobre el flujo |
| `Type mismatch` en `asUiState` | `map` devuelve `Success<T>`: indica el tipo, `map<T, UiState<T>> { ... }` |
| `retryTimes` hace 2 intentos y no 3 | `retry(times)` cuenta reintentos, no intentos; revisa el test |
| `Flow exception transparency is violated` | Has puesto un `try/catch` alrededor de `emit` |
| `workerThreadName` devuelve un hilo de la prueba | Falta `flowOn`, o está antes de `flow { }` |

## Entrega

Los 7 tests en verde. No hace falta `NOTAS.md`.
