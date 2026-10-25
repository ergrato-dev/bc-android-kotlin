# Ejercicio 02 — Flujos calientes y combinación

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Compartir estado y eventos entre varios colectores, convertir un flujo frío en caliente sin gastar de más y combinar entradas cancelando lo obsoleto.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`Counter`**: un `MutableStateFlow` privado, expuesto como `StateFlow` de solo lectura, e `increment` con `update`. El test comprueba que poner dos veces el mismo valor emite una sola vez.
2. **`EventBus`**: un `MutableSharedFlow(replay = replay)`. Lee los dos tests: el suscriptor tardío con y sin `replay`.
3. **`shareAsState`**: `stateIn` con `WhileSubscribed(5_000)`. El test comprueba que el origen no arranca sin suscriptores y se cancela 5 000 ms después de irse el último.
4. **`searchResults`**: `combine` de la lista y la consulta.
5. **`settled`**: `debounce` y `distinctUntilChanged` (necesita `@OptIn(FlowPreview::class)`).
6. **`searchLatest`**: `flatMapLatest` (necesita `@OptIn(ExperimentalCoroutinesApi::class)`). El test comprueba que la búsqueda lenta de `"a"` **no termina** y solo llega el resultado de `"ab"`.
7. **`NOTAS.md`**: (a) Cambia `update { it + 1 }` de `increment` por `_count.value = _count.value + 1` y ejecuta el test de los 100 000 incrementos varias veces: ¿qué pasa y por qué? Devuélvelo a `update`. (b) Con un `StateFlow<String?>` como "mensaje pendiente", ¿qué ocurre si el valor es el mismo texto dos veces seguidas? ¿Por qué un `StateFlow` no sirve para eventos?
8. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| El colector no ve nada tras `publish` | El test usa `UnconfinedTestDispatcher`; si usas otro, falta `runCurrent()` |
| El suscriptor tardío ve eventos antiguos con `replay = 0` | `replay` mal pasado al `MutableSharedFlow` |
| El origen arranca sin suscriptores | Usas `Eagerly` en lugar de `WhileSubscribed` |
| `This declaration is in a preview state` | Falta `@OptIn(FlowPreview::class)` en `settled` |
| `This declaration needs opt-in` | Falta `@OptIn(ExperimentalCoroutinesApi::class)` en `searchLatest` |
| `searchLatest` emite dos listas | Usas `flatMapMerge` o `map`; debe ser `flatMapLatest` |
| `combine` no emite nada | Alguna entrada es un flujo frío que aún no emitió |

## Entrega

Los 8 tests en verde y `NOTAS.md` con la respuesta del paso 7.
