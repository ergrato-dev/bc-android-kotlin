# Proyecto — Una actividad y sus destinos

⏱️ 4 h 30 min · Capa 23 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Desde la semana 18, la observación se abría en `ObservationActivity`, una actividad de Views, con la observación en los extras del `Intent` y el nivel elegido de vuelta con `setResult`. Esta semana la Bitácora es una sola actividad: la observación pasa a Compose como un destino de Navigation 3, con su foto y su nivel, y el resultado se guarda en el `ViewModel` en vez de viajar. El back stack sobrevive a la muerte del proceso, también con la foto recién hecha. Y un enlace `bitacora://observation/A-2` abre la Bitácora en esa observación, con la sesión debajo, si el código existe.

## Dónde trabajas

En **tu repositorio**, rama `week-24`, a partir de lo que entregaste en la semana 23. El `starter/` es la Bitácora de la semana 23 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya en el catálogo `navigation3` 1.2.0, con `navigation3-runtime` y `navigation3-ui`.

Y tests nuevos:

- `app/src/testField/kotlin/dev/bitacora/NavigationTest.kt`: la observación se abre en la misma actividad, elegir un nivel la guarda y vuelve, atrás no cambia nada, atrás en la sesión cierra, la observación abierta sobrevive a la muerte del proceso, y compartir sigue abriendo el selector;
- `app/src/testField/kotlin/dev/bitacora/PlaceAndPhotoTest.kt`, reescrito: la foto, desde el destino, con el `FileProvider`, guardada, cancelada, olvidada al volver atrás y viva tras la muerte del proceso;
- `app/src/testDemo/kotlin/dev/bitacora/DeepLinkTest.kt`: Android manda los enlaces a `MainActivity`, un enlace abre su observación, atrás desde él vuelve a la sesión, y los malos abren la sesión;
- en `src/test/kotlin/BuildTest.kt`, `oneActivityOpensTheLinks` sustituye a `theObservationScreenIsNotExported`: en el manifiesto, una sola actividad de la Bitácora, con el esquema `bitacora`.

`EditTest.kt` desaparece: probaba el contrato de `EditObservation`, que se va. Con la Bitácora de la semana 23, fallan 11 de 43 en `testFieldDebugUnitTest`, 3 de 28 en `testDemoDebugUnitTest` y `oneActivityOpensTheLinks`. Copia esos tests a tu repositorio, y borra `EditTest.kt`.

## Pasos

1. **El build** (`app/build.gradle.kts`): el plugin `kotlin.serialization` (ya está en el catálogo, lo usa `data`) y `navigation3-runtime` y `navigation3-ui`.
2. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest` y `git diff --stat gradle/verification-metadata.xml`. ¿Qué entra, además de Navigation 3? Pásalo por [OSV](https://osv.dev) antes de seguir (teoría 01).
3. **La observación en Compose**: `ObservationScreen(observation, onChoose: (Observation) -> Unit)` en `ObservationScreen.kt`, con un `Surface` en la raíz: el código (`titleLarge`), la descripción, la ubicación o "Sin ubicación", "Con foto" o "Sin foto", el botón "Foto" y un `Button` por nivel. La foto, con `rememberLauncherForActivityResult(TakePicture())` y el mismo `FileProvider` que la semana 19 (`filesDir/photos/CÓDIGO.jpg`), y guardada en un `rememberSaveable`: la cámara suele hacer que Android mate la app. Elegir un nivel llama a `onChoose` con la observación, ese nivel y la foto. Borra `ObservationActivity.kt` y `EditObservation.kt`, y la actividad del manifiesto.
4. **El `NavDisplay`** (`MainActivity`): `SessionKey` y `ObservationKey(code)` en `Navigation.kt` (teoría 01 y 02). En `setContent`, el back stack con `rememberNavBackStack`, `onBack` que quita la última, el decorador saveable, y una entrada por clave. Abrir una fila añade su clave; la entrada de la observación la busca por el código (y no pinta nada si todavía no está, teoría 02) y, al elegir, la guarda con `replace` y quita la clave (teoría 03).
5. **El `ViewModel`**: `fun observation(code: String): Observation?`, para comprobar un enlace.
6. **El enlace**: el `intent-filter` de `bitacora://observation` en `MainActivity`, y `startOf(intent, exists)` en `Navigation.kt`, con `UriDeepLinkMatcher` y `withBackStack`. En `onCreate`, `val start = startOf(intent) { notes.observation(it) != null }`, y el back stack empieza por ahí (teoría 04).
7. **Los tests**: `./gradlew :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` y `./gradlew :test` hasta el verde. Después, `./gradlew build`.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - en la sesión, el back stack es de una clave: atrás cierra la actividad (`isFinishing`);
   - abrir A-1, volver, abrir A-2: se ve la descripción de A-2, no la de A-1;
   - una foto hecha en A-1 y atrás: al volver a abrir A-1, "Sin foto" (el estado de un destino cerrado se olvida, teoría 05);
   - `startOf` con un `Intent` explícito a `MainActivity`, sin acción ni `Uri`: la sesión, sin excepción;
   - `startOf` con `bitacora://observation/A-1/fotos`: la sesión;
   - con una observación abierta por un enlace, elegir un nivel la guarda y deja la sesión con ella cambiada.
9. **Explica en tu README** qué hacía el usuario de tu dominio para abrir una observación antes y después; por qué la clave lleva el código y no la observación; y qué comprueba tu app de un enlace antes de abrirlo, y por qué.
10. **Abre el pull request** `week-24` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, una sola actividad, las claves serializables con un código, atrás que vuelve y en la sesión cierra, el resultado en el `ViewModel`, el enlace con la sesión debajo y comprobado, y tests con valores esperados escritos a mano.

## Pistas

- Tras un clic, `rule.waitForIdle()` antes de `onBackPressedDispatcher.onBackPressed()`: `NavDisplay` decide si recoge atrás al recomponer (teoría 03). Los tests del starter ya lo hacen.
- `DeepLinkRequest(intent)` necesita `import androidx.navigation3.runtime.deeplink.invoke`, y `withBackStack` su propio import.
- `Robolectric.buildActivity(MainActivity::class.java)` arranca con un `Intent` explícito, sin acción: sin la guarda de `intent.data`, fallan 25 de 43 en `field` y 7 de 28 en `demo`, con `DeepLinkRequest must have either a uri or extras.`
- La foto se guarda en el destino, no en el `ViewModel`, hasta que se elige un nivel: atrás sin elegir no guarda nada, tampoco la foto.
- `./gradlew :app:testFieldDebugUnitTest --tests '*NavigationTest'` para ir test a test.
