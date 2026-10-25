# Ejercicio 02 — Tareas con entradas y salidas

⏱️ 2 h · Teoría previa: 03 y 04

## Objetivo

Escribir una **clase de tarea propia** con entradas y salidas declaradas, y comprobar cuándo se ejecuta, cuándo está al día y cuándo se restaura de la caché.

## Cómo está montado

Como en el ejercicio 01: los tests están en el proyecto raíz de `starter/` (no lo toques) y el build que editas es `sandbox/build.gradle.kts`. `sandbox/data/observations.csv` es la entrada; `sandbox/settings.gradle.kts` ya configura una caché local dentro del proyecto. Para probarlo a mano, desde `starter/`: `./gradlew -p sandbox countObservations`.

## Pasos

1. **`CountLines`**: clase abstracta con `source` (`@InputFile`), `report` (`@OutputFile`) y una acción que escribe `lines=<N>` (las líneas no vacías, sin la cabecera).
2. **`countObservations`**: regístrala con las rutas por `layout`. Con el archivo de ejemplo da `lines=3`.
3. **`summary`**: depende de `countObservations` e imprime `Resumen: lines=3`. Lee el informe con un `Provider`.
4. **`skipHeader`**: una entrada `Property<Boolean>` que sale de `-PskipHeader` y vale `true` por defecto.
5. **Cachéala**: `@CacheableTask` y `@PathSensitive(PathSensitivity.NONE)`. Después de `clean`, `countObservations --build-cache` no se ejecuta.
6. **`NOTAS.md`**: añade la tarea `tasks.register("bad") { doLast { println(project.name) } }` y ejecútala con `./gradlew -p sandbox bad --configuration-cache`. (a) Copia el mensaje de error. (b) Arréglala capturando el valor antes de `doLast`. (c) Ejecuta la versión arreglada dos veces: ¿qué dice la segunda? (d) ¿Por qué la caché de configuración no permite `project` en la acción? Después borra `bad`.
7. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Class ... is not abstract` o error al instanciar | La clase y sus propiedades son `abstract`: `abstract val source: RegularFileProperty` |
| `Type mismatch` al asignar `source` | Asigna con `=` un `Provider`: `layout.projectDirectory.file("data/observations.csv")` |
| La tarea no vuelve a ejecutarse al pasar `-PskipHeader=false` | Falta `@get:Input` en `skipHeader` |
| Gradle dice que una propiedad no tiene anotación | En Kotlin se escribe `@get:Input`, `@get:InputFile`, `@get:OutputFile` (la anotación va en el *getter*) |
| `summary` imprime la ruta, no el contenido | `report.get().asFile.readText().trim()` dentro de `doLast` |
| `FROM_CACHE` no aparece | Falta `@CacheableTask`, o no pasaste `--build-cache` |
| Los números de línea no cuadran | Cuenta solo las líneas **no vacías** y quita la cabecera |

## Entrega

Los 7 tests en verde y `NOTAS.md` con la respuesta del paso 6.
