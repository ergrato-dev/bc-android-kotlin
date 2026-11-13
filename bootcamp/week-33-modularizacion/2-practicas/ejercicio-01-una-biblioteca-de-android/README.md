# Ejercicio 01 — Una biblioteca de Android

⏱️ 2 h · Teoría previa: 03

## Objetivo

La ayuda de la Bitácora (una pantalla con un título, un texto y cuántos consejos hay hoy) vive en su propio módulo, `:feature:help`, una biblioteca de Android. Al terminar, la biblioteca tiene sus textos en español y en inglés, su actividad declarada en su manifiesto y sus tests en Robolectric. La app la usa sin repetir nada: cambia el título por el suyo y quita el permiso que la biblioteca pide y la app no necesita.

## Cómo está montado

`starter/` tiene dos módulos:

- **`:feature:help`** (`com.android.library`, namespace `dev.bitacora.help`). En `Help.kt` están `HelpActivity` (un `TextView` con el título y el texto) y `tips()`, que tiene un `TODO`. Los textos están en `res/values` (español) y `res/values-en`, donde falta el plural. El manifiesto pide `VIBRATE`.
- **`:app`** (`com.android.application`, namespace `dev.bitacora`), sin código: depende de la biblioteca, y su manifiesto y sus recursos son lo que se prueba.

Hay tests en cada módulo. `feature/help/src/test/.../HelpTest.kt` (5) prueba la biblioteca sola, y `app/src/test/.../AppTest.kt` (6) la prueba dentro de la app. No los toques. Compila, y al empezar fallan los 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test --continue`. Los 5 de `HelpTest` fallan con `IllegalArgumentException: Path is not a file: .../android-all-instrumented-17-...jar`. ¿Por qué pide Robolectric la versión 17, si la app tiene `targetSdk = 36`? Arréglalo en `testOptions` de la biblioteca (teoría 03, apartado 6).
2. **El plural** (`tips`): `resources.getQuantityString` con `R.plurals.tips`. ¿De qué paquete es ese `R`? Escribe el nombre completo de la clase en `NOTAS.md`.
3. **En inglés**: el plural `tips` en `values-en` («1 tip», «2 tips»). Ejecuta `./gradlew :feature:help:lintDebug` antes y después: ¿qué error da lint sin él?
4. **La actividad**: declara `HelpActivity` en el manifiesto de la biblioteca, sin exportar. ¿Por qué `android:name=".HelpActivity"` acaba en `dev.bitacora.help.HelpActivity` y no en `dev.bitacora.HelpActivity`, si la app es `dev.bitacora`? Con eso pasan `HelpTest` entero y `theHelpActivityIsInTheApp`, sin tocar el manifiesto de la app.
5. **El título de la app**: en `app/src/main/res/values` y `values-en`, un `help_title` con «Ayuda de la Bitácora» y «Bitácora help». No toques la biblioteca. ¿Por qué `HelpActivity`, que lee `R.string.help_title` de la biblioteca, enseña el de la app?
6. **El permiso**: la app no vibra. Quita `VIBRATE` del manifiesto final desde el manifiesto de la app, con `tools:node` (teoría 03, apartado 5).
7. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
8. **`NOTAS.md`** (en `starter/`):
   - (a) Pon el namespace de la biblioteca a `dev.bitacora`, el mismo de la app, y ejecuta `./gradlew build`. Copia el error. ¿En qué tarea sale, y por qué no puede haber dos? Restáuralo.
   - (b) En `AppTest`, `theAppRenamesTheHelp` usa `dev.bitacora.help.R.string.help_title`. ¿Compilaría con `dev.bitacora.R.string.help_title`? ¿Y `dev.bitacora.R.string.help_body`? Pruébalo y explica la diferencia con lo que es una `R` no transitiva.
   - (c) Abre `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`. ¿Qué viene de la biblioteca y qué de la app? ¿Sigue `VIBRATE`?
   - (d) Si otra app usara `:feature:help`, ¿vería «Ayuda» o «Ayuda de la Bitácora»? ¿Y pediría vibrar?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Path is not a file: ...android-all-instrumented-17...` | La biblioteca no tiene `targetSdk`, y Robolectric usa el de `compileSdk` (37), que no está en la caché: `testOptions { targetSdk = 36 }` |
| `kotlin.NotImplementedError: An operation is not implemented: paso 2` | Falta `tips()` (paso 2) |
| `tipsInEnglish` da `1 consejo` | Sin el plural en `values-en`, Android usa el de `values` |
| `PackageManager$NameNotFoundException` | `HelpActivity` no está en el manifiesto de la biblioteca (paso 4) |
| `Unresolved reference 'R'` en `Help.kt` | `Help.kt` tiene que estar en el paquete del namespace (`dev.bitacora.help`), o importar `dev.bitacora.help.R` |
| `theAppDoesNotAskToVibrate` sigue en rojo con el permiso en el manifiesto de la app | Falta `tools:node="remove"`, o el `xmlns:tools` en `<manifest>` |
| `lintDebug` falla con `MissingTranslation` | Un texto o un plural de `values` no está en `values-en` |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas de los pasos 2 y 8.
