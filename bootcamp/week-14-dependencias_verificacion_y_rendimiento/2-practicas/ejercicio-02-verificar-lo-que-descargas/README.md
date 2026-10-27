# Ejercicio 02 — Verificar lo que descargas

⏱️ 2 h · Teoría previa: 03

## Objetivo

Hacer que el build compruebe, con sha256, cada archivo que usa (bibliotecas y plugins), y practicar el ciclo de añadir una dependencia: el build falla, regeneras el archivo y revisas qué ha entrado.

## Cómo está montado

`starter/sandbox/` es el ejercicio 01 resuelto, todavía sin `gradle/verification-metadata.xml`. Los tests (`src/test/kotlin/BuildTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal. Algunos estropean a propósito un sha256 del archivo (`tamper`), como si alguien hubiera sustituido ese artefacto, y comprueban que el build falla. Los 7 empiezan en rojo: sin el archivo, nadie verifica nada.

## Pasos

1. **Genera el archivo** desde `starter/`:
   ```bash
   ./gradlew -p sandbox --write-verification-metadata sha256 help
   ```
   Ábrelo (`sandbox/gradle/verification-metadata.xml`) y busca `kotlin-gradle-plugin`.
2. **Comprueba que verifica**: `./gradlew -p sandbox build` sigue en verde.
3. **Añade una dependencia**: `kotlinx-serialization-cbor` al catálogo (sin versión, la pone el BOM de serialization) y a `dependencies { }`. Ejecuta `./gradlew -p sandbox build`: tiene que fallar. Lee qué artefactos dice que faltan.
4. **Actualiza el archivo**: repite el comando del paso 1 y mira el diff (`git diff` si tu `sandbox/` está en un repositorio, o compara a mano). Solo tienen que entrar entradas de `cbor`.
5. **Ejecuta** `./gradlew test` (7 de 7).
6. **`NOTAS.md`** (en `starter/`):
   - (a) ¿Cuántos componentes tiene el archivo y cuántos son tuyos (los que declaras en `dependencies { }`)? ¿Por qué están los del plugin de Kotlin, si no son bibliotecas de tu programa?
   - (b) Cambia a mano un sha256 de `kotlinx-serialization-json-jvm-1.12.0.jar` y ejecuta `./gradlew -p sandbox build --dependency-verification lenient`. ¿Qué pasa? ¿Cuándo usarías `lenient` y cuándo nunca? Restáuralo.
   - (c) Quita `cbor` y vuelve a generar el archivo. ¿Siguen sus entradas? ¿Qué harías con ellas? Vuelve a dejar `cbor`.
   - (d) Un día, sin cambiar nada, el CI falla con `One artifact failed verification` en un jar que ya estaba en el archivo. Escribe, en orden, qué harías y qué no harías.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| Los tests no encuentran el archivo, pero lo generaste | Está en `starter/gradle/`: te faltó `-p sandbox`. Bórralo de ahí y repite el paso 1 |
| `theMetadataUsesSha256` falla por `sha1` o `md5` | Lo generaste con otro algoritmo: bórralo y repite el paso 1 con `sha256` |
| `theBuildPassesWithCbor` falla con `Dependency verification failed` | No regeneraste el archivo después de añadir `cbor` (paso 4) |
| `theBuildPassesWithCbor` falla sin error de verificación | `cbor` no está en `runtimeClasspath`: revisa el catálogo y `dependencies { }` |
| `... no está en gradle/verification-metadata.xml` en un test | El archivo no tiene ese artefacto: vuelve a generarlo con el build del ejercicio 01 intacto |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 6.
