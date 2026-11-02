# El estado de la lista

> Una lista también tiene estado: hasta dónde ha bajado el usuario. Si gira el móvil y vuelve arriba del todo, ha perdido su sitio. `LazyListState` guarda esa posición, sobrevive al giro y a la muerte del proceso, y deja leerla y cambiarla desde el código: para enseñar un botón "Arriba", o para llevar al usuario a una fila. Leerla mal hace que la pantalla se recomponga en cada píxel de desplazamiento. Esta teoría es ese estado, cómo se lee sin pagar de más y cómo se prueba.

## 🎯 Objetivos

- Guardar la posición de una lista con `rememberLazyListState` y saber qué sobrevive
- Leer la posición con `derivedStateOf`, y entender el aviso de lint si no
- Mover la lista desde el código con una corrutina de la composición

## 1. rememberLazyListState

```kotlin
val state = rememberLazyListState()
LazyColumn(state = state) { ... }
```

`rememberLazyListState` es un `rememberSaveable` con su `Saver`: guarda el índice del primer elemento visible y cuánto se ha desplazado dentro de él. Es estado de la pantalla, no de la Bitácora: el `ViewModel` no necesita saber hasta dónde ha bajado el usuario, y por eso vive en el composable, aunque la pantalla no tenga estado de la app ([semana 21, teoría 05](../../week-21-compose_desde_cero_y_estado/1-teoria/05-viewmodel-y-stateflow.md)).

> 🔬 Medido en el ejercicio 02: tras `performScrollToKey("A-300")` y la muerte del proceso, la actividad nueva enseña la observación 300 arriba. Y una fila abierta con `rememberSaveable` dentro de un `items` sigue abierta; con `remember`, se cierra (paso 6b).

En un test hay una trampa: el desplazamiento se aplica en la siguiente pasada de medida.

> 🔬 Medido en el ejercicio 02: con `performScrollToKey("A-300")` y justo después `saveInstanceState`, la actividad nueva vuelve arriba del todo (enseña la 3, la 6...). Con `rule.waitForIdle()` entre los dos, vuelve a la 300. El test guardaba la posición de antes.

## 2. Leer la posición sin recomponer de más

`state.firstVisibleItemIndex` es un estado de Compose: leerlo dentro de un composable lo apunta como dependencia ([semana 21, teoría 02](../../week-21-compose_desde_cero_y_estado/1-teoria/02-estado-y-recomposicion.md)). Cambia con cada fila que sale por arriba. Para un botón que solo tiene dos estados, aparecer o no, eso son muchas recomposiciones para nada:

```kotlin
if (state.firstVisibleItemIndex > 0) UpButton()   // recompone con cada fila
```

> 🔬 Medido en la sonda: `./gradlew :app:lintDebug` da `Warning: Reading a value annotated with @FrequentlyChangingValue inside composition [FrequentlyChangingValue from androidx.compose.runtime]`.

`derivedStateOf` calcula un estado a partir de otros, y solo avisa cuando cambia **el resultado**:

```kotlin
val scrolled by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
if (scrolled) UpButton()
```

> 🔬 Medido en la sonda, contando las pasadas con un `SideEffect` mientras la lista baja de la fila 1 a la 20: sin `derivedStateOf`, 20 recomposiciones; con él, 2 (la primera y el paso de `false` a `true`).

`derivedStateOf` va dentro de `remember`: sin él, se crearía uno nuevo en cada recomposición. Úsalo cuando el estado de entrada cambia mucho más que el resultado. Si cambian igual (un contador que enseñas tal cual), no gana nada.

## 3. Mover la lista desde el código

`scrollToItem` y `animateScrollToItem` son funciones `suspend`: esperan a que la lista llegue. Un `onClick` no es una corrutina, así que hace falta un ámbito ligado a la composición:

```kotlin
val scope = rememberCoroutineScope()
Button(onClick = { scope.launch { state.animateScrollToItem(0) } }) { Text(stringResource(R.string.up)) }
```

`rememberCoroutineScope` da un `CoroutineScope` que se cancela cuando el composable sale de la composición, como `viewModelScope` con el `ViewModel` ([semana 16](../../week-16-actividades_y_ciclo_de_vida/README.md)). No uses `GlobalScope` ni lances la corrutina durante la composición: lanzarla en cada recomposición desplazaría la lista una y otra vez.

> 🔬 Medido en el ejercicio 02: tras `performScrollToIndex(10)`, "Arriba" existe; al pulsarlo, la observación 1 vuelve a la composición y el botón desaparece.

## 4. Dónde va cada estado

| Estado | Dónde vive | Por qué |
| --- | --- | --- |
| Las observaciones, el borrador | `SavedStateHandle` del `ViewModel` | Son de la Bitácora; la actividad los usa para compartir |
| La posición de la lista | `rememberLazyListState` en la pantalla | Solo le importa a la lista |
| Si una fila está abierta | `rememberSaveable` dentro del `items` | Solo le importa a la fila; va con su clave |

Las tres sobreviven al giro y a la muerte del proceso. Si mañana otra pantalla necesitara la posición (para volver a la misma fila desde el detalle, por ejemplo), se subiría: `BitacoraScreen(listState = ...)`, con `rememberLazyListState()` como valor por defecto.

## ✅ Comprueba

- [ ] ¿Por qué la posición de la lista no va en el `SavedStateHandle`?
- [ ] ¿Cuántas recomposiciones ahorra `derivedStateOf` si el usuario baja 100 filas de golpe? ¿Y si el estado derivado fuera el propio índice?
- [ ] ¿Por qué `animateScrollToItem` no se puede llamar directamente en el `onClick`?
