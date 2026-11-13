# Proyecto — Lo que se apunta sin red

⏱️ 5 h · Capa 31 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 31, «Actualizar» traía las observaciones del equipo y pisaba con ellas las de aquí: lo que se apuntaba en el campo nunca llegaba al servidor, y podía perderse. Esta semana, la Bitácora es offline-first de verdad. Cada cambio (una observación nueva, una editada, una borrada) se guarda al momento en Room y deja una fila en una cola, `pending`. Una sincronización en WorkManager espera a que haya red, baja lo del servidor, decide los conflictos con la regla «gana el último» y sube lo que queda en la cola. Si falla la red, lo vuelve a intentar con espera exponencial; si se cierra la app o se reinicia el móvil, sigue en cola. La pantalla no cambia: el ViewModel no sabe nada de esto.

El servidor contesta ahora con la hora de cada observación, y acepta `PUT` y `DELETE` con la suya:

```json
{"observations": [
  {"code": "A-1", "level": "high", "description": "roya en el lote 2", "lat": 4.6, "lng": -74.08, "updated_at": 1730000000000}
]}
```

```
PUT    observations/A-2                    {"code":"A-2","level":"low","description":"pulgón","updated_at":1000}
DELETE observations/A-1?updated_at=2000
```

Con un `409 Conflict` dice que tiene una versión más nueva; con un `404` a un `DELETE`, que ya no la tiene.

## Dónde trabajas

En **tu repositorio**, rama `week-32`, a partir de lo que entregaste en la semana 31. El `starter/` es la Bitácora de la semana 31 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya:

- en el catálogo, WorkManager 2.12.0 (`work-runtime` en la app, `work-testing` en sus tests) y `room3-testing` en los tests de `data`; y `verification-metadata.xml` con los 21 componentes nuevos;
- en `data/Network.kt`, `ObservationApi.observations()`, que devuelve ya las clases del servidor (`List<RemoteObservation>`); en `data/Database.kt`, `RemoteObservation.toEntity()` (la traducción que antes estaba en `Network.kt`) y `RoomObservationRepository` con un tercer parámetro, `clock`;
- en `app/Sync.kt`, `SYNC_WORK`, `MAX_ATTEMPTS`, y `SyncWorker`, `SyncWorkerFactory`, `sync()` y `SyncingRepository` sin hacer;
- en `AppModule`, `observations(context, database, client)`, con el `Context` que hará falta;
- el `BuildTest` de la raíz con los tres permisos de WorkManager (`onlyTheExpectedPermissionsShip`), y en `DeleteTest`, la espera a que vuelva la fila tras «Deshacer» (teoría 05, apartado 3).

Y tests nuevos:

- `data/src/test/kotlin/dev/bitacora/data/SyncTest.kt`: la cola y la regla, con un servidor de mentira y un reloj del test;
- `data/src/test/kotlin/dev/bitacora/data/MigrationTest.kt`: de la versión 1 a la 2;
- `app/src/test/kotlin/dev/bitacora/SyncTest.kt`: el worker, la cola de WorkManager, la configuración de la app y el manifiesto;
- en `NetworkTest`, los cuatro primeros miran ya `RemoteObservation`, y `refreshWritesToTheDatabase` espera que gane la A-1 de aquí, sin subir (sin `updated_at`, la del servidor tiene hora 0).

Con la Bitácora de la semana 31 y estos cambios, fallan 12 de 39 en `:data:test`, 9 de 100 en `testFieldDebugUnitTest` y 9 de 88 en `testDemoDebugUnitTest`. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :data:test`. En `SyncTest` de `data`, ¿cómo lee el test la cola, si aún no existe? ¿Qué error da `queue()` ahora? Mira también cómo decide el servidor de mentira qué contestar (teoría 05, apartado 4).
2. **La versión 2**: en `ObservationEntity`, `updatedAt: Long = 0` con `@ColumnInfo(defaultValue = "0")`; la entidad `PendingChange` (tabla `pending`: `code` como clave, `deleted`, `updatedAt`); y en `@Database`, las dos entidades, `version = 2` y `autoMigrations = [AutoMigration(from = 1, to = 2)]`. Compila: ¿qué archivo nuevo aparece en `data/schemas/`? Y en el callback `onCreate`, el `INSERT` con la lista de columnas (teoría 04, apartado 1).
3. **La cola**: en el DAO, `replace(observations, now)` en una `@Transaction`: compara con lo que había (`snapshot()`), conserva la hora de las que no han cambiado, pone `now` a las nuevas y distintas, y encola esas y las que ya no están, como lápidas. En el repositorio, `save` pasa `clock()` (teoría 03, apartado 2; teoría 04, apartado 2).
4. **El servidor**: `updatedAt` en `RemoteObservation` con `@SerialName("updated_at")` y `= 0`; `toEntity()` con la hora, y la vuelta, `ObservationEntity.toRemote()`, con el nivel en minúsculas. En `ObservationApi`, `put(observation)` (`PUT observations/{code}`, con el cuerpo en JSON) y `delete(code, updatedAt)` (`DELETE observations/{code}` con el parámetro `updated_at`).
5. **Bajar, decidir, subir**: en el DAO, `merge(received)` con «gana el último» frente a la cola (con la misma hora, gana lo de aquí), y `sent(code, updatedAt)`, que borra de la cola solo si sigue siendo ese cambio. En `refresh()`: bajar, `merge`, y por cada fila de `pending()` (por hora), `put` o `delete`; un 409 o un 404 también la sacan de la cola; cualquier otro fallo la deja, y lo que no es de la red sigue subiendo (teoría 03, apartado 4; teoría 04, apartado 3).
6. **El worker y su fábrica**: `doWork()` traduce `RefreshResult`: `Done`, `success`; `BAD_RESPONSE` o el intento `MAX_ATTEMPTS`, `failure`; lo demás, `retry`. `SyncWorkerFactory` crea un `SyncWorker` con `repository.get()` si el nombre es el suyo, y si no, `null` (teoría 04, apartado 4).
7. **Encolar**: `sync()` con `enqueueUniqueWork(SYNC_WORK, ...)`, `CONNECTED` y backoff exponencial de 30 s. ¿`KEEP` o `REPLACE`? Decide, y explícalo en tu README (teoría 02, apartado 4). `SyncingRepository` sobrescribe `save`: el de dentro, y después `sync()`. En `AppModule`, `SyncingRepository(RoomObservationRepository(...), WorkManager.getInstance(context))`.
8. **El arranque**: `BitacoraApp` implementa `Configuration.Provider` con la `SyncWorkerFactory` inyectada; en el manifiesto, el inicializador automático de WorkManager fuera, sin quitar el resto de `androidx.startup` (teoría 04, apartado 5). Antes de hacerlo, prueba el paso 7 solo: ¿qué dicen los tests que abren la actividad?
9. **Los tests y la verificación**: `./gradlew :data:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :data:test` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo? Y `./gradlew build`. ¿Cuántos permisos tiene ahora el APK, y cuál no esperabas?
10. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
    - en `data`, guardar la misma lista dos veces no encola nada la segunda;
    - en `data`, cambiar la A-1 mientras se sube (en el `PUT` del servidor de mentira, otro `save` con `now` mayor) deja en la cola el cambio nuevo;
    - en `data`, con la misma hora aquí y en el servidor, gana lo de aquí y se sube;
    - en `data`, un `DELETE` que contesta 404 saca la lápida de la cola;
    - en `data`, sin red, `refresh()` no toca la tabla `observations` ni la cola;
    - en la app, con el WorkManager de los tests, un `BAD_RESPONSE` deja la sincronización en `FAILED`, y el siguiente `save` encola otra.
11. **Explica en tu README** qué política elegiste en `sync()` y qué perdería la otra; por qué la cola es una tabla y no una lista en memoria (o en el `Data` del trabajo); por qué hace falta la lápida; qué puede perder «gana el último» en tu dominio, con un ejemplo; y por qué `SyncWorkerFactory` recibe un `Provider`.
12. **Abre el pull request** `week-32` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, la versión 2 con su migración probada, la cola escrita en la misma transacción que el cambio y con lápidas, «gana el último» en el DAO, `refresh()` que baja, decide y sube sin tragarse la cancelación, el worker con `success`, `retry` y `failure` bien elegidos, la fábrica inyectada con `Provider`, WorkManager arrancado con la configuración de la app, el ViewModel sin cambios, y tests con valores esperados escritos a mano.

## Pistas

- `New NOT NULL column'updatedAt' added with no default value specified`: falta `@ColumnInfo(defaultValue = "0")` (paso 2).
- `table observations has 7 columns but 6 values were supplied` en `RoomTest`: el `INSERT` del callback, sin lista de columnas.
- `no such table: pending` en `SyncTest` de `data`: falta `PendingChange` en `@Database` (paso 2).
- `NoSuchFileException: schemas/dev.bitacora.data.BitacoraDatabase/2.json` en `MigrationTest`: la base de datos sigue en la versión 1; el esquema 2 lo escribe KSP al compilar con `version = 2`.
- `aNewerLocalChangeWins` ve «del equipo»: `refresh()` sigue haciendo `upsert` de todo; tiene que pasar por `merge` (paso 5).
- `refreshUploadsTheQueueAndEmptiesIt` ve el `PUT` con `"level":"LOW"`: en `toRemote()`, `level.name.lowercase()`.
- `refreshUploadsTheQueueAndEmptiesIt` ve el `PUT` de la A-2 con `"updated_at":2000`: `replace` pone la hora nueva a todas, también a las que no han cambiado (paso 3).
- `aServerErrorWhileUploadingKeepsTheQueue` ve la cola vacía: `sent` se llama aunque el `PUT` falle, o el `catch` se traga el 503.
- Todos los tests de pantalla fallan con `lateinit property workerFactory has not been initialized`: la fábrica recibe el repositorio, no un `Provider` (teoría 04, apartado 4).
- Todos los tests de pantalla fallan con `WorkManager is not initialized properly`: `BitacoraApp` no es `Configuration.Provider` (paso 8).
- `NoSuchMethodException: dev.bitacora.SyncWorker.<init>(android.content.Context,androidx.work.WorkerParameters)`: la fábrica devuelve `null` para `SyncWorker`, y WorkManager lo intenta por reflexión (paso 6).
- `theAutomaticInitializerIsRemoved` lista `androidx.work.WorkManagerInitializer`: falta el `meta-data` con `tools:node="remove"` (paso 8). Los demás de la lista (los de maps-compose y OkHttp, entre otros) se quedan.
- `savingQueuesASyncThatWaitsForTheNetwork` da `List is empty`: `sync()` no encola, o con otro nombre.
- Un test de pantalla agota los 5 s de `waitForTheList` una vez y pasa al repetirlo: el ordenador va cargado; repite el test solo antes de buscar otra cosa.
- `./gradlew :data:test --tests '*SyncTest'` y `./gradlew :app:testFieldDebugUnitTest --tests 'dev.bitacora.SyncTest'` para ir test a test.
