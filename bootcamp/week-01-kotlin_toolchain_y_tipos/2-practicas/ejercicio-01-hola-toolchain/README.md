# Ejercicio 01 — Compila tu primer programa con Gradle

⏱️ 1 h 30 min · Teoría previa: 01, 02 y 03

## Objetivo

Poner en verde 4 tests, ver con tus ojos lo que genera el compilador, y provocar un fallo por `allWarningsAsErrors`.

## Pasos

Trabaja dentro de `starter/`.

1. **Comprueba la toolchain.** Ejecuta `./gradlew --version`. Debe decir `Gradle 9.8.2`, `Kotlin 2.4.21` y una JVM 21. Anota las tres líneas.
2. **Mira el estado inicial.** `./gradlew test`. Los 4 tests fallan con `NotImplementedError`: así se ve un test en rojo.
3. **`saludo`.** Abre `src/main/kotlin/Main.kt` e implementa `saludo(nombre)`: `"Hola, <nombre>"`, o `"Hola, mundo"` si el nombre está en blanco (`isBlank()`).
4. **`resumenToolchain`.** Devuelve `"Kotlin X.Y.Z / JDK N"` con `KotlinVersion.CURRENT` y `Runtime.version().feature()`. Ejecuta `./gradlew test`: 4 de 4 en verde. Luego `./gradlew run`.
5. **Mira por dentro.**
   ```bash
   ./gradlew build
   ls build/classes/kotlin/main build/libs
   javap -c -cp build/classes/kotlin/main MainKt
   ```
   Responde: ¿cómo se llama la clase generada? ¿Qué llamada aparece al principio de `saludo` y qué protege?
6. **Provoca un fallo.** Añade al final de `Main.kt`:
   ```kotlin
   @Deprecated("usa saludo")
   fun viejo() = 1
   fun usaViejo() = viejo()
   ```
   Ejecuta `./gradlew compileKotlin` y copia las dos líneas `e:` que salen. Borra el código y comprueba que vuelve a compilar.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Permission denied: ./gradlew` | `chmod +x gradlew` |
| `No matching toolchains found ... 21` | Instala Temurin 21 (`docs/setup.md`) y comprueba `java -version` |
| La descarga de Gradle falla | Repite `./gradlew --version` |
| El IDE marca todo en rojo | "Reload Gradle project" |

## Entrega

Los 4 tests en verde y las respuestas de los pasos 1, 5 y 6 en un archivo `NOTAS.md` dentro del starter.
