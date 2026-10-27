# Ejercicio 01 — Tareas cacheables y reubicables

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

Activar la caché de build y conseguir que la tarea `countObservations` de la semana 9 salga `FROM-CACHE` después de `clean` y también desde una copia del proyecto en otra carpeta.

## Cómo está montado

`starter/sandbox/` es un build pequeño: un `Main.kt` y la tarea `countObservations`, que cuenta las filas de `data/observations.csv`. La tarea funciona y queda `UP-TO-DATE`, pero no es cacheable. Los tests (`src/test/kotlin/BuildTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal con la clase `Sandbox` (ya hecha) y ejecutan Gradle dentro. Cada `Sandbox` tiene su propia caché local, y `relocated()` crea otra copia en otra carpeta que comparte esa caché. Dos tests pasan desde el principio: son la red de seguridad de que el build sigue funcionando. Para probarlo a mano: `./gradlew -p sandbox countObservations`.

## Pasos

1. **Activa la caché** (`sandbox/gradle.properties`): `org.gradle.caching=true`.
2. **Hazla cacheable** (`sandbox/build.gradle.kts`): `@CacheableTask` en la clase `CountObservations`.
3. **Normaliza el CSV**: `@get:PathSensitive(PathSensitivity.RELATIVE)` en `source`. Sin esto, la tarea falla en cuanto la marcas como cacheable (paso 5a).
4. **Ejecuta** `./gradlew test` (7 de 7).
5. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `@PathSensitive` y ejecuta `./gradlew -p sandbox countObservations`. Copia el problema. ¿Por qué Gradle no lo exige a una tarea que no es cacheable? Restáuralo.
   - (b) Cambia `RELATIVE` por `ABSOLUTE` y ejecuta `./gradlew test`. ¿Qué test falla y con qué resultado? Ejecuta `./gradlew -p sandbox countObservations --rerun-tasks -Dorg.gradle.caching.debug=true` y copia la línea de `source`. Restáuralo.
   - (c) Ejecuta en `sandbox/` `countObservations` dos veces, luego `clean` y otra vez. Apunta el resultado de cada una. ¿Por qué la segunda es `UP-TO-DATE` y la tercera `FROM-CACHE`?
   - (d) Añade `generated=${System.currentTimeMillis()}` al texto del informe, repite (c) y mira el archivo después de `clean`. ¿Qué hora tiene? ¿Qué le pasaría a una tarea que usara este informe como entrada? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `cachingIsOnWithoutTheFlag` falla con `SUCCESS` | Falta `org.gradle.caching=true` en `sandbox/gradle.properties`: los tests construyen `sandbox/`, no `starter/` |
| `property 'source' is annotated with @InputFile but missing a normalization strategy` | Paso 3 |
| `countObservations` sale `SUCCESS` después de `clean` | Falta `@CacheableTask` encima de `abstract class CountObservations` |
| `aRelocatedCopyReusesTheResults` falla y `countObservations` sale `SUCCESS` | La normalización no es `RELATIVE` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.
