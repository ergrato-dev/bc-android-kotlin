# Probar Intents en la JVM

> En un test de Robolectric no hay otras apps, ni lanzador, ni selector. Cuando tu actividad llama a `startActivity`, Robolectric no abre nada: apunta el Intent. El test lo lee, abre la otra pantalla con él si quiere, y le devuelve a la tuya el resultado que haría falta. Es la forma de probar las dos mitades de una conversación por separado.

## 🎯 Objetivos

- Leer el Intent que lanzó una actividad, con resultado y sin él
- Entregar un resultado y comprobar qué hace la actividad con él
- Probar la pantalla que devuelve el resultado
- Simular otras apps instaladas, el modo estricto y la muerte del proceso

## 1. Lo que salió

```kotlin
val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
activity.openObservation(note)
val intent = shadowOf(activity).nextStartedActivity
assertEquals(ComponentName(app, ObservationActivity::class.java), intent.component)
```

`shadowOf(activity)` es el `ShadowActivity`: la parte de Robolectric que observa lo que hace la actividad. `nextStartedActivity` saca de la cola el siguiente Intent lanzado con `startActivity`, o `null` si no hay más. Para los lanzados con un `ActivityResultLauncher`, `nextStartedActivityForResult` devuelve el Intent y su `requestCode`.

Para abrir la pantalla de destino con ese Intent:

```kotlin
val screen = Robolectric.buildActivity(ObservationActivity::class.java, intent).setup().get()
```

## 2. Lo que vuelve

```kotlin
activity.changeLevel()
val request = shadowOf(activity).nextStartedActivityForResult
shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, Intent().putExtra(EXTRA_LEVEL, "HIGH"))
assertEquals(Level.HIGH, activity.level)
```

`receiveResult` busca el `requestCode` de ese Intent y le entrega la respuesta al registro de la actividad, que llama al callback. Lo que pruebas es **tu callback**, no la otra app.

> 🔬 Medido en la sonda: funciona igual con un contrato propio que con `GetContent`.

## 3. La otra pantalla

`ActivityScenario.launchActivityForResult` abre la actividad que devuelve el resultado y guarda lo que pone con `setResult`:

```kotlin
ActivityScenario.launchActivityForResult<LevelPickerActivity>(intent).use {
    it.onActivity { picker -> picker.choose(Level.HIGH) }
    assertEquals(Activity.RESULT_OK, it.result.resultCode)
}
```

`it.result` espera a que la actividad termine. Para volver atrás, `picker.onBackPressedDispatcher.onBackPressed()`.

## 4. Otras apps

El móvil de Robolectric no tiene más apps que la tuya. `resolveActivity` devuelve `null` para cualquier Intent implícito que no acepte tu propio manifiesto:

> 🔬 Medido en la sonda: `resolveActivity(ACTION_SEND text/plain)` da `null`, y `queryIntentActivities`, 0.

Para instalar una app de mentira, se le dice al `ShadowPackageManager` que existe y qué acepta:

```kotlin
val mail = ComponentName("com.example.mail", "com.example.mail.ComposeActivity")
shadowOf(app.packageManager).addActivityIfNotPresent(mail)
shadowOf(app.packageManager).addIntentFilterForActivity(mail, IntentFilter(Intent.ACTION_SEND).apply {
    addCategory(Intent.CATEGORY_DEFAULT)
    addDataType("text/plain")
})
```

> 🔬 Medido en la sonda: después, `resolveActivity` devuelve `com.example.mail/...ComposeActivity` para `text/plain`, y sigue en `null` para `image/jpeg`.

En cambio, los filtros de **tu** manifiesto Robolectric sí los lee. Con eso pruebas tus deep links: `queryIntentActivities(link, MATCH_DEFAULT_ONLY)` encuentra tu actividad.

## 5. El modo estricto

Por defecto, `startActivity` con un Intent que nadie acepta no falla en Robolectric: lo apunta y sigue. En un móvil, se cerraría la app. Para que falle como en un móvil:

```kotlin
shadowOf(app).checkActivities(true)
```

> 🔬 Medido en la sonda: con `checkActivities(true)`, `ACTION_SEND` sin app lanza `ActivityNotFoundException`, y también el selector (`android.intent.action.CHOOSER`). En un móvil, el selector siempre existe. Por eso ese modo se activa solo en los tests que lo necesitan.

## 6. La muerte del proceso, con un resultado pendiente

Como en la semana 16, `saveInstanceState` y `setup(saved)` simulan la muerte del proceso. Lo nuevo es entregar el resultado a la actividad recreada, con el `requestCode` del lanzamiento original:

```kotlin
val first = Robolectric.buildActivity(MainActivity::class.java).setup()
first.get().changeLevel()
val request = shadowOf(first.get()).nextStartedActivityForResult
val saved = Bundle()
first.pause().stop().saveInstanceState(saved).destroy()

val second = Robolectric.buildActivity(MainActivity::class.java).setup(saved).get()
assertTrue(second.activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, data))
```

> 🔬 Medido en la sonda: `dispatchResult` devuelve `true` y llama al callback de la actividad recreada. En una actividad nueva sin `saved`, devuelve `false`.

## ✅ Comprueba

- [ ] ¿Qué diferencia hay entre `nextStartedActivity` y `nextStartedActivityForResult`?
- [ ] ¿Por qué no se activa `checkActivities(true)` en todos los tests?
- [ ] Para probar la muerte del proceso con un resultado pendiente, ¿de dónde sale el `requestCode`?
