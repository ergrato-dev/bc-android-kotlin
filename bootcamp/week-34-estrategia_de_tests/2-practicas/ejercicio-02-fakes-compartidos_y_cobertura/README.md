# Ejercicio 02 — Fakes compartidos y cobertura

⏱️ 1 h 45 min · Teoría previa: 02, 03 y 06

## Objetivo

Un fake que vive en los tests de quien lo usa se copia, y cada copia miente a su manera. Al terminar, el fake de un almacén de notas vive en los `testFixtures` del módulo que define el almacén. Un contrato comprueba que se porta como el de verdad, y Kover junta la cobertura de los dos módulos y para `check` si baja del 80 %. Es la Bitácora del proyecto en pequeño.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM cuyos tests (`src/test/kotlin/NotesTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal y ejecutan Gradle dentro con TestKit, como en la semana 33.

`sandbox/` tiene dos módulos:

- `:store`: la interfaz `NoteStore` y `FileNoteStore`, una nota por línea en un archivo, sin repetir y en orden alfabético. Tiene un test propio.
- `:report`: `summary` e `import`, que usan un `NoteStore`. Sus tests usan `FakeNoteStore`, que está en `report/src/test`.

Compila, y al empezar fallan 6 de 8.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué pasan ya `theReportTestsPass` y `theFixturesAreNotShipped`? Compara `FakeNoteStore.add` con `FileNoteStore.add`: ¿en qué tres cosas se porta distinto?
2. **Los fixtures**: `java-test-fixtures` en `store/build.gradle.kts`, `FakeNoteStore.kt` a `store/src/testFixtures/kotlin/dev/bitacora/notes/`, y en `:report`, `testImplementation(testFixtures(project(":store")))` (teoría 03).
3. **El contrato**: en `store/src/test`, una clase abstracta `NoteStoreContract` con al menos 4 tests de lo que dice el comentario de `NoteStore` (empieza vacío, orden alfabético, sin repetir, sin vacías, sin espacios, borrar...). Después, dos subclases: `FakeNoteStoreContractTest` y `FileNoteStoreContractTest`, esta con `@TempDir` (teoría 02, apartado 4). Ejecuta `./gradlew :store:test` dentro de `sandbox/` con el `gradlew` de `starter/` y `-p sandbox`. ¿Cuáles fallan con el fake?
4. **Arregla el fake** hasta que el contrato pase con los dos.
5. **Kover**: el plugin `org.jetbrains.kotlinx.kover` versión `0.9.11` en el raíz del sandbox (en `plugins { }`, con versión) y en los dos módulos (sin versión). En el raíz, `kover(project(":store"))` y `kover(project(":report"))`, y la regla `minBound(80)`. En `:store`, fuera los `testFixtures` (teoría 06, apartados 1, 2 y 5).
6. **`check`**: ¿ejecuta `./gradlew check` el `koverVerify` del raíz? Compruébalo con `-m`. Si no lo ejecuta, ¿qué le falta al raíz? (Teoría 06, apartado 5.)
7. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
8. **`NOTAS.md`** (en `starter/`):
   - (a) Escribe en `ReportTest` un test de `import` con una línea repetida y una en blanco (`"poda\npoda\n\nriego"`). ¿Qué devolvía con el fake del principio, y qué devuelve ahora? ¿Qué te dice eso de los cuatro tests que ya pasaban?
   - (b) Quita `excludedSourceSets.add("testFixtures")`. ¿Qué test de `NotesTest` falla? Mira el porcentaje de líneas (`./gradlew koverLog -p sandbox`): ¿cambia? ¿Por qué aquí no, y en qué proyecto sí cambiaría? Restáuralo.
   - (c) En una copia, quita `FileNoteStoreContractTest` y compara `./gradlew :store:koverLog -p sandbox` con `./gradlew koverLog -p sandbox`. ¿Cuál baja más, y por qué el del raíz sigue por encima del 80 %? (Teoría 06, apartado 3.)
   - (d) ¿Por qué el contrato está en `src/test` y no en `src/testFixtures`? ¿Qué haría falta para ponerlo en `testFixtures`, y quién lo usaría?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'FakeNoteStore'` en `:report` | Falta `testImplementation(testFixtures(project(":store")))`, o el fake no está en `src/testFixtures/kotlin` |
| `no hay resultados de FakeNoteStoreContractTest en :store` | Las subclases se tienen que llamar así, en el paquete `dev.bitacora.notes` |
| `Plugin [id: 'org.jetbrains.kotlinx.kover'] was not found` en un módulo | En los módulos va sin versión, pero el raíz tiene que cargarlo con versión: sin `apply false`, porque el raíz también lo usa |
| `el fake cuenta en la cobertura` | Falta `excludedSourceSets.add("testFixtures")` en `:store` |
| `checkVerifiesTheCoverage` falla con `null` | El raíz no tiene tarea `check`: paso 6 |
| `Rule violated` sin haber añadido nada | Tu contrato o tus tests no ejecutan bastante: mira `build/reports/kover/html/index.html` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 8.
