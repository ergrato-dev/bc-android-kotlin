# Probar la arquitectura

> Con el reducer puro, el ViewModel delgado y el repositorio detrás de una interfaz, cada pieza se prueba donde es más barato: el reducer con JUnit, el ViewModel en la JVM con corrutinas de prueba, el repositorio con una carpeta temporal, y solo lo que une todo con Robolectric.

## 🎯 Objetivos

- Elegir dónde va cada test
- Probar un ViewModel en la JVM con `Dispatchers.setMain`
- Probar código con corrutinas con `runTest` y los *dispatchers* de prueba
- Escribir un repositorio de mentira útil

## 1. Cada cosa en su sitio

| Qué | Dónde | Con qué | Tarda |
| --- | --- | --- | --- |
| Las reglas | `ReducerTest` | JUnit | ms |
| El ViewModel: qué guarda, qué efectos manda, el handle | `ViewModelTest` | JUnit + `kotlinx-coroutines-test` + `FakeRepository` | ms |
| El archivo | `RepositoryTest` (en `data`) | JUnit + una carpeta temporal | ms |
| Que la actividad lo une todo | `ArchitectureTest` | Robolectric + Compose | s |

Es la pirámide de la semana 16 aplicada: muchas en la base, pocas arriba. Los tests de Robolectric de semanas anteriores (atrás, enlaces, el mapa) siguen siendo la red de seguridad: si la refactorización rompe algo que la persona ve, fallan ellos.

## 2. El ViewModel en la JVM

`viewModelScope` usa `Dispatchers.Main`, que en la JVM no existe. `kotlinx-coroutines-test` deja poner otro:

```kotlin
@Before
fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

@After
fun reset() = Dispatchers.resetMain()
```

> 🔬 Medido en el ejercicio 02, sin `setMain`: no hay excepción. `theListComesFromTheRepository` falla con la lista vacía: lo lanzado en `viewModelScope` no corre en el momento, y el `assertEquals` llega antes. Un test así puede pasar o fallar según la máquina.

Con `UnconfinedTestDispatcher`, lo que se lanza en `viewModelScope` empieza en el momento, como con `Main.immediate` en Android. El `collect` del `init` recibe la lista del repositorio antes de que el constructor termine:

```kotlin
@Test
fun theListComesFromTheRepository() {
    val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(listOf(riego)))
    assertEquals(listOf(riego), notes.state.value.observations)
}
```

`SavedStateHandle()` se crea vacío, o con valores: `SavedStateHandle(mapOf("draft" to "riego"))`. Para probar la muerte del proceso, dos ViewModels con el mismo handle (`theDraftSurvivesInTheHandle`).

> 🔬 En el ejercicio 02 no hay Android: `lifecycle-viewmodel` 2.11.0 es multiplataforma, y en un proyecto de Kotlin para la JVM Gradle resuelve `lifecycle-viewmodel-desktop`. El mismo `ViewModel`, `viewModelScope` incluido.

## 3. runTest y los efectos

`runTest` ejecuta un test con corrutinas con un reloj virtual: los `delay` no esperan de verdad. Dentro, `first()` suspende hasta el primer valor:

```kotlin
@Test
fun anEffectWaitsForItsCollector() = runTest {
    val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(listOf(riego)))
    notes.onIntent(BitacoraIntent.Delete("A-1"))
    assertEquals(BitacoraEffect.ShowUndo("A-1"), notes.effect.first())
}
```

Para comprobar que algo **no** llega, `withTimeoutOrNull(100) { flow.first() }` da `null`; con el reloj virtual, esos 100 ms no se esperan. Para recoger en paralelo mientras el test sigue, `backgroundScope.launch { flow.toList(list) }`: `runTest` lo cancela al terminar.

`StandardTestDispatcher(testScheduler)` encola en vez de ejecutar en el momento; `runTest` lo vacía al final. Es lo que se pasa como `io` al repositorio del archivo en `RepositoryTest`: la escritura ocurre, pero dentro del reloj del test.

## 4. Un repositorio de mentira

```kotlin
class FakeRepository(initial: List<Observation> = emptyList()) : ObservationRepository {
    override val observations = MutableStateFlow(initial)
    var saves = 0
        private set

    override suspend fun save(observations: List<Observation>) {
        saves++
        this.observations.value = observations
    }
}
```

Un *fake* es una implementación de verdad, solo que simple: guarda en memoria, y cuenta. No es un *mock* (que comprueba qué llamadas se le hacen): aquí se mira el resultado, `saves` y la lista. Con `saves` se prueba lo que no se ve en la pantalla: que escribir no guarda y que añadir guarda una vez (`onlyChangesToTheListAreSaved`). En el ejercicio 02, `failing = true` imita un disco lleno.

No hace falta una biblioteca de *mocks* para nada de esto, ni Turbine para los flujos: `first()`, `toList()` y `withTimeoutOrNull` cubren lo que la Bitácora necesita.

## 5. Lo que sigue necesitando Robolectric

Que la `Application` sea la nuestra, que el archivo se escriba de verdad en `filesDir` y que compartir no se repita al girar solo se ve con la actividad:

> 🔬 Medido en `ArchitectureTest`: Robolectric crea una `Application` nueva para cada test, con su `filesDir` vacío, pero dentro de un test es la misma para todas las actividades. Por eso "cerrar y abrir" (`destroy()` y otra `buildActivity` sin Bundle) encuentra la lista: el repositorio de la `Application` ya la tiene en memoria. Para probar que el archivo se escribe, hay que mirar el archivo (`theListIsWrittenToAFile`), con `waitUntil`: la escritura va en `Dispatchers.IO`, que el reloj de Compose no espera.

## Para recordar

- El reducer con JUnit; el ViewModel con `setMain` y un *fake*; el archivo con una carpeta temporal; la unión con Robolectric.
- `runTest`, `first()`, `withTimeoutOrNull` y `backgroundScope` bastan para los efectos.
- Un *fake* guarda y cuenta; se mira el resultado, no las llamadas.
- En Robolectric la `Application` dura un test: para probar el disco, mira el archivo.
