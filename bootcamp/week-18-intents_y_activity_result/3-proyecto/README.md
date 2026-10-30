# Proyecto — La Bitácora abre y comparte

⏱️ 4 h 30 min · Capa 17 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora pasa a tener dos pantallas. Debajo del resumen, cada observación es un botón (`A-1 · Alta · riego del sector 3`). Al pulsarlo se abre la pantalla de la observación, donde se cambia su nivel, y el cambio vuelve a la principal con Activity Result: la lista y el resumen se actualizan. Si el usuario vuelve atrás, no cambia nada. Si Android mata la app mientras el usuario edita, el cambio llega igual. Y un botón comparte el resumen con cualquier app, a través del selector de Android.

## Dónde trabajas

En **tu repositorio**, rama `week-18`, a partir de lo que entregaste en la semana 17. El `starter/` es la Bitácora de la semana 17 resuelta, con sus tests y con comentarios `Paso N` donde va cada cambio.

En `src/test/kotlin/BuildTest.kt` hay un test de ejemplo que empieza en rojo: comprueba en el APK que la pantalla de la observación no se exporta. Los tests de Robolectric de la semana 17 empiezan en verde. Copia todo eso a tu repositorio.

## Pasos

1. **El texto de cada nivel**: lleva `Level.label` de `MainActivity` a su propio archivo (`Levels.kt`), como `internal`. Las dos pantallas lo usan.
2. **El contrato** (`EditObservation.kt`): `ActivityResultContract<Observation, Observation?>`.
   - `createIntent`: un Intent explícito a `ObservationActivity` con la observación en un extra.
   - `parseResult`: la observación editada si es `RESULT_OK`; si no, `null`.
   - La observación viaja como el JSON de `data` (`encode` y `decode`), como en el `SavedStateHandle`: `core` no puede ser `Parcelable` (teoría 01). Dos extensiones de `Intent` (`putObservation` y `observation()`) evitan repetirlo en cada pantalla.
3. **La pantalla de la observación** (`ObservationActivity`):
   - El título es el código; muestra la descripción y un botón por nivel, con los textos de los recursos.
   - Pulsar un nivel devuelve `RESULT_OK` con la observación con ese nivel, y cierra.
   - Sin observación en el Intent, se cierra (`finish()` y `return`).
   - En el manifiesto, con `android:exported="false"`: solo la abre la Bitácora.
4. **El resultado** (`MainActivity` y `ObservationDraftViewModel`):
   - `replace(observation)` en el `ViewModel`, que sustituye la observación con el mismo código en el `SavedStateHandle`.
   - Un lanzador de `EditObservation` como propiedad de `MainActivity`. Su callback llama a `replace` y repinta.
5. **La lista**: debajo del resumen, un `LinearLayout` con un botón por observación. El texto es un recurso con formato (`%1$s · %2$s · %3$s`: código, nivel y descripción), en los dos idiomas. Pulsarlo lanza `EditObservation`.
6. **Compartir**: un botón `Compartir` que lanza `ACTION_SEND` (`text/plain`) dentro de `Intent.createChooser`. El texto es el resumen y, debajo, una línea por observación, con el mismo formato que la lista. El título del selector sale de los recursos.
7. **Los tests de la semana 17**: el botón nuevo cambia la lista de botones de la pantalla. Actualiza los tests que la comprueban, con los valores escritos a mano.
8. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:testDebugUnitTest :app:lintDebug` y `git diff --stat gradle/verification-metadata.xml`. Explica en el README si cambia algo y por qué (pista: ¿has añadido alguna dependencia?).
9. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**:
   - cada observación es un botón con su texto, y pulsarlo lanza un Intent explícito a `ObservationActivity` con esa observación,
   - un resultado `RESULT_OK` sustituye la observación: la lista y el resumen cambian (`1 observación · Baja: 1 · Media: 0 · Alta: 0`),
   - un `RESULT_CANCELED` no cambia nada,
   - la pantalla de la observación devuelve el nivel elegido (`launchActivityForResult`), y volver atrás no devuelve nada,
   - sin observación, la pantalla se cierra,
   - compartir lanza el selector con `ACTION_SEND` y el texto completo,
   - el resultado llega a la actividad recreada tras la muerte del proceso (teoría 05).
10. **Explica en tu README** por qué el lanzador es una propiedad y no se crea al pulsar, qué viaja en el extra y por qué no es un `Parcelable`, y qué vería el usuario si la pantalla de la observación tuviera `exported="true"` y otra app la abriera sin extra.
11. **Abre el pull request** `week-18` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, lanzador registrado al crear la actividad, contrato propio, `ObservationActivity` sin exportar, `core` sin Android, ningún texto de la interfaz en Kotlin y tests con valores calculados a mano.

## Pistas

- `shadowOf(activity).nextStartedActivityForResult` saca el Intent que lanzó `edit.launch(...)`; `receiveResult` le devuelve la respuesta (teoría 05).
- Para pulsar el primer botón de la lista en un test: `activity.list.getChildAt(0).performClick()`, con `list` como propiedad pública de solo lectura (`lateinit var list: LinearLayout` y `private set`), como `summary`.
- El extra del selector se lee con `IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)`.
- El resumen de la pantalla ya se calcula en `showSummary()`. Si la lista se repinta en el mismo sitio, el callback del lanzador solo tiene que llamarla.
- `./gradlew :app:testDebugUnitTest` es mucho más rápido que `./gradlew test` mientras trabajas en las pantallas.
