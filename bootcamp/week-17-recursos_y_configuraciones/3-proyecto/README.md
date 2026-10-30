# Proyecto — La Bitácora habla tu idioma

⏱️ 4 h 30 min · Capa 16 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La pantalla de la semana 16 sin un solo texto en Kotlin. El título, la pista del campo, los botones de nivel y el resumen salen de los recursos: en el idioma de tu dominio por defecto, y traducidos a un segundo idioma. El resumen dice cuántas observaciones hay con el plural correcto (`1 observación · Baja: 0 · Media: 0 · Alta: 1`). La app tiene su icono adaptativo con versión monocroma, declara sus idiomas para que el usuario pueda elegir el de la app en los ajustes de Android 13 o superior, y el APK solo lleva esos idiomas.

## Dónde trabajas

En **tu repositorio**, rama `week-17`, a partir de lo que entregaste en la semana 16. El `starter/` es la Bitácora de la semana 16 resuelta, con sus tests y con comentarios `Paso N` donde va cada cambio. Trae ya el dibujo del icono (`app/src/main/res/drawable/ic_launcher_foreground.xml`); puedes cambiarlo por uno de tu dominio.

En `src/test/kotlin/BuildTest.kt` hay un test de ejemplo que empieza en rojo: comprueba qué idiomas hay en el APK. Los tests de Robolectric de la semana 16 empiezan en verde y se pondrán en rojo en el paso 3, cuando la pantalla cambie de idioma. Copia todo eso a tu repositorio.

## Pasos

1. **Los textos** (`app/src/main/res/values/strings.xml`), en el idioma de tu dominio, que es el idioma por defecto:
   - `title` con formato: `Bitácora %1$s v%2$s` (el dominio y la versión).
   - `description_hint`.
   - Un texto por nivel: `level_low`, `level_medium`, `level_high`.
   - El plural `observations` con todas las formas que use tu idioma. El español necesita `one`, `many` y `other` (teoría 03).
2. **La traducción** (`app/src/main/res/values-en/strings.xml`, o el idioma que elijas): los mismos nombres, con sus plurales. Si tu dominio necesita una región (`b+es+419`), añádela con solo lo que cambia.
3. **La pantalla** (`MainActivity`): `getString` para el título, la pista y los botones, y `getQuantityString` para el total del resumen. Para el texto de cada nivel, una propiedad privada en `app` que devuelve el recurso de cada `Level`: `core` sigue sin saber nada de Android.
4. **El icono**:
   - `app/src/main/res/values/ic_launcher.xml` con el color `ic_launcher_background`.
   - `app/src/main/res/mipmap-anydpi/ic_launcher.xml` con `<background>`, `<foreground>` y `<monochrome>`.
   - `android:icon="@mipmap/ic_launcher"` en el manifiesto.
5. **El idioma por app**, en `bitacora.android-application`: `androidResources { generateLocaleConfig = true }`. En la app, `app/src/main/res/resources.properties` dice de qué idioma es `values/` (`unqualifiedResLocale=es`).
6. **Solo tus idiomas** (`app/build.gradle.kts`): `androidResources { localeFilters += listOf(...) }` con los de tu app, regiones incluidas.
7. **Los tests de la semana 16**: `@Config(qualifiers = "es")` (o tu idioma) en la clase `MainActivityTest`, y los textos esperados en tu idioma. El móvil de Robolectric está en inglés de EE. UU.: sin esa línea, tus tests ven la traducción (teoría 02).
8. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:testDebugUnitTest :app:lintDebug` y `git diff --stat gradle/verification-metadata.xml`. Explica en el README lo que cambia, si cambia algo (teoría 05).
9. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**. Los de Robolectric van en `app/src/test/kotlin/dev/bitacora/`; los del build, en `BuildTest.kt`:
   - los botones y la pista en tu idioma,
   - el plural del resumen con 2 observaciones,
   - toda la pantalla en el segundo idioma (`@Config(qualifiers = "en")` en ese test), con el resumen,
   - un idioma que no tienes (`fr`) cae en el tuyo,
   - el manifiesto declara `android:localeConfig`, y el XML generado tiene tu idioma como `defaultLocale` y el segundo idioma,
   - el icono es adaptativo y tiene `<monochrome>` (`res/mipmap-anydpi-v21/ic_launcher.xml` en el APK),
   - `:app:lintDebug` pasa, y un texto nuevo sin traducir lo para con `[MissingTranslation]`.
10. **Explica en tu README** qué idiomas lleva el APK y de dónde sale cada uno, por qué tu idioma por defecto está en `values/` y no en `values-es`, y qué vería un usuario con el móvil en un tercer idioma.
11. **Abre el pull request** `week-17` → `main` con la salida de `./gradlew test` y lo que ha cambiado en la verificación.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, ningún texto de la interfaz en Kotlin, plurales completos, `lintDebug` en verde, `generateLocaleConfig` en el convention plugin y `localeFilters` en la app, `core` y `data` sin Android, y tests con valores calculados a mano.

## Pistas

- Si un texto que sabes que existe da `Unresolved reference`, comprueba que está en `values/` y no solo en la traducción. Un texto sin valor por defecto no llega a la `R` con la que se compila (teoría 01).
- Para leer los botones en un test, recorre los hijos del `LinearLayout` de la pantalla y quédate con los `Button`.
- `resources.configs` en el APK incluye los idiomas de AndroidX que deja pasar tu filtro. Con el español por defecto y `en` en el filtro, verás `default`, `en` y también `es`: son textos de `androidx.core`, no tuyos.
- Si un test de lint falla, ejecuta `./gradlew :app:lintDebug` en tu proyecto y abre `app/build/reports/lint-results-debug.html`. La copia de TestKit se borra al terminar.
- `./gradlew :app:testDebugUnitTest` es mucho más rápido que `./gradlew test` mientras trabajas en la pantalla. Los tests de `BuildTest.kt` construyen APK en una copia del proyecto y tardan minutos.
