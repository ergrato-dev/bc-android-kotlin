# Espresso y Espresso-Intents

> La Bitácora es de Compose, pero no todo lo que toca lo es. El mapa es un `MapView` dentro de un `AndroidView` (semana 27), las bibliotecas traen pantallas hechas con vistas, y muchas apps que mantendrás tienen años de XML. Para las vistas, la herramienta es **Espresso**. Y para lo que sale de la app (compartir, la cámara, el correo), tanto con vistas como con Compose, es **Espresso-Intents**. Esta teoría cubre las dos, en la JVM con Robolectric.

## 🎯 Objetivos

- Escribir un test de vistas con `onView`, `perform` y `check`
- Comprobar con `intended` lo que la app manda a otra
- Contestar con `intending` lo que la app espera de otra (una foto)
- Saber qué falla cuando la barra de acción tapa la pantalla

## 1. Espresso en tres palabras

```kotlin
onView(withId(R.id.note)).perform(typeText("riego"), closeSoftKeyboard())
onView(withId(R.id.send)).check(matches(isEnabled()))
```

| Parte | Qué es | Ejemplos |
| --- | --- | --- |
| `onView(matcher)` | Encuentra **una** vista con un matcher de Hamcrest | `withId`, `withText`, `withContentDescription`, `isDisplayed`, `allOf(...)` |
| `perform(acción...)` | Hace algo con ella | `click()`, `typeText`, `replaceText`, `scrollTo()`, `swipeLeft()`, `closeSoftKeyboard()` |
| `check(aserción)` | Comprueba cómo está | `matches(isEnabled())`, `matches(withText("Con foto"))`, `doesNotExist()` |

Es el mismo esquema que Compose (buscar, actuar, comprobar), con otra API. Como en Compose, Espresso espera solo antes de cada paso a que el hilo principal no tenga nada pendiente. Lo que va en otros hilos necesita un `IdlingResource` o una espera explícita.

La actividad la abre una regla de `androidx.test.ext.junit`:

```kotlin
@get:Rule
val activity = ActivityScenarioRule(ReportActivity::class.java)
```

Con Robolectric, Espresso funciona en la JVM: los tests del ejercicio 02 tardan lo mismo que los de Compose.

## 2. Vistas y Compose en la misma pantalla

| Lo que hay | Con qué se prueba |
| --- | --- |
| Una pantalla de vistas | Espresso: `onView` |
| Una pantalla de Compose | La regla de Compose: `onNode...` |
| Compose dentro de una vista (`ComposeView`) | La regla de Compose encuentra los nodos; Espresso, la vista que los contiene |
| Una vista dentro de Compose (`AndroidView`, el mapa) | Espresso para lo de dentro de la vista; la regla de Compose para el resto |

Los dos se pueden mezclar en el mismo test: cada uno espera a su parte.

## 3. Lo que sale de la app: `intended`

Compartir en la Bitácora abre el selector del sistema con el texto de la sesión (semana 18). Sin Intents, un test de Robolectric solo podía mirar `shadowOf(activity).nextStartedActivity`. Con Espresso-Intents:

```kotlin
@get:Rule(order = 0)
val intents = IntentsRule()                     // antes de abrir la actividad: graba todo lo que sale

@Test
fun shareSendsTheText() {
    rule.launch()
    rule.bitacora {
        add("riego", "Alta")
        share()
    }
    intended(
        allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(equalTo(Intent.EXTRA_INTENT), allOf(hasAction(Intent.ACTION_SEND), hasExtra(equalTo(Intent.EXTRA_TEXT), containsString("A-1 · Alta · riego")))),
        ),
    )
}
```

El selector envuelve el `Intent` de verdad en `EXTRA_INTENT`, y los matchers se anidan para mirar dentro. `IntentsRule` hace `Intents.init()` antes del test y `Intents.release()` después. Sin ella (🔬 medido): `NullPointerException: Intents not initialized. Did you forget to call init()?`.

> 🔬 Medido en el proyecto, con el texto equivocado:
>
> ```
> Wanted to match 1 intents. Actually matched 0 intents.
> IntentMatcher: (has action: is "android.intent.action.CHOOSER" and has extras: has bundle with: key:
>   "android.intent.extra.INTENT" value: (has action: is "android.intent.action.SEND" and ... "A-1 · Baja · riego"))
> Recorded intents:
> -Intent { act=android.intent.action.CHOOSER (has extras) } ...
> ```
>
> El mensaje enseña lo que se buscaba y lo que salió de verdad.

## 4. Lo que vuelve: `intending`

Pedir una foto con `registerForActivityResult(TakePicturePreview())` abre la cámara, que en Robolectric no existe. `intending` contesta en su lugar:

```kotlin
val photo = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    .respondWith(ActivityResult(Activity.RESULT_OK, Intent().putExtra("data", photo)))
onView(withId(R.id.photo)).perform(click())
onView(withId(R.id.status)).check(matches(withText("Con foto")))
```

> 🔬 Medido en el ejercicio 02:
>
> | `intending` | Lo que recibe el callback | La pantalla |
> | --- | --- | --- |
> | `RESULT_OK` con el `Bitmap` en `"data"` | El `Bitmap` | «Con foto» y la vista previa visible |
> | `RESULT_CANCELED` | `null` | «Sin foto» |
> | Sin `intending` | Nada: el resultado no llega | «Sin foto»; el test que espera la foto falla |

`intending` no comprueba nada: prepara la respuesta. Si quieres comprobar además que se pidió la foto, añade `intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))` después.

La Bitácora pide la foto con `TakePicture` y un `FileProvider` (semana 19). Sus tests ya contestan con `ShadowActivity` de Robolectric. `intending` hace lo mismo con una API que también funciona en un dispositivo.

## 5. La barra que tapa la pantalla

> 🔬 Medido en el ejercicio 02, con `@GraphicsMode(NATIVE)` y el tema `Theme.Material.Light` (con barra de acción):
>
> - la captura sale con la barra encima del contenido, y el campo de texto queda debajo, sin verse;
> - `typeText("riego roto")` no escribe nada y no da ningún error: el texto queda vacío.
>
> Con `Theme.Material.Light.NoActionBar`, la captura sale entera y `typeText` escribe. En el modo por defecto de Robolectric (`LEGACY`), `typeText` escribía también con la barra.

`typeText` pulsa la vista antes de escribir, para darle el foco. Si la pulsación cae en la barra, el campo nunca recibe el foco. Lo que hay que llevarse: un test de UI que «no hace nada» sin fallar suele estar tocando otra cosa. Mira la pantalla (una captura, teoría 06) antes de buscar el error en la app.

## ⚠️ Errores comunes

| Error | Qué pasa | Solución |
| --- | --- | --- |
| `intended` sin `IntentsRule` | `NullPointerException: Intents not initialized` | `IntentsRule` con `order = 0` |
| `IntentsRule` después de la regla de la actividad | Lo que sale en `onCreate` no se graba | `order` más bajo para `IntentsRule` |
| Buscar el `ACTION_SEND` de compartir directamente | No coincide: lo que sale es `ACTION_CHOOSER` | `hasExtra(EXTRA_INTENT, ...)` para mirar dentro |
| Sin `intending` para la cámara o un selector | El resultado no llega y la pantalla no cambia | `intending(...).respondWith(...)` antes de la acción |
| `typeText` en un campo tapado | No escribe y no falla | Tema sin barra en los tests, o `replaceText` |
| `onView` para un nodo de Compose | `NoMatchingViewException`: no es una vista | La regla de Compose |

## 📝 Resumen

- Espresso: `onView(matcher).perform(...).check(...)`. Funciona en Robolectric.
- Vistas con Espresso, Compose con su regla, y en la misma pantalla los dos.
- `IntentsRule` graba lo que sale de la app. `intended` lo comprueba, también lo que va dentro de un selector.
- `intending` contesta lo que la app espera de otra app: la foto, un contacto, un archivo.
- Un `typeText` que no escribe sin fallar suele estar pulsando otra cosa.

## 🔗 Recursos

- [Espresso](https://developer.android.com/training/testing/espresso)
- [Espresso-Intents](https://developer.android.com/training/testing/espresso/intents)
- [Chuleta de Espresso](https://developer.android.com/training/testing/espresso/cheat-sheet)
- [Robolectric y Espresso](https://robolectric.org/androidx_test/)
