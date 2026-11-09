# Rúbrica de Evaluación — Semana 30: Room y migraciones

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: las observaciones en Room |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué se queda corto en el archivo JSON de la semana 29 cuando hay muchas observaciones? ¿Qué cuatro cosas pone Room encima de SQLite? | 10 |
| 2 | ¿Qué cambia en Room 3 frente a Room 2? ¿Por qué eso permite que `data` tenga la base de datos sin depender de Android? | 10 |
| 3 | ¿Por qué `ObservationEntity` no es `Observation`? ¿Cómo guarda Room un `enum` y un `Double?`? | 10 |
| 4 | ¿Qué hacen una `ForeignKey` con `CASCADE` y un `Index` único? ¿Quién lanza la excepción si se incumplen, y qué dice? ¿Por qué importa el orden de las columnas del índice? | 10 |
| 5 | `@Insert`, `@Update` y `@Upsert`: ¿qué hace cada uno si la clave ya existe? ¿Cuándo una consulta devuelve `Flow` y cuándo es `suspend`? ¿Cuándo vuelve a emitir un `Flow` de Room? | 10 |
| 6 | ¿Para qué sirve `@Transaction` en `replace` y en una consulta con `@Relation`? ¿Qué pasa si falta en la segunda? | 10 |
| 7 | `AndroidSQLiteDriver` y `BundledSQLiteDriver`: ¿cuál usa la Bitácora en el móvil y cuál en los tests de `data`, y por qué? ¿Qué pasa con el empaquetado en Robolectric? ¿Y con dos instancias de la base de datos sobre el mismo archivo? | 10 |
| 8 | ¿Por qué la Bitácora necesita `loaded`? ¿Qué fallaba sin él? ¿Por qué no basta con mirar si la lista está vacía? | 10 |
| 9 | Columna nueva, columna renombrada, y `packed` que pasa a `status`: ¿qué migración necesita cada una? ¿Qué pasa si falta una, y qué haría `fallbackToDestructiveMigration(true)`? ¿Por qué se sube `schemas/` al repositorio? | 10 |
| 10 | ¿Por qué el JSON se importa en `onCreate` y se borra en `onOpen`? En Robolectric, ¿por qué un test que abre la actividad tiene que esperar a `loaded`, y por qué hay que atender el hilo principal mientras espera? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Se reescribe entero con cada cambio, para buscar hay que leerlo todo, las relaciones van a mano, dos cambios a la vez necesitan un `Mutex`, y no tiene versiones para cambiar de formato. Room comprueba el SQL al compilar, convierte filas en objetos, emite un `Flow` cuando cambia una tabla y lleva versiones con migraciones que se prueban.
2. Paquete `androidx.room3`, solo KSP y solo genera Kotlin, multiplataforma, un *driver* de SQLite en lugar de `SupportSQLiteOpenHelper`, corrutinas primero (consultas, migraciones y *callbacks* `suspend`), y algún nombre (`room3 { }`, `@Relation` con listas). Al ser multiplataforma, `data` usa `room3-runtime-jvm` y se prueba en la JVM; la app recibe la variante de Android y crea la base de datos con su builder.
3. `core` no sabe de Room, y la tabla puede cambiar (una columna, un nombre) sin tocar el dominio; el repositorio traduce. El `enum`, por su nombre como `TEXT`, sin converter. Un `Double?`, como una columna `REAL` que admite `NULL`.
4. `ForeignKey`: no puede haber una cosa de una salida que no existe, y `CASCADE` borra las cosas con la salida. `Index` único: no se repite la combinación. Lo hace cumplir SQLite: `SQLiteException` con `FOREIGN KEY constraint failed` (787) o `UNIQUE constraint failed: items.tripId, items.name` (2067). Un índice sirve para las columnas por las que empieza: con `("name", "tripId")`, buscar por `tripId` (al borrar una salida) recorre la tabla, y KSP avisa.
5. `@Insert` falla (salvo `onConflict`); `@Update` la cambia, y si no existe no hace nada; `@Upsert` la inserta o la actualiza. `Flow` para lo que se sigue (la pantalla); `suspend` para escribir o leer una vez. Vuelve a emitir con cualquier cambio en las tablas que lee la consulta, aunque no toque sus filas (con el mismo resultado, emite igual).
6. En `replace`: el `DELETE` y el `UPSERT` se hacen juntos o nada, y quien lee no ve la tabla a medias. En `@Relation`: son dos consultas, y entre una y otra podría cambiar algo. Sin ella, KSP avisa (`It is usually desired to annotate this method with @Transaction`); es un aviso de KSP, no de Kotlin, y no rompe el build.
7. En el móvil, el del sistema (por defecto, sin `setDriver`): no añade nada al APK. En `data`, el empaquetado: en la JVM no hay SQLite del sistema. El empaquetado añade 1 MB por ABI, y en Robolectric no carga (`UnsatisfiedLinkError: no sqliteJni`), porque sus bibliotecas son para Android. Con dos instancias, lo que escribe una no hace emitir los `Flow` de la otra: `@Singleton`.
8. La lista llega de Room en otro hilo, después del primer fotograma. Sin `loaded`, `startOf(intent)` buscaba la A-2 en una lista vacía y abría la sesión (`aLinkOpensItsObservation` y `backFromALinkGoesToTheSession`). Una lista vacía puede ser real (la persona lo borró todo); `loaded` dice si ya se sabe.
9. Nueva: `AutoMigration` y `@ColumnInfo(defaultValue = ...)`. Renombrada: `AutoMigration` con un *spec* `@RenameColumn`. `packed` a `status`: una `Migration` a mano con SQL (`ADD COLUMN`, `UPDATE`, `DROP COLUMN`). Si falta: `A migration from 3 to 4 was required but not found`, al primer acceso, sin borrar nada. Con `fallbackToDestructiveMigration(true)`, borraría las tablas y empezaría de cero: vale para una caché, no para las observaciones. `schemas/` es la única copia de cómo era cada versión: de ahí salen las automáticas y los tests.
10. `onCreate` se llama una sola vez, al crear el archivo, dentro de la transacción que crea las tablas: el JSON entra una vez, y las de partida no vuelven si la persona las borra. En `onOpen` la creación ya está guardada: borrar en `onCreate` perdería el JSON si la creación fallase después. En Robolectric, `waitForIdle` espera al hilo principal y no a `Dispatchers.IO`, y la pantalla está en blanco hasta `loaded`. Room deja la lista para el hilo principal, que en Robolectric es el del test: si nadie lo atiende (`shadowOf(Looper.getMainLooper()).idle()`), `waitUntil` se agota.

**Ejercicio 01, paso 7**: (a) Antes, ninguna: la tabla no tiene clave foránea, y el test falla porque esperaba una. Después, `SQLiteException` con `Error code: 787, message: FOREIGN KEY constraint failed`; la lanza SQLite (Room activa las claves foráneas). (b) `tripId column references a foreign key but it is not part of an index`: al borrar una salida, SQLite busca sus cosas por `tripId`, y sin un índice que empiece por esa columna recorre la tabla entera. (c) `The return value includes a data class with a @Relation. It is usually desired to annotate this method with @Transaction`; es un `w: [ksp]`, y `allWarningsAsErrors` es del compilador de Kotlin. (d) Room emite desde su hilo; en el dispatcher de `runTest` y con el tiempo virtual, el test no ve las emisiones (o se queda colgado). Con un `Channel`, cada `receive()` espera a la siguiente emisión de verdad, sin adivinar cuánto tarda.

**Ejercicio 02, paso 6**: (a) `New NOT NULL column'quantity' added with no default value specified`. El `= 1` es de Kotlin, para los objetos nuevos; las filas que ya están en el archivo necesitan un `DEFAULT` en SQL. (b) Un *spec* con `@RenameColumn` o `@DeleteColumn`: Room ve una columna que desaparece y otra que aparece, y lo mismo podría ser un borrado y una columna nueva. (c) `fromThreeToFourPackedBecomesStatus`, `fromOneToFourInOneGo` y `theAppOpensAVersionOneFile`: `Migration didn't properly handle: items`, porque sobra `packed`. Lo detecta al terminar la migración, comparando la tabla con el esquema de la versión 4. (d) Al usarla (`count()`), no al crearla. La persona vería la app fallar al abrir, con sus datos intactos en el archivo; con `fallbackToDestructiveMigration(true)`, la app abriría vacía y habría perdido su libreta.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La libreta de la salida (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `ForeignKey` con `CASCADE` e `Index` único que empieza por `tripId`; `@Transaction` en `trips` y en `copy`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado, con los mensajes de (a), (b) y (c) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Las versiones de la libreta (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 en verde | 13 |
| Dos `AutoMigration` (la segunda con *spec*) y `MIGRATION_3_4` en SQL; los cuatro esquemas en `schemas/`, con `1.json` sin cambios (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con los mensajes de (a), (b) y (c) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Las observaciones en Room

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 29 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: sin posición ni foto, cambiar un nivel, un nivel que no existe, el JSON solo una vez, `loaded` en el ViewModel, cerrar y abrir en Robolectric | 25 |
| Room | Entidad, DAO y base de datos en `data`, sin Android; el orden por el número del código; `replace` con `@Transaction`; la base de datos en `AppModule` con `@Singleton` y el builder de Android; sin `BundledSQLiteDriver` en la app | 25 |
| Datos y carga | El JSON importado en `onCreate` y borrado en `onOpen`, el roto apartado; las de partida solo al crear; `schemas/.../1.json` subido; la pantalla espera a `loaded` para resolver el enlace | 15 |
| Adaptación al dominio | Las columnas y el orden de tu dominio; README con lo que pide el paso 11 | 15 |
| Entrega | Rama `week-30`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `core` o `data` dependen de Android, o `core` de Room | -10 |
| La base de datos sin `@Singleton`, o creada fuera del grafo | -10 |
| Las de partida se insertan en cada arranque, o el JSON se importa más de una vez | -10 |
| `fallbackToDestructiveMigration` con las observaciones | -10 |
| Un test de la semana 29 o del starter borrado o debilitado para que pase (salvo `RepositoryTest`, en el paso 8) | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-30` → `main` en el repositorio del aprendiz.
