# Proyecto — Las observaciones en Room

⏱️ 4 h 30 min · Capa 29 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 29, las observaciones vivían en `observations.json`, que se leía entero al abrir y se reescribía entero con cada cambio. Esta semana pasan a una base de datos de Room, que es la fuente de verdad: el ViewModel recoge su `Flow` y le manda cada cambio. La primera vez que se abre la versión nueva, lo que había en el JSON pasa a la tabla y el archivo se borra. La persona no ve nada distinto, y eso es lo que se pide: sus observaciones siguen ahí, y un enlace a la A-2 sigue abriendo la A-2.

## Dónde trabajas

En **tu repositorio**, rama `week-30`, a partir de lo que entregaste en la semana 29. El `starter/` es la Bitácora de la semana 29 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya:

- en el catálogo, Room 3.0.3 (`room3-runtime`, `room3-compiler` y el plugin `androidx.room3`) y `sqlite-bundled` 2.7.1; en `data/build.gradle.kts`, los plugins `ksp` y `room`, `room3 { schemaDirectory(...) }`, las dependencias, guava restringida a 33.7.2-jre en `ksp` y `verification-metadata.xml` con los componentes nuevos;
- en `data/Database.kt`, `ObservationEntity` (solo con el código), el DAO (solo con `all()`), `BitacoraDatabase`, `bitacora()`, que no hace nada, y `RoomObservationRepository`, que devuelve una lista vacía y no guarda;
- en `AppModule`, el `@Provides @Singleton` de `BitacoraDatabase`, con `Room.databaseBuilder<BitacoraDatabase>(context, context.getDatabasePath("bitacora.db").path)` y `bitacora(...)`; `observations()` sigue dando el repositorio del JSON;
- en `Session.kt`, el campo `loaded` del estado, y el ViewModel ya lo pone a `true` con la primera lista;
- `app/src/test/kotlin/dev/bitacora/Launch.kt`, con `rule.launch(...)`, y los tests que abren la actividad ya la usan (teoría 05, apartado 4).

Y tests nuevos:

- `data/src/test/kotlin/dev/bitacora/data/RoomTest.kt`: el repositorio en la JVM, con el JSON viejo y las de partida;
- `app/src/test/kotlin/dev/bitacora/DatabaseTest.kt`: la base de datos de `AppModule` en Robolectric;
- `app/src/testField/kotlin/dev/bitacora/ArchitectureTest.kt`: `theListIsWrittenToAFile` pasa a ser `theListIsInTheDatabase`.

Con la Bitácora de la semana 29, fallan 7 de 20 en `:data:test` (`theInitialOnesOnlyOnce` ya pasa: espera una lista vacía), 1 de 83 en `testFieldDebugUnitTest` (`theListIsInTheDatabase`) y 1 de 74 en `testDemoDebugUnitTest` (`theSamplesAreInTheDatabase`; en field pasa, porque field no tiene observaciones de partida). Copia esos archivos a tu repositorio, y también los tests que ahora usan `rule.launch`.

## Pasos

1. **Lee y ejecuta**: `./gradlew :data:test`. Mira `data/build/generated/ksp/main/kotlin/dev/bitacora/data/`: ¿qué ha generado Room con la entidad de una sola columna? Y abre `data/schemas/`.
2. **La entidad** (`ObservationEntity`): una columna por campo de `Observation`; `level` como `Level` (Room lo guarda por su nombre), y la posición en `latitude` y `longitude`, que pueden ser `null` (teoría 02, apartado 1).
3. **El DAO**: `all()` ordenado por el número del código (`A-10` después de `A-2`); `@Upsert` de una lista; un `DELETE ... WHERE code NOT IN (:codes)`, y `replace`, con `@Transaction`, que borra las que no están y hace el *upsert* del resto (apartados 3 y 5).
4. **Crear la base de datos** (`bitacora()`): `setQueryCoroutineContext(Dispatchers.IO)` y un `RoomDatabase.Callback`. En `onCreate`, lo que hay en `legacy` (con `decode`), o `initial` si no hay archivo; si está roto, apártalo a `.bad` como en la semana 28. Se inserta con SQL: `connection.prepare("INSERT INTO observations VALUES (?, ?, ?, ?, ?, ?)")`. En `onOpen`, borra `legacy` (teoría 04, apartado 5).
5. **El repositorio**: `observations` es `dao.all()` con un `map` a `Observation`; `save` es `replace` con la lista traducida a entidades.
6. **El grafo**: en `AppModule`, `observations` recibe la `BitacoraDatabase` (ya la da `database()`) y devuelve un `RoomObservationRepository`. Una sola base de datos para toda la app: ¿por qué `database()` lleva `@Singleton`? (teoría 03, apartado 2).
7. **Esperar a la lista**: ejecuta `./gradlew :app:testDemoDebugUnitTest --tests '*DeepLinkTest'`. Fallan dos. En `MainActivity`, mueve `startOf` dentro de `setContent`, después de `if (!state.loaded) return@setContent`, y con `remember` (teoría 03, apartado 4).
8. **Lo que sobra**: borra `JsonFileObservationRepository` y `RepositoryTest.kt`. Sus casos (sin archivo, lo guardado sobrevive, el archivo roto se aparta) están en `RoomTest`. `encode` y `decode` se quedan: los usa la importación y el `SavedStateHandle`.
9. **Los tests y el esquema**: `./gradlew :data:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Sube `data/schemas/dev.bitacora.data.BitacoraDatabase/1.json`: a partir de aquí, un cambio de la tabla es una versión nueva con su migración. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :data:test` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo? Y `./gradlew build`.
10. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
    - en `data`, una observación sin posición ni foto vuelve sin posición ni foto (no con `0.0, 0.0`);
    - en `data`, cambiar el nivel de la A-2 con `save` y abrir otra vez: la A-2 con el nivel nuevo, y las demás igual;
    - en `data`, un JSON con un nivel que no existe (`"level": "URGENT"`) se aparta y quedan las de partida;
    - en `data`, después de importar, escribir otro `observations.json` y abrir otra vez: no se importa (ya no es `onCreate`);
    - en el ViewModel, con un repositorio que todavía no ha emitido (un `MutableSharedFlow` sin valor), `loaded` es `false`; al emitir, `true`;
    - en Robolectric, añadir una observación, cerrar la actividad y abrir otra: sigue (como `theListSurvivesClosingTheApp`, pero con `rule.launch`) y está en la base de datos.
11. **Explica en tu README** qué ganaste y qué perdiste al pasar del JSON a Room en tu dominio; por qué la entidad no es la clase del dominio; por qué el JSON se importa en `onCreate` y se borra en `onOpen`; qué haría falta si la próxima versión añadiese una columna a la tabla; y por qué la pantalla espera a `loaded` y no a que la lista tenga algo.
12. **Abre el pull request** `week-30` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, Room en `data` sin Android, la base de datos en `AppModule` con `@Singleton`, el JSON importado una vez, el esquema subido, la pantalla que espera a `loaded`, y tests con valores esperados escritos a mano.

## Pistas

- `UnsatisfiedLinkError: no sqliteJni in java.library.path` en Robolectric: has puesto `setDriver(BundledSQLiteDriver())` en `bitacora()`. En el móvil, sin `setDriver`, Room usa el SQLite de Android; el empaquetado, solo en los tests de `data` (teoría 03, apartado 1).
- `There is a problem with the query: [SQLITE_ERROR] SQL error or missing database (no such column: ...)` en `kspKotlin`: la consulta pide una columna que la entidad no tiene. Room lo comprueba al compilar.
- `theOrderIsByNumber` da `[A-1, A-10, A-2]`: ordenas por el texto del código.
- `theInitialOnesOnlyOnce` vuelve a dar las de partida: las insertas en `onOpen`, o en el repositorio, en lugar de en `onCreate`.
- `theJsonIsImportedAndDeleted` encuentra el archivo: no lo borras, o lo borras antes de que acabe `onCreate`.
- `aLinkOpensItsObservation` no encuentra `A-2`: el paso 7.
- Un test que abre la actividad se agota con `Condition still not satisfied after 5000 ms`: el ViewModel nunca pone `loaded`. ¿El `Flow` del repositorio emite algo?
- `./gradlew :data:test --tests '*RoomTest'` para ir test a test.
