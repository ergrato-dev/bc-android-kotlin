# Ejercicio 02 — Las versiones de la libreta

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

La libreta de la versión 1 ya está en los móviles: una tabla `items` con `trip`, `name` y `packed`. La app va a cambiar tres veces: una cantidad por cosa, `trip` pasa a llamarse `tripName`, y `packed` (sí o no) se convierte en `status`. Al terminar, una libreta de cualquier versión se abre con la última sin perder nada, y cada paso tiene su test.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con Room 3.0.3, `room3-testing` y el SQLite empaquetado. `Versiones.kt` tiene la versión 1: `Item`, un DAO con `count()` y `LibretaDatabase` con `version = 1`. Su esquema, `schemas/dev.bitacora.LibretaDatabase/1.json`, ya está: es el de los móviles, **no lo toques**. `MIGRATION_3_4` existe y no hace nada.

Los tests (`src/test/kotlin/dev/bitacora/VersionesTest.kt`, no los toques) crean una libreta de la versión 1 con `MigrationTestHelper` y SQL, y la llevan hacia delante. Al empezar, fallan 5 de 6: faltan los esquemas de las versiones 2, 3 y 4.

## Pasos

1. **Lee** `schemas/.../1.json`: busca el `createSql` de `items`. Ejecuta `./gradlew test` y mira el primer error.
2. **Versión 2**: `quantity: Int = 1` en `Item`, con `@ColumnInfo(defaultValue = "1")`; `version = 2` y `autoMigrations = [AutoMigration(from = 1, to = 2)]`. Compila y busca `2.json` (teoría 04, apartado 2).
3. **Versión 3**: `trip` pasa a ser `tripName`. Un *spec* con `@RenameColumn(tableName = "items", fromColumnName = "trip", toColumnName = "tripName")`, una `AutoMigration(from = 2, to = 3, spec = ...)` y `version = 3`.
4. **Versión 4**: `enum class Status { PENDING, PACKED }`; en `Item`, `packed` se va y entra `status: Status = Status.PENDING` con `@ColumnInfo(defaultValue = "'PENDING'")` (con comillas: es SQL). `MIGRATION_3_4` añade la columna, pone `PACKED` donde `packed = 1` y borra `packed`. `version = 4` (apartado 3).
5. Ejecuta `./gradlew test` (6 de 6) y `./gradlew build`. Comprueba que `schemas/` tiene `1.json` a `4.json` y que `1.json` no ha cambiado (`git diff`).
6. **`NOTAS.md`** (en `starter/`):
   - (a) En el paso 2, quita el `defaultValue` y compila. ¿Qué dice KSP? ¿Por qué no le sirve el `= 1` de Kotlin? Restáuralo.
   - (b) En el paso 3, quita el *spec* de la `AutoMigration`. ¿Qué pide Room, y por qué no puede saber él solo que es la misma columna? Restáuralo.
   - (c) Comenta el `DROP COLUMN` de `MIGRATION_3_4`. ¿Qué tests fallan, y qué dice el error? ¿En qué momento lo detecta Room? Restáuralo.
   - (d) `withoutTheManualMigrationItRefuses`: ¿en qué línea falla, al crear la base de datos o al usarla? ¿Qué le pasaría a una persona con la versión 3 instalada si la app saliese sin `addMigrations(MIGRATION_3_4)`? ¿Y con `fallbackToDestructiveMigration(true)`?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `NoSuchFileException: schemas/.../2.json` | Falta `version = 2` en `@Database`, o no has compilado desde el cambio |
| `New NOT NULL column'quantity' added with no default value specified` | `@ColumnInfo(defaultValue = "1")` |
| `AutoMigration Failure: Please declare an interface extending 'AutoMigrationSpec'` | El *spec* con `@RenameColumn`, en la `AutoMigration` de la 2 a la 3 |
| `Migration didn't properly handle: items` | La tabla migrada no es la entidad: mira la columna de más o de menos en `Expected` y `Found` |
| `1.json` aparece cambiado en `git diff` | Has cambiado la versión 1: vuelve a `version = N` y restaura el archivo con `git checkout` |

## Entrega

Los 6 tests en verde, `./gradlew build` en verde, los cuatro esquemas en `schemas/` y `NOTAS.md` con las respuestas del paso 6.
