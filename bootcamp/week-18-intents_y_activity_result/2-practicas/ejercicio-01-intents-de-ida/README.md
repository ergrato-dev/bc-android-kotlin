# Ejercicio 01 — Intents de ida

⏱️ 2 h · Teoría previa: 01, 02, 03 y 05

## Objetivo

Que la app abra pantallas y otras apps: una pantalla suya con una nota entera en un extra, el selector para compartir y un mapa. Además, saber si hay una app para compartir antes de enseñar el botón, y que un enlace `bitacora://observation/A-7` abra la nota sin que la app se rompa si el enlace viene mal.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Robolectric configurado como en la semana 16. `MainActivity` tiene tres botones que llaman a funciones que tienes que escribir: `openObservation`, `share` y `openMap`, más `canShare`, que decide si el botón de compartir está activo. `ObservationActivity` todavía no está en el manifiesto, y `Note` es una `data class` normal.

El catálogo ya trae el plugin de Parcelize (`libs.plugins.kotlin.parcelize`), cargado con `apply false` en el raíz.

Los tests (`app/src/test/kotlin/dev/bitacora/IntentsTest.kt`, no los toques) leen con `shadowOf(activity).nextStartedActivity` lo que lanzó la actividad (teoría 05). Los 7 empiezan en rojo.

## Pasos

1. **La nota viaja entera**: aplica el plugin de Parcelize en `app/build.gradle.kts` y marca `Note` con `@Parcelize` y `: Parcelable`.
2. **Intent explícito**:
   - `openObservation(note)`: `startActivity` con un Intent a `ObservationActivity` y la nota en `EXTRA_NOTE`.
   - Registra `ObservationActivity` en el manifiesto.
   - En `ObservationActivity`, lee la nota con `IntentCompat.getParcelableExtra` y muestra `<código>: <texto>` en `shown`.
3. **Intents implícitos**:
   - `share(text)`: `ACTION_SEND`, tipo `text/plain`, el texto en `EXTRA_TEXT`, y todo dentro de `Intent.createChooser`.
   - `openMap(latitude, longitude)`: `ACTION_VIEW` con `"geo:$latitude,$longitude".toUri()`.
4. **¿Hay alguien?**: `canShare()` devuelve `true` si `resolveActivity` encuentra una app para `ACTION_SEND` de tipo `text/plain`. Añade al manifiesto el `<queries>` que lo hace funcionar en Android 11 o superior (teoría 02).
5. **El deep link**:
   - En el manifiesto, un `intent-filter` en `ObservationActivity` con `VIEW`, las categorías `DEFAULT` y `BROWSABLE`, y `<data android:scheme="bitacora" android:host="observation" />`. Ahora necesita `android:exported="true"`.
   - En `ObservationActivity`, si no hay nota, el código sale de `intent.data?.lastPathSegment` y se muestra tal cual. Si tampoco hay código, `finish()` y `return`.
6. **Ejecuta** `./gradlew test` (7 de 7).
7. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `android:exported` de `ObservationActivity`. ¿Qué dice el build y en qué tarea? ¿Pasaría lo mismo sin el `intent-filter`? Restáuralo.
   - (b) Cambia la lectura de la nota por `intent.getParcelableExtra(EXTRA_NOTE)`. ¿Qué dice el compilador? Ahora usa `intent.getParcelableExtra(EXTRA_NOTE, Note::class.java)` y ejecuta `./gradlew :app:lintDebug`. ¿Qué dice lint? ¿Por qué `IntentCompat` resuelve los dos problemas? Vuelve a `IntentCompat`.
   - (c) Quita la categoría `DEFAULT` del filtro. ¿Qué test falla y con qué mensaje? ¿Por qué el test busca con `MATCH_DEFAULT_ONLY` y no con `0`? Restáurala.
   - (d) Quita `<queries>`. ¿Siguen los tests en verde? ¿Qué devolvería `canShare()` en un Android 14 con Gmail instalado? Restáuralo.
   - (e) Añade `shadowOf(app).checkActivities(true)` al principio de `sharingGoesThroughTheChooser` (sin commitearlo). ¿Qué pasa? ¿Pasaría en un móvil sin ninguna app para compartir? ¿Por qué?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'Parcelize'` | Falta el plugin en `app/build.gradle.kts`, o el import `kotlinx.parcelize.Parcelize` |
| `None of the following candidates is applicable` en `putExtra(EXTRA_NOTE, note)` | `Note` aún no es `Parcelable`: ninguna versión de `putExtra` la acepta |
| `android:exported needs to be explicitly specified` | Una actividad con `intent-filter` declara `android:exported` siempre (teoría 03) |
| `'...getParcelableExtra(p0: String!): T?' is deprecated` | Usa `IntentCompat.getParcelableExtra(intent, EXTRA_NOTE, Note::class.java)` |
| `No se lanzó ningún Intent` | La función no llama a `startActivity`, o lo hace con otro contexto |
| `theDeepLinkOpensTheObservation` da `[]` | Revisa la acción, las dos categorías, el esquema y el host del filtro |
| `aLinkWithoutACodeClosesTheScreen` falla con un `NullPointerException` | Sin nota y sin código, `finish()` y `return` antes de usar el código |
| `[UseKtx]` en lint | Usa `toUri()` de `androidx.core.net` en lugar de `Uri.parse` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.
