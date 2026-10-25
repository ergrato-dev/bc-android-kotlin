# Ejercicio 01 — Suspend y scopes

⏱️ 2 h · Teoría previa: 01, 02 y 05

## Objetivo

Escribir funciones `suspend` que usan varias coroutines a la vez, se cancelan bien y se prueban en tiempo virtual.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`. Los tests ya están escritos en `src/test/kotlin/MainTest.kt`: léelos, dicen qué se espera.

1. **`fetchAll`**: todas las llamadas a la vez, resultados en el orden de `ids`. El test comprueba que el reloj virtual avanza **100 ms**, no 300.
2. **`race`**: devuelve la más rápida y **cancela** a la otra. Mira en el test cómo se sabe que la lenta no terminó.
3. **`retry`**: reintenta hasta `times` veces y lanza el último error. Escribe el `catch` de forma que una `CancellationException` **no** se reintente (hay un test).
4. **`orDefault`**: `withTimeoutOrNull`. Cambia por `withTimeout` y observa qué excepción sale.
5. **`startCountdown`**: una extensión de `CoroutineScope` que devuelve el `Job`. Comprueba qué pasa con `ticks` si quitas el `delay`.
6. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `currentTime` da 300 en `fetchAll` | Esperas cada `async` antes de lanzar el siguiente; primero `map`, luego `awaitAll` |
| `race` espera 300 ms | Falta cancelar a la otra: `coroutineContext.cancelChildren()` al tener ganador |
| `retry` reintenta la cancelación | `catch (e: CancellationException) { throw e }` va **antes** de `catch (e: Exception)` |
| `This declaration needs opt-in` | El test ya trae `@file:OptIn`; si lo ves en tu código, añádelo a ese archivo |
| El test no termina | Un `launch` sin esperar y sin cancelar; revisa el paso 5 |
| `Unresolved reference: coroutineContext` | `import kotlin.coroutines.coroutineContext` |

## Entrega

Los 7 tests en verde. No hace falta `NOTAS.md`.
