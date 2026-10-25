# Proyecto — Sincronización asíncrona de la Bitácora

⏱️ 4 h · Capa 5 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora recibe datos de varias **fuentes remotas** (simuladas) que tardan distinto y a veces fallan. `sync` las pide a la vez, con un máximo de peticiones paralelas y un tiempo límite por fuente; lo recibido pasa por `parseLine`, y el informe final dice qué entró, qué se descartó, qué fuente falló y por qué.

## Dónde trabajas

En **tu repositorio**, rama `week-06`. El `starter/` trae el modelo de la semana 5 ya hecho (`Code`, `Level`, `Observation`, `parseLine`, `Logbook`); **usa tus versiones y tus nombres del dominio**. Lo nuevo está en `Sync.kt`, con las firmas y los motivos exactos en los comentarios.

## Pasos

1. **`fetchSources`**: pide todas las fuentes a la vez, con un `Semaphore(maxParallel)`.
   - Resultado en el orden del mapa, no en el de llegada.
   - `require(timeoutMs > 0)` y `require(maxParallel > 0)`.
   - Tiempo agotado: `Failed(source, "tiempo agotado (<timeoutMs> ms)")`. **El reloj empieza cuando la fuente arranca**, no mientras espera su turno.
   - Excepción de la fuente: `Failed(source, e.message ?: nombre simple de la clase)`.
   - Si cancelan la sincronización, la cancelación se propaga: **no** la conviertas en un `Failed`.
   - El fallo de una fuente no cancela a las demás.
2. **`sync`**: recorre los resultados **en orden**; cada línea pasa por `parseLine`. Las válidas se añaden (si `add` devuelve `false`, suma a `duplicates`); las inválidas van a `invalid` sin cambiar línea ni motivo; las fuentes caídas van a `failed`.
3. **Cambia `Main.kt`** por datos de tu dominio: una fuente lenta, una que falle, una con una línea repetida y una línea inválida. Ejecuta `./gradlew run`.
4. **Escribe al menos 8 tests** en `SyncTest.kt`, con `runTest` y calculados **a mano** (no `Thread.sleep`):
   - tres fuentes de 100 ms con `maxParallel` = 3, = 2 y = 1: **100, 200 y 300 ms**,
   - tiempo agotado: motivo exacto y `currentTime`,
   - el tiempo límite cuenta desde el arranque de la fuente (no desde la cola),
   - una excepción **con** mensaje y otra **sin** mensaje,
   - un fallo no cancela a las demás fuentes,
   - duplicadas e inválidas, con el **orden** correcto,
   - los dos `require`,
   - cancelar la sincronización no produce un `Failed`.
5. **Abre el pull request** `week-06` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, sin `GlobalScope`, sin `runBlocking` fuera de `main`, sin tragarse `CancellationException`, tests con valores calculados a mano.

## Pistas

- `sources.map { (name, source) -> async { ... } }.awaitAll()` dentro de `coroutineScope` conserva el orden.
- `permits.withPermit { ... }` rodea **todo** el trabajo de una fuente, incluido su tiempo límite.
- `withTimeoutOrNull(timeoutMs) { source.fetch() }` devuelve `null` al agotarse; no hace falta capturar nada para ese caso.
- Para la excepción sin mensaje: `e::class.simpleName`.
- Para probar la cancelación: `launch { sync(...) }`, `delay(50)`, `job.cancel()`, `job.join()` y comprueba `job.isCancelled`.
- Si el tiempo virtual no coincide, dibuja una línea de tiempo en papel con el límite de paralelismo.
