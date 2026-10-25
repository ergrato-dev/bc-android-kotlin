# Ejercicio 01 — Módulos y dependencias

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Convertir tres carpetas con código en un build de **tres módulos** (`core`, `data` y `app`) y comprobar cómo se relacionan: el orden de construcción, qué se recompila y qué ve cada módulo.

## Cómo está montado

Igual que la semana pasada: los tests están en el proyecto raíz de `starter/` (no lo toques) y el build que editas está en `sandbox/`. Allí ya tienes el código de los tres módulos y el `build.gradle.kts` de `core`. Para probarlo a mano, desde `starter/`: `./gradlew -p sandbox :app:run`.

## Pasos

1. **`settings.gradle.kts`**: incluye los tres módulos.
2. **`data/build.gradle.kts`**: la estructura de `core`, más `api(project(":core"))` y la biblioteca de serialization como `implementation`.
3. **`app/build.gradle.kts`**: `application`, `implementation(project(":data"))` y `mainClass`. **No declares `core`** en `app`.
4. **Experimenta** y apunta en `NOTAS.md` (en `starter/`):
   - (a) Cambia `api(project(":core"))` por `implementation(project(":core"))` y ejecuta `./gradlew -p sandbox :app:compileKotlin`. Copia el primer error. ¿Por qué ocurre? Pon `api` otra vez.
   - (b) Añade `implementation(project(":data"))` a las dependencias de `core` y ejecuta `./gradlew -p sandbox :core:compileKotlin`. Copia el mensaje. ¿Qué dos formas hay de resolver un ciclo? Quita la línea.
   - (c) Cambia el **cuerpo** de `countByLevel` (por ejemplo, escribe `this.count`) y ejecuta `:app:compileKotlin`: ¿qué tareas se ejecutan y cuáles quedan `UP-TO-DATE`? Después añade una función pública a `core` y repite. ¿Qué cambia y por qué?
   - (d) `./gradlew -p sandbox :app:dependencies --configuration compileClasspath` y la misma con `runtimeClasspath`: ¿qué aparece en una y no en la otra?
   Después deja el código como estaba.
5. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Project ':app'` no sale en `projects` | Falta el `include(":app")` o está escrito sin los dos puntos |
| `Cannot access class 'Observation'` en `app` | `data` usa `implementation(project(":core"))`: tiene que ser `api` |
| `Unresolved reference: serialization` en `data` | Falta el plugin `kotlin("plugin.serialization")` (sin versión: está en el raíz) |
| `Plugin ... was not found` | Los plugins se aplican sin versión: `kotlin("jvm")`, no `kotlin("jvm") version "..."` (la versión está en el raíz) |
| `No tests found` en data | Faltan las dependencias de prueba (`kotlin("test")`, el BOM de JUnit y `junit-platform-launcher`) |
| El test de la salida falla por un salto de línea | El programa imprime dos líneas; no cambies `Main.kt` |
| Los tests tardan | Es normal: cada test arranca Gradle de verdad |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 4.
