# Glosario — Semana 30

| Término | Definición |
| --- | --- |
| **AndroidSQLiteDriver** | El *driver* que usa el SQLite del sistema. Es el que pone Room en Android si no se llama a `setDriver` |
| **AutoMigration** | Migración que escribe Room comparando el esquema exportado de una versión con la entidad de la siguiente |
| **AutoMigrationSpec** | Lo que se le dice a una `AutoMigration` que no puede adivinar, como `@RenameColumn` o `@DeleteColumn` |
| **BundledSQLiteDriver** | *Driver* que trae su propio SQLite (`sqlite-bundled`), el mismo en todas partes. En la JVM no hay otro; en Android añade una biblioteca nativa por ABI |
| **Callback** | `RoomDatabase.Callback`: `onCreate` se llama una vez, al crear el archivo; `onOpen`, cada vez que se abre |
| **Clave foránea** | Columna que apunta a la clave primaria de otra tabla; SQLite no deja que apunte a algo que no existe. `CASCADE` borra las filas con su padre |
| **DAO** | *Data Access Object*: la interfaz con las consultas, de la que Room genera la implementación |
| **@Database** | La clase de la base de datos: sus entidades, su versión y sus migraciones automáticas |
| **defaultValue** | En `@ColumnInfo`, el `DEFAULT` de SQL de una columna; lo reciben las filas que ya había al añadirla |
| **Driver** | La pieza que habla con SQLite (`SQLiteDriver`); Room 3 lo usa en lugar de `SupportSQLiteOpenHelper` |
| **@Embedded** | Pone las columnas de otra clase dentro de una entidad o de un resultado |
| **@Entity** | Una tabla escrita como `data class`: una columna por propiedad |
| **Esquema exportado** | El JSON de cada versión en `schemas/`, con sus `CREATE TABLE`; se sube al repositorio |
| **fallbackToDestructiveMigration** | Si falta una migración, borrar las tablas y empezar de cero; solo para lo que se puede volver a bajar |
| **Índice** | Estructura que acelera las búsquedas por unas columnas; `unique` impide repetir la combinación. Sirve para las columnas por las que empieza |
| **loaded** | En el estado de la Bitácora, si ya ha llegado la primera lista de la base de datos; no es lo mismo que una lista vacía |
| **Migration** | Migración a mano: SQL que pasa la base de datos de una versión a la siguiente |
| **MigrationTestHelper** | De `room3-testing`: crea una base de datos de una versión con su esquema exportado, migra y comprueba las tablas |
| **@PrimaryKey** | La columna que identifica cada fila; `autoGenerate` deja que la ponga SQLite |
| **@Query** | Una consulta en SQL dentro del DAO, comprobada contra las tablas al compilar |
| **@Relation** | Junta las filas de otra tabla (las cosas de una salida) con una segunda consulta; en Room 3, con listas de columnas |
| **Room 3** | La versión de Room en `androidx.room3`: multiplataforma, solo KSP y Kotlin, con *drivers* y corrutinas |
| **setQueryCoroutineContext** | Dónde trabajan las consultas de Room; en la Bitácora, `Dispatchers.IO` |
| **SQLite** | La base de datos en un archivo que viene en cada Android y que Room usa por debajo |
| **@Transaction** | Lo que va dentro se hace entero o no se hace, y quien lee no lo ve a medias |
| **@Upsert** | Inserta la fila, o la actualiza si la clave ya existe |
| **Versión** | El número de `@Database`; SQLite lo guarda en el archivo, y Room busca migraciones si es menor que el del código |
