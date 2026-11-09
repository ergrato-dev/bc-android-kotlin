# Ejercicio 01 — El grafo de la salida

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

La lista de una salida al campo tiene dos formas de guardarse: en memoria y en un archivo. Quien la usa (`Planner`) no sabe cuál recibe. Al terminar, un grafo de Dagger decide que es la del archivo, en la carpeta que se le da al crearlo, y que hay una sola para todo el componente. Sin Android y sin Hilt: Dagger a secas, para ver lo que Hilt hace por ti.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con Dagger 2.60.1 y KSP 2.3.13; el compilador de Dagger corre en `ksp`, con guava restringida a 33.7.2-jre. Trae hechos `Checklist.kt` (`ChecklistRepository`, `InMemoryChecklistRepository`, `FileChecklistRepository` y `Planner`, todos con `@Inject constructor`) y `TripComponent.kt`, con un `TripModule` que da la de memoria, el componente y su `Factory` con `@BindsInstance dir: File`, y comentarios `Paso N`.

Los tests (`src/test/kotlin/dev/bitacora/GraphTest.kt`, no los toques) crean el componente con `DaggerTripComponent.factory().create(dir)`, la clase que genera Dagger. Al empezar, fallan 4 de 6.

## Pasos

1. **La del archivo**: en `TripModule`, el `@Binds` recibe un `FileChecklistRepository` (teoría 02, apartado 3). ¿De dónde saca Dagger el `File` que pide su constructor?
2. **Una sola**: `@Singleton` en el `@Binds` y en el `@Component` (teoría 03, apartado 2). `aPlannerIsNewEachTime` y `twoComponentsTwoRepositories` tienen que seguir en verde: no le pongas scope a `Planner`.
3. Ejecuta `./gradlew test` (6 de 6) y `./gradlew build`.
4. **`NOTAS.md`** (en `starter/`). Cada apartado rompe el grafo a propósito: ejecuta `./gradlew build`, apunta la primera línea con `[Dagger/...]` y restáuralo.
   - (a) Borra el `@Binds`. ¿Qué error da, en qué tarea, y qué camino enseña?
   - (b) Añade un segundo `@Binds` que dé la de memoria. ¿Qué error?
   - (c) Quita `@Singleton` del `@Component` y déjalo en el `@Binds`. ¿Qué error, y por qué tiene sentido?
   - (d) Abre `build/generated/ksp/main/java/dev/bitacora/DaggerTripComponent.java` (Dagger genera Java). Busca cómo crea el repositorio y cómo crea el `Planner`. ¿Qué diferencia hay entre los dos, y a qué se parece del `BitacoraApp` de la semana 28?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'DaggerTripComponent'` | El grafo no compila y Dagger no la ha generado: busca más arriba la línea con `[Dagger/...]` |
| `theRepositoryIsTheFileOne` sigue en rojo | El `@Binds` sigue recibiendo la de memoria |
| `twoPlannersShareIt` en rojo | Falta `@Singleton` en el `@Binds` (o en `FileChecklistRepository`) |
| `aPlannerIsNewEachTime` en rojo | Le has puesto `@Singleton` a `Planner`: no lo necesita |

## Entrega

Los 6 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 4.
