# Probar un Flow con Turbine

> Probar un `Flow` es probar una secuencia de valores en el tiempo. En la semana 30, `theFlowEmitsEachChange` lo hacía a mano, con un `Channel` y un `collect` en `backgroundScope`. [Turbine](https://github.com/cashapp/turbine), de Cash App, deja escribir lo mismo como una lista de lo que se espera, y falla tanto si falta un valor como si sobra.

## 🎯 Objetivos

- Probar un `Flow` con `test { awaitItem() }`
- Saber cómo falla Turbine: falta un valor, sobra un valor o el `Flow` no acaba
- Probar un `StateFlow` sabiendo qué valores se ven
- Probar varios `Flow` a la vez

> 🔬 Medido con Turbine 1.2.1 y kotlinx-coroutines-test 1.11.0.

## 1. De `Channel` a Turbine

La semana 30:

```kotlin
val emitted = Channel<List<String>>(Channel.UNLIMITED)
backgroundScope.launch(Dispatchers.Default) {
    repository.observations.collect { list -> emitted.send(list.map { it.code }) }
}
assertEquals(emptyList(), emitted.receive())
repository.save(listOf(a))
assertEquals(listOf("A-1"), emitted.receive())
```

Con Turbine, en el contrato de esta semana:

```kotlin
repository.observations.test {
    assertEquals(emptyList(), awaitItem())
    repository.save(listOf(a))
    assertEquals(listOf(a), awaitItem())
    repository.save(listOf(a, b))
    assertEquals(listOf(a, b), awaitItem())
}
```

`test { }` recoge el `Flow` en una corrutina aparte y guarda cada evento en una cola. Dentro del bloque se van leyendo:

| Función | Espera |
| --- | --- |
| `awaitItem()` | El siguiente valor |
| `awaitComplete()` | Que el `Flow` termine |
| `awaitError()` | Que el `Flow` falle; devuelve la excepción |
| `expectNoEvents()` | Que no haya nada en la cola ahora mismo |
| `skipItems(n)` | Se salta `n` valores |
| `cancelAndIgnoreRemainingEvents()` | Cancela y no mira lo que quede |

El mismo contrato pasa contra `FakeRepository`, cuyo `MutableStateFlow` emite en el hilo del test, y contra Room, que emite desde `Dispatchers.IO`. Turbine espera igual en los dos casos.

## 2. Cómo falla

> 🔬 Medido, tres errores:
>
> - Sobra un valor: `flowOf(1, 2).test { awaitItem() }` falla al salir del bloque con
>   `TurbineAssertionError: Unconsumed events found: - Item(2) - Complete`.
> - Falta un valor: `MutableSharedFlow<Int>().test { awaitItem() }` falla con
>   `TurbineAssertionError: No value produced in 3s`, a los 3 s **reales**.
> - El `Flow` falla cuando se esperaba un valor: `awaitItem()` da
>   `TurbineAssertionError: Expected item but found Error(IllegalStateException)`, con la excepción del `Flow` como causa. Si el fallo es lo que se prueba, `awaitError()`.

El primero es el que más vale: un test con Turbine dice exactamente qué valores salen, no «al menos estos». Si el ViewModel emitiera un estado de más (un parpadeo de `refreshing`), el test lo vería. Para un `Flow` que no acaba (un `StateFlow`, el de Room), al final se dice qué se hace con el resto: `cancelAndIgnoreRemainingEvents()`, o nada si no queda ninguno. Al salir del bloque, Turbine cancela la recogida.

Los 3 s son de tiempo real, aunque el test corra en `runTest`, para que un `Flow` que emite desde `Dispatchers.IO` tenga tiempo de llegar. Se cambian con `test(timeout = 5.seconds) { }`.

## 3. `StateFlow`: qué valores se ven

Un `StateFlow` se queda solo con el último valor: quien llega tarde ve el actual, no la historia. Turbine recoge con un dispatcher que no pone nada en cola:

> 🔬 Medido:
>
> ```kotlin
> val state = MutableStateFlow(0)
> state.test {
>     state.value = 1
>     state.value = 2
>     state.value = 3
>     awaitItem()   // 0
>     awaitItem()   // 1
> }
> ```

En este caso se ven todos, porque cada asignación llega a la cola de Turbine antes de la siguiente. Si el que asigna corre en otro hilo, o en un `StandardTestDispatcher` sin dejar paso a Turbine, se pueden perder los intermedios. Un test que depende de ver un estado intermedio tiene que asegurarse de que se suspende entre medias. Si solo importa el final, se lee con `expectMostRecentItem()` o con `state.value`.

En la Bitácora, `refreshingGoesOnAndOff` mira solo el campo que interesa:

```kotlin
notes.state.map { it.refreshing }.test {
    assertFalse(awaitItem())
    notes.onIntent(BitacoraIntent.Refresh)
    assertTrue(awaitItem())
    repository.answer.value = RefreshResult.Done(2)
    assertFalse(awaitItem())
}
```

Con `map`, los cambios de otros campos (la lista, el borrador) saldrían como valores repetidos de `refreshing`. En este test no los hay, y si los hubiera, Turbine lo diría. Si un test los tolera, `distinctUntilChanged()`.

## 4. Efectos: una vez y nada más

```kotlin
notes.effect.test {
    notes.onIntent(BitacoraIntent.Delete("A-1"))
    assertEquals(BitacoraEffect.ShowUndo("A-1"), awaitItem())
    expectNoEvents()
}
```

`expectNoEvents()` comprueba lo que la semana 28 explicaba con palabras: el `Channel` entrega cada efecto una vez.

## 5. Tiempo virtual dentro de Turbine

Dentro de `runTest`, los `delay` del `Flow` son virtuales:

> 🔬 Medido: `flow { delay(5_000); emit("late") }.test { awaitItem() }` devuelve `late` con `currentTime == 5000`, en milisegundos reales.

Así prueba el ejercicio 01 un `debounce`: la búsqueda sale a los 700 ms virtuales.

## 6. Varios `Flow` a la vez

```kotlin
turbineScope {
    val a = repository.observations.testIn(backgroundScope)
    val b = settings.settings.testIn(backgroundScope)
    a.awaitItem()
    b.awaitItem()
    a.cancel()
    b.cancel()
}
```

`testIn` devuelve un `ReceiveTurbine`, que se lee igual que el bloque de `test`. `turbineScope` es el ámbito donde Turbine los crea. A diferencia de `test { }`, cada uno se cancela a mano (`cancel()` o `cancelAndIgnoreRemainingEvents()`).

> 🔬 Medido: dos `MutableStateFlow` con `testIn(backgroundScope)` dan cada uno su valor inicial con `awaitItem()`.

## Para recordar

- `test { awaitItem() }` recoge el `Flow` y lee sus eventos en orden. Si sobra uno, falla al salir.
- Esperar un valor tiene un límite de 3 s reales; los `delay` del `Flow`, dentro de `runTest`, son virtuales.
- Un `StateFlow` puede saltarse valores intermedios: no hay que depender de ellos sin forzar una suspensión.
- `expectNoEvents()` para «una vez y nada más»; `turbineScope` y `testIn` para varios `Flow`.
