# Entidades y DAO

> Una entidad es una tabla escrita como `data class`; un DAO, una interfaz con las consultas que se pueden hacer. Room lee las dos al compilar y escribe la implementación. Lo que no se puede expresar con una anotación (una consulta con `ORDER BY`, un borrado con condición) se escribe en SQL dentro de `@Query`, y Room lo comprueba contra las tablas.

## 🎯 Objetivos

- Escribir una entidad con su clave primaria, sus índices y sus claves foráneas
- Escribir un DAO con `@Insert`, `@Update`, `@Upsert`, `@Delete` y `@Query`
- Elegir entre `suspend` y `Flow`, y saber cuándo hace falta `@Transaction`
- Leer una relación de uno a muchos con `@Relation`

## 1. La entidad

```kotlin
@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey val code: String,
    val level: Level,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val photo: String?,
)
```

- Una columna por propiedad, con el mismo nombre. `@ColumnInfo(name = "...")` le da otro, y `defaultValue` le pone un valor por defecto en SQL (teoría 04).
- Un tipo con `?` es una columna que admite `NULL`; uno sin `?`, `NOT NULL`.
- **El `enum` se guarda por su nombre**, como `TEXT`, sin escribir nada. Para un tipo que Room no conoce (una fecha, una lista), un `@TypeConverter`.
- **La clave primaria** de la Bitácora es el código (`A-1`), que ya es único. Si no lo hubiese, `@PrimaryKey(autoGenerate = true) val id: Long = 0`: SQLite pone el número al insertar, y el 0 quiere decir "todavía no tiene".

## 2. Índices y claves foráneas

En el ejercicio 01, cada cosa de la lista es de una salida:

```kotlin
@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tripId", "name", unique = true)],
)
data class Item(@PrimaryKey(autoGenerate = true) val id: Long = 0, val tripId: Long, val name: String, val packed: Boolean = false)
```

- `ForeignKey`: no puede haber una cosa de una salida que no existe, y con `CASCADE`, al borrar la salida se borran sus cosas.
- `Index(..., unique = true)`: dos cosas con el mismo nombre en la misma salida no; en salidas distintas, sí. Un índice también acelera las búsquedas por esas columnas.

> 🔬 Medido en el ejercicio 01: con el índice al revés, `Index("name", "tripId")`, KSP avisa con `tripId column references a foreign key but it is not part of an index`: borrar una salida tendría que recorrer la tabla entera. Un índice sirve para las columnas por las que empieza.
>
> Y Room activa las claves foráneas (en SQLite van apagadas por defecto). Insertar una cosa de una salida que no existe da `SQLiteException` con `Error code: 787, message: FOREIGN KEY constraint failed`; repetir un nombre, `Error code: 2067, message: UNIQUE constraint failed: items.tripId, items.name`.

## 3. El DAO

```kotlin
@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations ORDER BY CAST(SUBSTR(code, INSTR(code, '-') + 1) AS INTEGER)")
    fun all(): Flow<List<ObservationEntity>>

    @Upsert
    suspend fun upsert(observations: List<ObservationEntity>)

    @Query("DELETE FROM observations WHERE code NOT IN (:codes)")
    suspend fun deleteOthers(codes: List<String>)
}
```

| Anotación | Qué hace | Si ya existe la clave |
| --- | --- | --- |
| `@Insert` | `INSERT`; devuelve el id nuevo (`Long`) | Falla (`onConflict` lo cambia) |
| `@Update` | `UPDATE` por clave primaria | Lo cambia; si no existe, no hace nada |
| `@Upsert` | Lo inserta, o lo actualiza si ya está | Lo actualiza |
| `@Delete` | `DELETE` por clave primaria | |
| `@Query` | El SQL que escribas | |

- Los parámetros entran con `:nombre`. Una lista (`:codes`) se convierte en `?, ?, ?`.
- El orden: por el texto, `A-10` va antes que `A-2`. `CAST(SUBSTR(...) AS INTEGER)` ordena por el número, como `nextCode()`.
- Room comprueba el SQL contra las tablas: una columna que no existe es un error de `kspKotlin`.

## 4. `suspend` o `Flow`

- **`suspend`**: una vez. Para escribir, y para leer algo que no hace falta seguir (`itemsOf(trip)` antes de copiar).
- **`Flow`**: emite el resultado, y otra vez cada vez que cambia alguna de las tablas que lee la consulta. Es lo que recoge el ViewModel: lo que escribe la pantalla vuelve por el mismo camino.

> 🔬 Medido en el ejercicio 01, `pendingFollowsTheChanges`: `pending(trip)` emite `[]`, después `[linterna]` al insertar, y `[]` otra vez al marcarla guardada. Un `UPDATE` también cuenta como cambio.

Room vuelve a ejecutar la consulta con cualquier cambio en la tabla, aunque no toque las filas que lees.

> 🔬 Medido: con `pending(a)` recogido, insertar una cosa en la salida `b` hace que emita `[]` otra vez. Si importa, `distinctUntilChanged()` quita las repetidas.

## 5. Transacciones

Lo que va dentro de `@Transaction` se hace entero o no se hace, y quien lee no ve nada a medias:

```kotlin
@Transaction
suspend fun replace(observations: List<ObservationEntity>) {
    deleteOthers(observations.map { it.code })
    upsert(observations)
}
```

Sin ella, entre el `DELETE` y el `UPSERT` el `Flow` podría emitir una lista sin las que se están actualizando. Una función con cuerpo en la interfaz del DAO es una función normal; `@Transaction` la envuelve.

## 6. Relaciones

```kotlin
data class TripWithItems(
    @Embedded val trip: Trip,
    @Relation(parentColumns = ["id"], entityColumns = ["tripId"])
    val items: List<Item>,
)

@Transaction
@Query("SELECT * FROM trips ORDER BY name")
fun trips(): Flow<List<TripWithItems>>
```

`@Embedded` pone las columnas de `Trip`; `@Relation` hace una segunda consulta para las cosas de cada salida y las junta. Son dos consultas, por eso van en una transacción: entre una y otra podría cambiar algo.

> 🔬 Medido: sin `@Transaction`, KSP avisa con `w: [ksp] ... The return value includes a data class with a @Relation. It is usually desired to annotate this method with @Transaction`. Es un aviso de KSP, no del compilador de Kotlin: `allWarningsAsErrors` no lo convierte en error.

## Para recordar

- Entidad = tabla; `?` = admite `NULL`; el `enum` va por su nombre.
- Las claves foráneas y los índices únicos los hace cumplir SQLite, con una `SQLiteException`.
- `@Upsert` inserta o actualiza; `@Query` para todo lo demás, con el SQL comprobado al compilar.
- `Flow` para lo que la pantalla sigue; `suspend` para escribir o leer una vez.
- Varias operaciones que van juntas, y las relaciones: `@Transaction`.
