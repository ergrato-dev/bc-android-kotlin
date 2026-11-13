# Probar el trabajo en segundo plano

> Un trabajo que corre «cuando haya red» y reintenta «dentro de 30 segundos» parece imposible de probar sin esperar. `work-testing` lo resuelve con dos herramientas: una que crea el worker para llamarlo como una función, y un WorkManager de tests donde el test decide cuándo hay red y cuándo ha pasado la espera. Esta teoría cuenta las dos, lo que hace Robolectric con el WorkManager de verdad, y cómo se prueba la sincronización de `data` con un servidor de mentira.

## 🎯 Objetivos

- Probar un worker solo con `TestListenableWorkerBuilder`
- Probar la cola con `WorkManagerTestInitHelper`, `SynchronousExecutor` y el `TestDriver`
- Saber qué hace el WorkManager de verdad en Robolectric
- Probar la cola de cambios y la regla de conflictos con `MockEngine` y SQL

## 1. El worker, solo

`TestListenableWorkerBuilder` crea un worker con los datos de entrada y el número de intento que digas, y `doWork()` se llama como cualquier función `suspend`:

```kotlin
private fun work(attempt: Int = 0): ListenableWorker.Result = runBlocking {
    TestListenableWorkerBuilder<SyncWorker>(context)
        .setWorkerFactory(factory)
        .setRunAttemptCount(attempt)
        .build()
        .doWork()
}

@Test
fun afterFiveAttemptsItGivesUp() {
    repository.answer.value = RefreshResult.Failed(NetworkError.TIMEOUT)
    assertEquals(ListenableWorker.Result.retry(), work(attempt = 3))
    assertEquals(ListenableWorker.Result.failure(), work(attempt = 4))
}
```

Sin cola, sin condiciones, sin esperas: es el test de la lógica de `doWork()`. `Result` tiene `equals`: `Result.success(workDataOf("bytes" to 2048L))` se compara con lo que devuelve. Con `setWorkerFactory`, el worker se crea con su fábrica, igual que en la app; la de los tests de la Bitácora recibe el `FakeRepository`:

```kotlin
private val factory = SyncWorkerFactory { repository }
```

(`Provider` es una interfaz de Java con un solo método: una lambda vale.)

## 2. La cola, con el WorkManager de los tests

Para probar lo que pasa al encolar (condiciones, trabajo único, reintentos), hace falta un WorkManager. El de los tests se arranca así, antes de cada test:

```kotlin
@Before
fun testWorkManager() {
    WorkManagerTestInitHelper.initializeTestWorkManager(
        context,
        Configuration.Builder().setExecutor(SynchronousExecutor()).setWorkerFactory(factory).build(),
    )
    workManager = WorkManager.getInstance(context)
}
```

- **`SynchronousExecutor`**: todo en el hilo del test. Cuando `enqueue` vuelve, el trabajo ya está guardado; cuando le toca correr, corre antes de que vuelva la llamada que lo ha permitido.
- **El `TestDriver`**: en este WorkManager, nada corre solo. Las condiciones no se cumplen hasta que el test lo dice:

```kotlin
val driver = WorkManagerTestInitHelper.getTestDriver(context)!!
driver.setAllConstraintsMet(id)   // ya hay red (y batería, y lo que pida)
driver.setInitialDelayMet(id)     // ha pasado el setInitialDelay
driver.setPeriodDelayMet(id)      // ha pasado el periodo de un periódico
```

Un test de la Bitácora, entero:

```kotlin
@Test
fun savingQueuesASyncThatWaitsForTheNetwork() = runBlocking {
    SyncingRepository(repository, workManager).save(listOf(Observation("A-1", Level.HIGH, "roya")))
    val info = sync().single()
    assertEquals(WorkInfo.State.ENQUEUED, info.state)
    assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
    assertEquals(0, repository.refreshes)
    networkArrives()
    assertEquals(WorkInfo.State.SUCCEEDED, sync().single().state)
    assertEquals(1, repository.refreshes)
}
```

> 🔬 Medido con work-testing 2.12.0: tras un `retry()`, el trabajo vuelve a `ENQUEUED` con `runAttemptCount = 1`. Para que corra otra vez, `setInitialDelayMet` **no** sirve (no corre: es para `setInitialDelay`, no para el backoff); `setAllConstraintsMet` otra vez, sí, sin esperar el backoff. El backoff se comprueba en `WorkInfo.nextScheduleTimeMillis`, que queda unos 30 s por delante con el exponencial de 30 s.

Para leer el estado: `getWorkInfosForUniqueWork("sync").get()` o `getWorkInfosByTag(...)`. Con `SynchronousExecutor`, `.get()` no espera.

## 3. El WorkManager de verdad, en Robolectric

Los tests de pantalla de la Bitácora (los que usan `launch()`) arrancan `BitacoraApp` de verdad, con `AppModule`, y por tanto con `SyncingRepository` y `WorkManager.getInstance(context)`. ¿Qué hace ahí?

> 🔬 Medido en Robolectric 4.17:
> - Sin `Configuration.Provider` en la aplicación, `WorkManager.getInstance(context)` lanza `IllegalStateException: WorkManager is not initialized properly.` Robolectric no ejecuta el inicializador de `androidx.startup`.
> - Con `Configuration.Provider`, WorkManager arranca bajo demanda. Pero es un objeto estático: Robolectric crea una `Application` por test, y el WorkManager del primer test sigue siendo el mismo en los siguientes, con los trabajos de antes.
> - Un trabajo con `NetworkType.CONNECTED` no llega a correr nunca (la condición de red no se cumple); uno sin condiciones, sí: acaba en `SUCCEEDED` sin que el test haga nada.

Por eso los tests de pantalla no se enteran: cada cambio encola una sincronización que espera una red que no llega. Y por eso los tests que sí miran la cola usan `initializeTestWorkManager` en su `@Before`: sustituye a ese objeto estático por uno de tests, limpio.

**Un efecto lateral.** Guardar ahora hace más trabajo en el hilo de Room (leer la tabla, comparar, escribir la cola). Un test que comprobaba la pantalla justo después de «Deshacer» empezó a fallar a veces, y siempre pasaba solo:

> 🔬 Medido: `DeleteTest.undoPutsItBackInItsPlace` falló una vez en la suite completa y pasó dos de dos solo. Esperar a que la fila vuelva (`rule.waitUntil { ... }`, con el `idle()` del looper principal, como `waitForTheList` de la semana 30) lo arregla. Un test que mira el resultado de una escritura en otro hilo tiene que esperarla, no suponer que ya está.

## 4. La cola de `data`, con un servidor de mentira

`SyncTest` de `data` es JVM pura, como `NetworkTest` de la semana 31: Room con el driver empaquetado y `MockEngine`. El servidor de mentira contesta según el método:

```kotlin
addHandler { request ->
    if (offline) throw IOException("sin red")
    if (request.method == HttpMethod.Get) {
        respond("""{"observations": $remote}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
    } else {
        sent += "${request.method.value} ${request.url.encodedPathAndQuery} ${request.body.toByteArray().decodeToString()}".trim()
        respond("", status)
    }
}
```

El test cambia `remote` (lo que hay en el servidor), `status` (lo que contesta a un `PUT` o un `DELETE`), `offline` y el reloj (`now`), y mira `sent`:

```kotlin
assertEquals(
    listOf(
        """PUT /v1/observations/A-2 {"code":"A-2","level":"low","description":"pulgón","updated_at":1000}""",
        "DELETE /v1/observations/A-1?updated_at=2000",
    ),
    sent,
)
```

> 🔬 Medido: el cuerpo del `PUT` no lleva `lat` ni `lng` cuando son `null`: kotlinx-serialization no escribe los valores por defecto (`encodeDefaults = false`). El servidor tiene que tratar «falta» como «sin posición».

**La cola, con SQL.** Los tests leen la tabla `pending` con una consulta, no con el DAO:

```kotlin
private suspend fun queue(): List<String> = database.useReaderConnection { connection ->
    connection.usePrepared("SELECT code, deleted, updatedAt FROM pending ORDER BY updatedAt, code") { row ->
        buildList { while (row.step()) add("${row.getText(0)} ${if (row.getBoolean(1)) "borrar" else "subir"} ${row.getLong(2)}") }
    }
}
```

Así el test no depende de cómo se llamen los métodos del DAO, y compila antes de que existan: lo que comprueba es la tabla. `"A-1 borrar 2000"` se lee mejor en un fallo que un `PendingChange(code=A-1, deleted=true, updatedAt=2000)`.

**La migración**, como en la semana 30, con `MigrationTestHelper`: una base de datos de la versión 1 escrita con el SQL de entonces, migrada a la 2 y comprobada (`updatedAt` a 0, `pending` vacía).

## Para recordar

- `TestListenableWorkerBuilder` para la lógica de `doWork()`: intento, entrada y fábrica a mano.
- `initializeTestWorkManager` con `SynchronousExecutor` para la cola; el `TestDriver` dice cuándo se cumplen las condiciones. Tras un `retry`, `setAllConstraintsMet` otra vez.
- En Robolectric, el WorkManager de verdad necesita `Configuration.Provider`, es estático entre tests, y lo que pide red no corre.
- La sincronización de `data`: `MockEngine` que contesta por método, reloj del test, y la cola leída con SQL.
- Lo que se escribe en otro hilo se espera en el test.
