# Ejercicio 02 — Idiomas e icono en el APK

⏱️ 2 h · Teoría previa: 03, 04 y 05

## Objetivo

Decidir desde el build qué recursos van en el APK: solo los idiomas de tu app (y no las decenas que traen las bibliotecas), la lista de idiomas para que el usuario elija el de la app en los ajustes de Android, un icono adaptativo y un nombre distinto en la versión de debug. Al final, que lint no encuentre ninguna traducción pendiente.

## Cómo está montado

Como en la semana 15: `starter/sandbox/` es una app Android, y los tests de `starter/src/test/kotlin/BuildTest.kt` (no los toques) la copian a una carpeta temporal, la construyen con TestKit y abren el APK con `apkanalyzer`. Necesitas `ANDROID_HOME`. Los 7 tests empiezan en rojo.

La app ya tiene textos en inglés (`values`), español (`values-es`) y español de Latinoamérica (`values-b+es+419`). También tiene el dibujo del icono (`drawable/ic_launcher_foreground.xml`) y depende de `androidx.activity`.

## Pasos

Todo se hace dentro de `sandbox/`.

1. **Solo tus idiomas** (`app/build.gradle.kts`): un bloque `androidResources { }` dentro de `android { }` con `localeFilters += listOf("en", "es", "b+es+419")`.
2. **Idioma por app**: en el mismo bloque, `generateLocaleConfig = true`. Crea `app/src/main/res/resources.properties` con `unqualifiedResLocale=en-US`: así sabe AGP de qué idioma son los textos de `values/` (teoría 03).
3. **El icono adaptativo**:
   - En `app/src/main/res/values/ic_launcher.xml`, un color `ic_launcher_background` (`#2E7D32` o el de tu dominio).
   - En `app/src/main/res/mipmap-anydpi/ic_launcher.xml`, un `<adaptive-icon>` con `<background>` (el color), `<foreground>` y `<monochrome>` (los dos, el dibujo).
   - En el manifiesto, `android:icon="@mipmap/ic_launcher"`.
4. **La traducción que falta**: `./gradlew -p sandbox :app:lintDebug` y lee el error. Arréglalo en `values-es`.
5. **El nombre de debug** (`app/src/debug/res/values/strings.xml`): `app_name` = `Field log (debug)`. Así, en tu móvil, la versión de pruebas no se confunde con la de verdad.
6. **Ejecuta** `./gradlew test` (7 de 7).
7. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `localeFilters` y construye el debug. ¿Cuántas configuraciones de texto tiene el APK (`apkanalyzer resources configs --type string`)? ¿De dónde salen? ¿Qué idiomas lista el `localeConfig` generado?
   - (b) Deja el filtro en `listOf("en", "es")`. ¿Qué pierde el APK? ¿Qué texto verá un usuario de Argentina?
   - (c) ¿Qué nombre tiene la app de debug en un móvil en español? ¿Por qué no es `Field log (debug)`? ¿Qué harías para que lo fuera?
   - (d) Quita `<monochrome>` y pasa lint. ¿Qué dice? ¿Qué pierde el usuario de Android 13 o superior?
   - (e) Quita la traducción de `app_name` en `values-es` y ejecuta `assembleRelease`. ¿Falla? ¿Qué diferencia hay entre `lintDebug` y el `lintVitalRelease` que se ejecuta dentro de `assembleRelease`? Restáurala.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `No resources.properties file found` | Falta `app/src/main/res/resources.properties`, o no está en `res/` |
| `onlyTheAppLanguagesShip` da `[default, es]` | El filtro no incluye `b+es+419`: un filtro de idioma no arrastra sus regiones |
| `theIconIsAdaptive` no encuentra `res/mipmap-anydpi-v21/ic_launcher.xml` | El archivo no está en `mipmap-anydpi`, o falta `android:icon` en el manifiesto. aapt2 le añade `-v21` al empaquetarlo (teoría 04) |
| `error: resource color/ic_launcher_background (aka dev.bitacora.agro:color/ic_launcher_background) not found.` | El color no existe, o tiene otro nombre |
| `theDebugBuildHasItsOwnName` falla en debug | El archivo no está en `app/src/debug/res/values/`, o el nombre no es `app_name` |
| `lintFindsNothingToStopTheBuild` falla | Ejecuta lint como en el paso 4 y abre `sandbox/app/build/reports/lint-results-debug.html` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.
