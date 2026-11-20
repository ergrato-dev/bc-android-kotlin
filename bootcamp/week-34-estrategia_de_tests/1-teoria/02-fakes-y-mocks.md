# Dobles de prueba: fakes frente a mocks

> `ViewModelTest` no usa Room: le pasa a `BitacoraViewModel` un `FakeRepository` en memoria. Esa pieza que sustituye a la de verdad es un **doble de prueba**, y hay varias formas de hacerlo. Esta teoría las compara, explica por qué la Bitácora usa fakes y no mocks, y presenta el contrato: los mismos tests para el fake y para Room, para que el fake no mienta.

## 🎯 Objetivos

- Distinguir dummy, stub, fake, spy y mock
- Escribir un test con MockK y saber qué cuesta
- Elegir entre un fake y un mock
- Mantener honesto un fake con un test de contrato

## 1. Los dobles

Gerard Meszaros les puso nombre en *xUnit Test Patterns*:

| Doble | Qué hace | En la Bitácora |
| --- | --- | --- |
| **Dummy** | Rellena un parámetro que no se usa | `offline`, el `ObservationApi` sin red de `RoomTest` |
| **Stub** | Contesta lo que el test le dice | `answer` de `FakeRepository`: `refresh()` devuelve lo que el test ponga |
| **Fake** | Funciona de verdad, pero con un atajo (en memoria, sin red) | `FakeRepository`, `FakeSettings`, `MockEngine` |
| **Spy** | El de verdad (o un fake), que además apunta lo que le piden | `saves` y `refreshes` de `FakeRepository` |
| **Mock** | Lleva escritas las llamadas que espera y falla si no llegan | Ninguno |

`FakeRepository` combina tres papeles: es un fake (guarda en un `MutableStateFlow`), un stub (`answer`) y un spy (`saves`). Es lo normal en un doble escrito a mano.

## 2. Un mock con MockK

[MockK](https://mockk.io) es la biblioteca de mocks de Kotlin. La Bitácora no la usa. Se presenta aquí para que la reconozcas, porque sale en muchos proyectos.

> 🔬 Medido con MockK 1.14.12 en un proyecto aparte (no entra en la Bitácora).

```kotlin
val store = mockk<NoteStore>()
coEvery { store.add(any()) } returns Unit      // co-: para funciones suspend
store.add("x")
coVerify(exactly = 1) { store.add("x") }       // falla si no se llamó así, exactamente una vez
```

Si un mock recibe una llamada que no tiene configurada, falla:

```
MockKException: no answer found for NoteStore(#4).getNotes() [getNotes(): Flow] among the configured answers: ()
```

Con `mockk<NoteStore>(relaxed = true)` devuelve valores vacíos en lugar de fallar. Para `notes`, un `Flow` de mentira que nadie ha configurado.

## 3. Por qué la Bitácora usa fakes

Un test con mocks comprueba **qué llamadas** se hacen. Un test con fakes comprueba **qué queda** después. Compara los dos para `onlyChangesToTheListAreSaved`:

```kotlin
// Con el fake: el estado final
notes.onIntent(BitacoraIntent.Add(Level.HIGH))
assertEquals(1, repository.saves)

// Con un mock: la llamada
coVerify(exactly = 1) { repository.save(any()) }
```

Se parecen. La diferencia aparece cuando el código cambia:

- **Un refactor rompe los mocks.** Imagina que el ViewModel pasa a guardar con un `saveAll` nuevo. El comportamiento es el mismo, pero `coVerify { save(...) }` falla. Con el fake no pasa nada, porque solo mira lo que hay guardado.
- **Los mocks se repiten.** Cada test vuelve a escribir qué contesta el repositorio. El fake se escribe una vez y vale para todos los tests. Desde esta semana, también para los de otros módulos (teoría 03).
- **Un mock contesta lo que le digas, aunque sea imposible.** Puede devolver una lista desordenada que Room nunca devolvería. Un fake también puede mentir, pero se puede comprobar (apartado 4).

La [guía de Android](https://developer.android.com/training/testing/fundamentals/test-doubles) recomienda lo mismo: fakes para las dependencias propias, y mocks solo cuando no hay otra forma (una clase final de otro que no se puede imitar, o comprobar que se llamó a algo que no deja estado).

## 4. El contrato: que el fake no mienta

Un fake es código, y puede estar mal. Si `FakeRepository` no ordena como Room, los tests de la app que lo usan prueban una app que no existe. El remedio es escribir **una vez** lo que cualquier `ObservationRepository` tiene que cumplir, y ejecutarlo contra los dos:

```kotlin
abstract class ObservationRepositoryContract {
    protected abstract fun repository(initial: List<Observation> = emptyList()): ObservationRepository

    @Test
    fun theOrderIsByNumber() = runTest {
        val repository = repository()
        repository.save(listOf(c, b, a))
        assertEquals(listOf("A-1", "A-2", "A-10"), repository.observations.first().map { it.code })
    }
    // ...
}

class FakeRepositoryTest : ObservationRepositoryContract() {
    override fun repository(initial: List<Observation>): ObservationRepository = FakeRepository(initial)
}

class RoomRepositoryTest : ObservationRepositoryContract() {
    @TempDir lateinit var dir: File
    override fun repository(initial: List<Observation>): ObservationRepository = /* Room en dir */
}
```

JUnit 5 ejecuta los `@Test` de la clase abstracta en cada subclase: cinco tests escritos, diez ejecutados.

> 🔬 Medido en la Bitácora: con el `FakeRepository` de la semana 33, el contrato falla en uno de los diez.
>
> ```
> FakeRepositoryTest > theOrderIsByNumber() FAILED
>     expected: <[A-1, A-2, A-10]> but was: <[A-10, A-2, A-1]>
> ```
>
> Room ordena por número (`ORDER BY CAST(substr(code, ...) AS INTEGER)`), y el fake devolvía la lista en el orden en que se guardó. Ningún test de la app lo había notado.

Lo que va en el contrato es lo que la app da por hecho (el orden, lo que sale de la lista, que el `Flow` emite cada cambio). Lo que solo tiene Room (sobrevivir a cerrar la base de datos, importar el JSON de la semana 29) se queda en `RoomTest`.

## 5. Cuándo sí un mock

- **No se puede escribir un fake razonable.** Por ejemplo, una clase de un SDK que no tiene interfaz.
- **Lo que interesa es la llamada, porque no deja estado.** Por ejemplo, «se registra una métrica» o «se cancela el trabajo de WorkManager». Aun así, la Bitácora prueba WorkManager con `work-testing`, que es un fake de Google.
- **El código es de otro y no lo vas a refactorizar.**

## Para recordar

- Dummy, stub, fake, spy y mock. `FakeRepository` es a la vez fake, stub y spy.
- Los mocks comprueban llamadas y se rompen al refactorizar; los fakes comprueban el estado final.
- Un fake puede mentir: un contrato con los mismos tests para el fake y para el de verdad lo comprueba. En la Bitácora encontró que el fake no ordenaba.
- MockK, para lo que no tiene fake: `coEvery`, `coVerify` y `relaxed`.
