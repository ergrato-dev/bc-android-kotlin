# Recomponer menos

> Una recomposición de más no se ve: la pantalla queda igual. Se nota cuando son muchas (cada fila, con cada tecla, o con cada fotograma del scroll) y el móvil se queda sin tiempo para dibujar. Esta teoría es cómo contarlas, de dónde salen las que sobran en la Bitácora y en el ejercicio 02, y cómo se quitan. Y una regla: primero se mide.

## 🎯 Objetivos

- Contar recomposiciones en un test
- Evitar instancias nuevas de lo que no ha cambiado
- Leer en la composición solo lo que importa, con `derivedStateOf`

## 1. Contar

En el móvil, el Layout Inspector de Android Studio enseña cuántas veces se ha recompuesto y saltado cada composable. En un test, un contador:

```kotlin
object Recompositions {
    val counts = mutableMapOf<String, Int>()

    @Composable
    @NonSkippableComposable
    fun count(name: String) {
        SideEffect { counts[name] = (counts[name] ?: 0) + 1 }
    }
}
```

`SideEffect` se ejecuta después de cada composición que termina. Se llama al principio de la función que se quiere contar: `Recompositions.count(row.code)`. Es para aprender; en la app no va.

> 🔬 Medido en el ejercicio 02: sin `@NonSkippableComposable`, el contador de la pantalla se quedaba en 0 aunque la pantalla se recompusiera. `count("screen")` es una función `skippable` con un parámetro que no cambia nunca: Compose se la saltaba.

## 2. Instancias nuevas

Con *strong skipping*, un parámetro inestable se compara con `===`: una instancia nueva, aunque sea igual, recompone. Y uno estable con `equals`: una nueva igual, no.

> 🔬 Medido en la Bitácora de la semana 25, con un contador en `ObservationRow`: con las observaciones de ejemplo, 5 teclas en el borrador recomponen 0 filas. Tras borrar una, 5 teclas recomponen 10 (las 2 filas que se ven, 5 veces cada una).

La diferencia es de dónde salen las observaciones. Antes de borrar, el handle no tiene `"observations"` y el estado usa `Samples.observations`, siempre las mismas instancias. Después, cada vez que cambia el borrador, `combine` vuelve a ejecutar su lambda, y `decode` saca del JSON observaciones nuevas, iguales a las de antes:

```kotlin
combine(handle.getStateFlow("draft", ""), ..., handle.getStateFlow<String?>("observations", null)) { draft, ..., observations ->
    BitacoraUiState(draft, ..., observations?.let(::decode) ?: Samples.observations)   // con cada tecla
}
```

Dos arreglos, y la Bitácora lleva los dos:

- **No crear instancias**: `decode` en un `map` del flujo de `"observations"`, antes de `combine`. Solo se ejecuta cuando cambia el JSON; con cada tecla, `combine` recibe la misma lista.
- **Comparar con `equals`**: el archivo de estabilidad de la teoría 04. Las observaciones nuevas e iguales dejan de recomponer.

> 🔬 Medido en la Bitácora: con el `map`, las 5 teclas tras borrar recomponen 0 filas; con solo el archivo de estabilidad, también 0.

El `map` quita el trabajo de raíz (tampoco se vuelve a leer el JSON con cada tecla); el archivo hace que el informe diga la verdad sobre `core` y protege de la próxima instancia nueva que nadie vea venir.

> 🔬 Medido en la Bitácora, con los dos arreglos: al cambiar el nivel de A-1 (`replace`) o borrar A-3, que no se ve, se recomponen una vez A-1 y A-2, las dos filas visibles, con el archivo de estabilidad y sin él, y también sin `SwipeToDelete` ni `animateItem`. El informe dice `stable observation`, y aun así la fila de A-2 no se salta cuando cambia la lista. Una vez por cambio de la lista no es un problema; una vez por tecla, sí. Por eso se cuenta antes de seguir arreglando.

## 3. Dónde se calcula

En el ejercicio 02, la pantalla saca un `Item` de cada observación para pintar la fila:

```kotlin
val items = observations.map { it.toItem() }     // con cada recomposición de la pantalla
LazyColumn { items(items, key = { it.code }) { ObservationRow(it, ...) } }
```

> 🔬 Medido en el ejercicio 02: con el `map` en la pantalla, 5 teclas recomponen 4 veces cada una de las 4 filas que se ven. Con el `map` dentro de la lambda de `LazyColumn`, 0: la lambda se recuerda según lo que captura (la lista, que no cambia con el borrador), y `LazyColumn` no la vuelve a ejecutar.

`remember(observations) { observations.map { it.toItem() } }` arregla las teclas, pero no un cambio de nivel: la lista es nueva y saca cuatro `Item` nuevos.

> 🔬 Medido en el ejercicio 02: con `remember(observations)` y sin `@Immutable`, cambiar el nivel de A-1 recompone las 4 filas (falla `aNewLevelRecomposesOnlyItsRow` con `[1, 1, 1, 1]`). Con `@Immutable`, `[1, 0, 0, 0]`.

## 4. Leer solo lo que importa

Un estado que cambia mucho, leído en la composición, recompone mucho. El botón "Arriba" del ejercicio 02 depende de si la lista ha bajado:

```kotlin
val scrolled = list.firstVisibleItemIndex > 0                                  // con cada fila que sale
val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } } // solo al cambiar el Boolean
```

`firstVisibleItemIndex` cambia con cada fila que sale por arriba, y la pantalla entera se recompone. `derivedStateOf` lee el índice por dentro y solo avisa cuando cambia su resultado.

> 🔬 Medido en el ejercicio 02: con un `swipeUp()`, la pantalla se recompone 8 veces leyendo el índice, y 1 con `derivedStateOf`.

`derivedStateOf` es para cuando el resultado cambia mucho menos que lo que lee. Si cambian igual (`derivedStateOf { a + b }`), solo añade trabajo. Y lo que cambia en cada fotograma y solo se dibuja (un color, un desplazamiento) se lee al dibujar, como en la semana 25.

## 5. Primero, medir

Ninguna de estas recomposiciones rompía un test de la semana 25: la pantalla era la misma. Optimizar sin medir llena el código de `remember` y `@Stable` que no hacen nada, o que esconden un fallo si la promesa es falsa. El orden es: contar (o mirar el Layout Inspector), leer el informe para saber por qué, arreglar la causa y volver a contar.

## ✅ Comprueba

- [ ] ¿Por qué el contador necesita `@NonSkippableComposable`?
- [ ] ¿Por qué en la Bitácora las filas no se recomponían al teclear con las de ejemplo, y sí después de borrar?
- [ ] `remember(observations)` o `@Immutable`: ¿qué arregla cada uno?
- [ ] ¿Cuándo sirve `derivedStateOf`, y cuándo solo añade trabajo?
