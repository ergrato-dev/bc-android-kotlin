# Aparecer, irse y moverse

> Animar un color es cambiar un valor. Que algo aparezca o se vaya es otra cosa: mientras se va, todavía está, y lo de alrededor tiene que hacerle sitio o quitárselo poco a poco. Esta teoría es `AnimatedVisibility`, `AnimatedContent`, las filas que se mueven en una `LazyColumn` y las transiciones entre destinos de Navigation 3.

## 🎯 Objetivos

- Hacer que algo aparezca y se vaya con `AnimatedVisibility`
- Cambiar un contenido por otro con `AnimatedContent`
- Animar las filas de una lista y las transiciones entre destinos, y probarlas

## 1. AnimatedVisibility

```kotlin
AnimatedVisibility(expanded) { Text(observation.description) }
```

En vez de `if (expanded)`. Al pasar a `true`, el contenido entra (por defecto, `fadeIn() + expandIn()`); al pasar a `false`, sale (`fadeOut() + shrinkOut()`) y, al terminar, deja la composición. Las transiciones se eligen con `enter` y `exit`: `slideInVertically()`, `expandVertically()`, `fadeIn(tween(150))`, y se suman con `+`.

Mientras sale, el contenido sigue en el árbol:

> 🔬 Medido en el ejercicio 01: a los 50 ms de cerrar una tarjeta, la descripción sigue existiendo (`assertExists`); a los 1050 ms, ya no.

> 🔬 Medido en el ejercicio 01 (paso 5d): con `if (expanded)`, a los 50 ms de abrir A-1, A-2 ya está en su sitio final (`64.0 < 88.0 < 88.0`): el contenido salta en vez de crecer.

Un test que comprueba que algo se ha ido tiene que esperar a que termine de irse. Con el reloj automático, `assertDoesNotExist()` ya lo hace.

## 2. AnimatedContent

Para cambiar un contenido por otro, como el texto de la ubicación al pasar de "Ubicando…" a las coordenadas:

```kotlin
AnimatedContent(placeText(state), label = "place") { text -> Text(text) }
```

Cada vez que cambia el valor, el de antes sale y el nuevo entra a la vez (por defecto, con un fundido). Durante la transición, los dos están en el árbol.

## 3. Las filas de una lista

En una `LazyColumn` con claves, `Modifier.animateItem()` en cada fila anima tres cosas: la que aparece (fundido), la que se va (fundido) y las que cambian de sitio (con un `spring`).

```kotlin
items(observations, key = { it.code }) { observation ->
    ObservationRow(observation, ..., Modifier.animateItem())
}
```

Sin clave no puede saber qué fila es cuál, y no anima. Con la clave del código de la semana 22, sí.

> 🔬 Medido en la sonda: al borrar la fila del medio, la de debajo está en 228 px y sube a 225 a los 50 ms, 214 a los 100, 206 a los 200 y 204 a los 300, donde se queda.

> 🔬 Medido en el ejercicio 02 (paso 5e): sin `animateItem()`, a los 50 ms la fila de debajo ya está en su sitio final (`76.0 < 76.0 < 136.0`).

## 4. Entre destinos

`NavDisplay` anima el cambio de destino con un `AnimatedContent`, con una transición por defecto. Se cambia con `transitionSpec` (al abrir), `popTransitionSpec` (al volver) y `predictivePopTransitionSpec` (mientras se hace el gesto de atrás):

```kotlin
NavDisplay(
    ...,
    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
)
```

> 🔬 Medido en la sonda: con la transición por defecto, la pantalla de antes y la nueva están las dos en el árbol desde el primer fotograma hasta los 550 ms; a los 600 ya solo queda la nueva.

Por eso, con el reloj parado, `onNodeWithText` puede encontrar textos de las dos pantallas. Los tests de la Bitácora usan el reloj automático para navegar, y lo paran solo para mirar una animación concreta.

## ✅ Comprueba

- [ ] ¿Qué diferencia hay, en el árbol de semántica, entre `if (visible)` y `AnimatedVisibility(visible)` al ocultar?
- [ ] ¿Qué necesita una `LazyColumn` para que `animateItem()` sepa qué fila se ha movido?
- [ ] Con el reloj parado, tras abrir una observación, un test encuentra el título de la sesión. ¿Por qué?
