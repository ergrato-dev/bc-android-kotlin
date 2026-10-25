# Proyecto — Bitácora reactiva

⏱️ 4 h · Capa 6 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora deja de ser una lista que se consulta y pasa a ser un dato que se **observa**: `LogbookStore` expone un `StateFlow` con las observaciones, un flujo de contadores por nivel y una vista filtrable que se recalcula cuando cambia el almacén, la consulta o el nivel. La sincronización de la semana 6 emite **un evento por fuente en cuanto termina** y, si el colector se va, cancela las fuentes pendientes.

## Dónde trabajas

En **tu repositorio**, rama `week-07`. El `starter/` trae el modelo de las semanas 5 y 6 ya hecho (`Code`, `Level`, `Observation`, `parseLine`, `fetchOne`, `slowSource`); **usa tus versiones y tus nombres del dominio**. Lo nuevo está en `Store.kt` y `SyncFlow.kt`, con las firmas y los motivos exactos en los comentarios. Tu `Logbook` de semanas anteriores puede quedarse o sustituirse por el almacén: decide y justifícalo en el pull request.

## Pasos

1. **`LogbookStore`**: un `MutableStateFlow<List<Observation>>` privado. `add` y `remove` son **atómicos** (`update`) y devuelven si añadió / cuántas quitó. Cuidado: el bloque de `update` puede repetirse.
2. **`countByLevel`**: siempre los tres niveles (con ceros), sin emitir dos veces el mismo mapa seguido.
3. **`watch`**: `combine` del almacén, la consulta y el nivel. Consulta vacía = todas; nivel `null` = cualquiera; orden de inserción.
4. **`syncInto`**: `channelFlow`, una coroutine por fuente, `Semaphore(maxParallel)` y `fetchOne`. Cada fuente emite **un** `SyncEvent` al terminar (`Imported` o `SourceFailed`), en el orden en que termina. `require`s comprobados al recoger. Con `take(1)`, las demás fuentes se cancelan.
5. **`summarize`**: un `fold` sobre los eventos.
6. **Cambia `Main.kt`** por datos de tu dominio (una fuente lenta, una que falle, una con una línea repetida y una inválida) y ejecuta `./gradlew run`: verás los eventos y los contadores cambiar **a medida que llegan**.
7. **Escribe al menos 8 tests** en `StoreTest.kt`, con `runTest` y calculados **a mano**:
   - `remove` (cuántos quita, y 0 si no existe),
   - `countByLevel` con ceros y siguiendo los cambios (duplicada: no emite),
   - `watch` con consulta y con nivel,
   - el **orden de llegada** de los eventos y `currentTime` con tres fuentes de tiempos distintos,
   - `maxParallel` = 1 y = 2 (tiempo total),
   - un fallo y un tiempo agotado como eventos (motivo exacto),
   - duplicadas e inválidas contadas por fuente,
   - `take(1)` cancelando el resto (`currentTime` y contenido del almacén),
   - los `require` al recoger,
   - muchas coroutines añadiendo a la vez (1 000 en `Dispatchers.Default`),
   - `summarize`.
8. **Abre el pull request** `week-07` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, sin `GlobalScope`, listas inmutables en el `StateFlow`, `update` en lugar de leer y escribir `value`, sin `try/catch` alrededor de `emit`, tests con valores calculados a mano.

## Pistas

- `var added = false` asignado **dentro** de `update { }` es la forma de sacar un resultado de un `update`.
- `Level.entries.associateWith { level -> list.count { it.level == level } }` da los tres niveles siempre.
- `store.observations` ya es un `StateFlow`: pásalo directamente a `combine`.
- Calcula el evento **dentro** de `withPermit` y envíalo **fuera**: así no retienes el permiso mientras esperas a que el canal acepte el valor.
- Para observar un flujo infinito en un test: `runTest(UnconfinedTestDispatcher())` y `backgroundScope.launch { flujo.collect { seen += it } }`.
- Para probar `take(1)`: `syncInto(...).take(1).toList()` y comprueba `currentTime` (el de la fuente más rápida).
