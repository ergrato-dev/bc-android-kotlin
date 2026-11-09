# Probar Room

> Room 3 funciona en la JVM, así que casi todo se prueba sin Android: la base de datos en un `@TempDir`, con el SQLite empaquetado, en milisegundos. Lo difícil no es la base de datos, son los hilos. Room responde desde el suyo, y tanto `runTest` como Robolectric esperan solo a lo que conocen. Esta teoría cuenta cómo esperar lo justo.

## 🎯 Objetivos

- Probar el DAO y el repositorio en la JVM, en memoria o en un archivo
- Probar un `Flow` de Room sin que el test se quede colgado
- Probar migraciones con `MigrationTestHelper` contra los esquemas exportados
- Probar la pantalla con la base de datos de verdad en Robolectric, esperando a la primera lista

## 1. En la JVM

```kotlin
private val database = Room.inMemoryDatabaseBuilder<LibretaDatabase>().setDriver(BundledSQLiteDriver()).build()

@AfterTest
fun close() = database.close()
```

En memoria, cada test empieza vacío y no deja nada. Para probar lo que sobrevive a cerrar (el repositorio de la Bitácora, la importación del JSON), un archivo en un `@TempDir`, y abrir y cerrar como haría la app:

```kotlin
private suspend fun <T> withRepository(initial: List<Observation> = emptyList(), block: suspend (ObservationRepository) -> T): T {
    val database = open(initial)
    try {
        return block(RoomObservationRepository(database))
    } finally {
        database.close()
    }
}

@Test
fun whatIsSavedSurvivesClosing() = runTest {
    withRepository { it.save(listOf(a, b)) }
    assertEquals(listOf(a, b), withRepository { it.observations.first() })
}
```

`first()` sobre el `Flow` de Room es una consulta normal: emite lo que hay y el test sigue.

## 2. Un `Flow` que sigue los cambios

Para ver la segunda emisión hay que estar recogiendo cuando ocurre el cambio, y Room emite desde su hilo:

```kotlin
@Test
fun pendingFollowsTheChanges() = runTest {
    val trip = libreta.insert(Trip(name = "Páramo"))
    val emitted = Channel<List<String>>(Channel.UNLIMITED)
    backgroundScope.launch(Dispatchers.Default) { libreta.pending(trip).collect { list -> emitted.send(list.map { it.name }) } }
    assertEquals(emptyList(), emitted.receive())
    val id = libreta.insert(Item(tripId = trip, name = "linterna"))
    assertEquals(listOf("linterna"), emitted.receive())
}
```

- Se recoge en `Dispatchers.Default`, como en la app, y en `backgroundScope`: `runTest` lo cancela al acabar.
- Cada `receive()` espera a la siguiente emisión, sin relojes. Una tras otra, el test no avanza hasta que Room ha respondido.

> 🔬 Medido: la primera versión de ese test recogía con `launch { ... take(3).toList() }` en el dispatcher de `runTest` y esperaba con un bucle de `yield()`. No terminó: `UncompletedCoroutinesError: After waiting for 1m, the test body did not run to completion`.

> 🔬 Medido: `withTimeoutOrNull(2000) { ... }` dentro de `runTest` usa el reloj virtual, y devolvió `null` aunque el `Flow` sí emitía. Con `withContext(Dispatchers.Default) { withTimeoutOrNull(2000) { ... } }`, el tiempo es de verdad y llega la emisión.

## 3. Migraciones

`room3-testing` trae `MigrationTestHelper`. En la JVM recibe la carpeta de los esquemas, la ruta del archivo, el *driver* y la clase de la base de datos:

```kotlin
private val helper by lazy {
    MigrationTestHelper(Path.of("schemas"), file.toPath(), BundledSQLiteDriver(), LibretaDatabase::class)
}

@Test
fun fromOneToTwoEveryItemHasQuantityOne() = runTest {
    helper.createDatabase(1).use {
        it.execSQL("INSERT INTO items (trip, name, packed) VALUES ('Páramo', 'linterna', 1), ('Páramo', 'agua', 0)")
    }
    helper.runMigrationsAndValidate(2).use {
        assertEquals(listOf("linterna|1", "agua|1"), it.rows("SELECT name, quantity FROM items ORDER BY id"))
    }
}
```

- `createDatabase(1)` crea un archivo con el `1.json`, no con la entidad de ahora. Las filas se escriben en SQL, porque la clase de la versión 1 ya no existe.
- `runMigrationsAndValidate(n, migrations)` corre las automáticas y las que le pases, y comprueba las tablas contra `n.json`. Devuelve la conexión, para mirar los datos.
- `Path.of("schemas")` es relativo a la carpeta del proyecto, que es donde Gradle ejecuta los tests.

> 🔬 Medido en el starter del ejercicio 02: sin `2.json`, `runMigrationsAndValidate(2)` falla con `java.nio.file.NoSuchFileException: schemas/dev.bitacora.LibretaDatabase/2.json`. El esquema sale al compilar con `version = 2`.

Un test con el builder de verdad (`theAppOpensAVersionOneFile`) comprueba además que la app tenga las migraciones en el `addMigrations`: `MigrationTestHelper` usa las que le pasas, no las de la app.

## 4. La pantalla con la base de datos de verdad

Los tests de Robolectric que abren `MainActivity` sin Hilt usan el `AppModule` de verdad, y ahora eso es Room con el SQLite de Android. Abre bien:

```kotlin
@Test
fun theSamplesAreInTheDatabase() = runBlocking {
    val database = AppModule.database(ApplicationProvider.getApplicationContext())
    assertEquals(Samples.observations, RoomObservationRepository(database).observations.first())
    database.close()
}
```

Lo que cambia es cuándo llega la lista. `waitForIdle` y los `assert` de Compose esperan al hilo principal, no a `Dispatchers.IO`.

> 🔬 Medido: con `loaded` en la actividad y los tests de antes sin tocar, fallan 30 de 83 en field y 21 de 74 en demo. Abren la actividad, buscan un texto y la pantalla todavía está en blanco.

Los tests abren la actividad con una función que espera a la lista (`app/src/test/kotlin/dev/bitacora/Launch.kt`):

```kotlin
fun ComposeTestRule.launch(intent: Intent? = null, saved: Bundle? = null): ActivityController<MainActivity> =
    Robolectric.buildActivity(MainActivity::class.java, intent)
        .let { if (saved == null) it.setup() else it.setup(saved) }
        .also { waitForTheList(it.get()) }

fun ComposeTestRule.waitForTheList(activity: MainActivity) {
    val viewModel = ViewModelProvider(activity)[BitacoraViewModel::class.java]
    waitUntil(5_000) {
        shadowOf(Looper.getMainLooper()).idle()
        viewModel.state.value.loaded
    }
}
```

- `ViewModelProvider(activity)` devuelve el mismo ViewModel que tiene la actividad: está en su `ViewModelStore`.
- `waitUntil` repite la condición hasta que se cumple, y el tiempo es de verdad.

> 🔬 Medido: sin `shadowOf(Looper.getMainLooper()).idle()`, `waitUntil` se agota (`Condition still not satisfied after 5000 ms`). Room responde y deja la lista para el hilo principal, pero en Robolectric el hilo principal es el del test, y nadie lo atiende mientras `waitUntil` espera.

> 🔬 Medido: un `CountingIdlingResource` de Espresso en el ViewModel, registrado en `IdlingRegistry`, no lo arregló: seguían fallando los mismos tests. Y obligaba a meter una dependencia de test en el código de la app.

Los tests con Hilt (`HiltTest`) no cambian: `FakeModule` da un repositorio en memoria, y su `Flow` emite al momento.

## Para recordar

- En la JVM: `inMemoryDatabaseBuilder` para lo que empieza vacío; un archivo en `@TempDir` para lo que sobrevive a cerrar.
- Un `Flow` de Room: se recoge en `Dispatchers.Default` y se espera con un `Channel`. En `runTest`, el tiempo es virtual.
- Migraciones: `MigrationTestHelper` con las filas en SQL, y un test con el builder de la app.
- Robolectric con la base de datos de verdad: abrir y esperar a `loaded`, atendiendo el hilo principal.
