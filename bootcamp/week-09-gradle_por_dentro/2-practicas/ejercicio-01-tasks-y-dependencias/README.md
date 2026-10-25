# Ejercicio 01 — Tareas y dependencias

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Construir, tarea a tarea, un pequeño *pipeline* de empaquetado: copiar, comprimir, avisar y limpiar. Aprendes a declarar tareas con grupo y descripción, a usar `Copy`, `Zip` y `Delete`, y a relacionar tareas sin que Gradle las ejecute de más.

## Cómo está montado

Dentro de `starter/` hay **dos proyectos**:

- el **raíz** (`build.gradle.kts`, `src/test/`): solo contiene los tests, que ejecutan Gradle con TestKit. **No lo toques.**
- `sandbox/`: el build que **tú editas** (`sandbox/build.gradle.kts`). Trae `data/` con dos `.csv` y un `notes.txt`.

Para probarlo a mano desde `starter/`: `./gradlew -p sandbox hello`.

## Pasos

1. **`hello`**: imprime `Hola desde Gradle`, con `group = "bitacora"` y una descripción.
2. **`stage`** (`Copy`): copia solo los `.csv` de `data/` a `build/stage`, renombrados a `obs-<nombre>`.
3. **`bundle`** (`Zip`): empaqueta lo que produce `stage` en `build/dist/bitacora-1.0.0.zip`. Usa `from(stage)`.
4. **`announce`**: imprime `Paquete listo: <nombre del zip>`. Debe ejecutarse siempre justo después de `bundle` (`finalizedBy`), y **omitirse** con `-Pquiet` (`onlyIf` con `providers.gradleProperty`).
5. **`release`**: tarea de ciclo de vida que depende de `bundle`.
6. **`reset`** (`Delete`): borra `build/`.
7. **Experimenta** y apunta en `NOTAS.md` (en `starter/`):
   - (a) `./gradlew -p sandbox bundle --dry-run`: ¿qué orden sale y qué marca lleva cada tarea?
   - (b) Ejecuta `bundle` dos veces: ¿qué dice la segunda? Añade una línea a `data/notes.txt` y repite: ¿y ahora? ¿Por qué? Haz lo mismo con `data/a.csv`.
   - (c) Cambia `from(stage)` por `from("build/stage")` y ejecuta `bundle` con `build/` borrado. ¿Qué pasa y por qué?
   - (d) Pon un `println` suelto (fuera de cualquier tarea) y ejecuta `hello`. ¿Cuándo sale? Muévelo a un `doLast`. ¿Y ahora?
   Después deja el build como estaba.
8. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference: Copy` o `Zip` | Son tipos de Gradle; se registran con `tasks.register<Copy>("stage") { ... }` |
| `Group` o `Description` no aparecen | Faltan `group = "bitacora"` y `description = "..."` dentro del bloque |
| Los tests no encuentran `obs-a.csv` | `rename { "obs-$it" }` dentro de `from("data") { ... }`, no fuera |
| `bundle` no espera a `stage` | Usa `from(stage)` (la tarea), no una ruta |
| `announce` no corre tras `bundle` | Falta `finalizedBy("announce")` en `bundle` |
| `-Pquiet` no salta `announce` | `onlyIf { !quiet.isPresent }` con `val quiet = providers.gradleProperty("quiet")` |
| `release` no construye el zip | `dependsOn(bundle)` |
| Los tests tardan | Es normal: cada test arranca Gradle de verdad (unos segundos) |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.
