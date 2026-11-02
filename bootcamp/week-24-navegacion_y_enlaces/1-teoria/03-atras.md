# Atrás

> El botón o el gesto de atrás es de Android, y cada app tiene que responder igual: volver a donde estaba, y en la primera pantalla, salir. Con una actividad por pantalla eso lo hacía Android solo. Con destinos, lo decides tú en `onBack`. Esta teoría es qué hace `NavDisplay` con atrás, cómo devuelve un destino un resultado sin `setResult`, y un detalle de los tests.

## 🎯 Objetivos

- Escribir `onBack` y explicar qué pasa con una sola clave en el back stack
- Devolver un resultado desde un destino a través del `ViewModel`
- Probar atrás en Robolectric sin falsos verdes ni falsos rojos

## 1. onBack

`NavDisplay` recoge atrás (del botón, del gesto o de `onBackPressedDispatcher`) y llama a `onBack`. Casi siempre:

```kotlin
onBack = { backStack.removeLastOrNull() }
```

Con una sola clave no hay a dónde volver, y `NavDisplay` no recoge atrás: lo deja pasar a Android, que cierra la actividad.

> 🔬 Medido en el ejercicio 01: en la sesión, sola en el back stack, atrás deja la actividad con `isFinishing` a `true`. Con una observación abierta, atrás vuelve a la sesión y `isFinishing` sigue en `false`.

`onBack` se llama, pero lo que haga es cosa tuya. Si no quita nada, atrás no hace nada:

> 🔬 Medido en la sonda: con `onBack = {}` y una observación abierta, atrás no cambia de pantalla ni cierra la actividad. En el ejercicio 01 (paso 6d) eso hace fallar 5 de los 9 tests.

Un atrás que no responde es de lo peor que se puede hacer en Android. Si una pantalla tiene que preguntar algo antes de salir (un borrador sin guardar), pregunta, y después quita la clave.

## 2. Un resultado sin setResult

En la semana 18, `ObservationActivity` devolvía el nivel elegido con `setResult`, y `MainActivity` lo leía en el callback de `registerForActivityResult`. Entre destinos no hace falta: los dos están en la misma actividad, con el mismo `ViewModel`.

```kotlin
entry<ObservationKey> { key ->
    ObservationScreen(observation, onChoose = {
        notes.replace(it)            // el resultado, en el ViewModel
        backStack.removeLastOrNull() // y vuelve
    })
}
```

El resultado no viaja: se guarda donde está la verdad, y la sesión lo ve porque observa el mismo estado. Y atrás sin elegir no cambia nada, sin un `RESULT_CANCELED` que comprobar.

> 🔬 Medido en el proyecto: `choosingALevelSavesItAndGoesBack` y `goingBackChangesNothing`, los dos en verde con el `ViewModel` de la semana 18, sin tocarlo más que para buscar una observación por su código.

Navigation 3 trae además un `ResultEventBus` (en `runtime.result`) para pasar resultados de un destino a otro sin un `ViewModel` común. En la Bitácora no hace falta.

## 3. Atrás en un test

`NavDisplay` decide si recoge atrás al recomponer: con una clave, no; con dos, sí. En un test, tras un clic, la recomposición todavía no ha pasado.

> 🔬 Medido en el proyecto: abrir una observación con `performClick()` y llamar enseguida a `onBackPressedDispatcher.onBackPressed()` cierra la actividad (`isFinishing` a `true`) con la observación todavía en pantalla. Con `rule.waitForIdle()` antes, vuelve a la sesión.

Por eso los tests de la semana tienen un ayudante:

```kotlin
private fun ComponentActivity.back() {
    rule.waitForIdle()
    onBackPressedDispatcher.onBackPressed()
}
```

En un móvil no pasa: entre el toque y el gesto de atrás hay muchos fotogramas.

## ✅ Comprueba

- [ ] Con la sesión sola en el back stack, ¿quién responde a atrás? ¿Y con una observación encima?
- [ ] ¿Por qué un destino no necesita `setResult` para devolver el nivel elegido?
- [ ] Un test pulsa una fila y atrás, y la actividad se cierra. ¿Qué falta?
