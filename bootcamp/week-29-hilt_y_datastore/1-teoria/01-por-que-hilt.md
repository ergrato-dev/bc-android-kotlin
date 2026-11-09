# Por qué Hilt

> En la semana 28, la Bitácora recibió su repositorio por el constructor, y alguien tenía que crearlo: `BitacoraApp`, a mano, con una `viewModelFactory` en la actividad. Con dos dependencias se lleva bien. Con veinte, que dependen unas de otras y viven cada una lo suyo, ese código crece solo y nadie lo prueba. Hilt lo escribe por ti, al compilar, y si el grafo está mal, no compila.

## 🎯 Objetivos

- Ver qué parte de la DI a mano de la semana 28 se repite y se puede generar
- Saber qué es Dagger, qué añade Hilt y qué hace KSP en el build
- Conocer las tres anotaciones que ponen en marcha Hilt en una app
- Reconocer los fallos de Hilt que salen al ejecutar, y saber qué cuesta en el APK

## 1. Lo que hacía `BitacoraApp`

```kotlin
class BitacoraApp : Application() {
    val repository: ObservationRepository by lazy {
        JsonFileObservationRepository(File(filesDir, "observations.json"), Samples.observations)
    }
    val settings: SettingsRepository by lazy {
        DataStoreSettingsRepository(File(filesDir, "datastore/settings.preferences_pb"))
    }
}

private val notes: BitacoraViewModel by viewModels {
    viewModelFactory {
        initializer {
            val app = application as BitacoraApp
            BitacoraViewModel(createSavedStateHandle(), app.repository, app.settings)
        }
    }
}
```

Hace tres cosas, y ninguna es de la Bitácora:

1. **Saber cómo se crea cada cosa**: el repositorio necesita un archivo, el archivo necesita `filesDir`.
2. **Decidir cuánto vive**: `by lazy` en la `Application` quiere decir "una para toda la app".
3. **Entregarla**: la factory busca cada dependencia y llama al constructor en el orden bueno.

Cada clase nueva que necesite el ViewModel obliga a tocar la `Application`, la factory y cada test que la cree. Y si alguien olvida el `by lazy` y crea dos, el fallo sale al ejecutar, a veces solo en un móvil.

## 2. Dagger, Hilt y KSP

**Dagger** es un generador de código: lee anotaciones como `@Inject`, `@Module` y `@Component`, y escribe al compilar las clases que crean y entregan las dependencias, sin reflexión. Si a alguien le falta una dependencia, la compilación falla con el camino entero. Es lo que haces en el [ejercicio 01](../2-practicas/ejercicio-01-el-grafo-de-la-salida/README.md), sin Android.

**Hilt** es Dagger con los componentes de Android ya hechos: uno para la app, uno por actividad, uno por ViewModel (teoría 03), y el código que conecta cada uno con su clase de Android. Tú no escribes ningún `@Component`.

**KSP** (*Kotlin Symbol Processing*) es quien ejecuta el compilador de Dagger dentro del build de Kotlin. Sustituye a `kapt`, que era más lento y está en mantenimiento. En el build de la Bitácora:

```kotlin
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
}
```

`ksp(...)` es una configuración aparte: el compilador de Dagger corre en el build, no va en el APK. Lo generado aparece en `app/build/generated/ksp/`; ábrelo una vez y verás la factory de la semana 28, escrita por otro.

> 🔬 Medido con AGP 9.4.1 y Kotlin 2.4.21: KSP 2.3.13 y Hilt 2.60.1 compilan sin cambios en el resto del build. El compilador de Dagger trae guava 33.6.0-jre, con el aviso GHSA-xxph-c9ww-hj94; el build la restringe a 33.7.2-jre en `ksp` y `kspTest`, como en la semana 16. Al regenerar `verification-metadata.xml` entran 51 componentes nuevos.

## 3. Tres anotaciones para empezar

```kotlin
@HiltAndroidApp
class BitacoraApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notes: BitacoraViewModel by viewModels()
}

@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: ObservationRepository,
    private val preferences: SettingsRepository,
) : ViewModel()
```

- `@HiltAndroidApp` genera el grafo de la app y lo arranca con la `Application`. Sigue haciendo falta `android:name` en el manifiesto.
- `@AndroidEntryPoint` conecta la actividad con el grafo y le cambia la factory de ViewModels por una que sabe crear los `@HiltViewModel`. Por eso `viewModels()` ya no lleva bloque.
- `@HiltViewModel` con `@Inject constructor` apunta el ViewModel en el grafo. El `SavedStateHandle` lo pone Hilt, sin módulo.

Lo que sale de `BitacoraApp` (cómo se crean los repositorios) va a un módulo, `AppModule` (teoría 02).

## 4. Lo que no falla al compilar

Dagger comprueba el grafo, pero no sabe si la actividad está conectada a él. Esto sale al ejecutar:

> 🔬 Medido con Robolectric, quitando una anotación cada vez:
> - Sin `@AndroidEntryPoint` en `MainActivity`: `Cannot create an instance of class dev.bitacora.BitacoraViewModel`. La factory por defecto busca un constructor sin argumentos o con solo el handle.
> - Sin `@HiltAndroidApp` en `BitacoraApp`: `Hilt Activity must be attached to an @HiltAndroidApp Application`.

Los dos salen en el primer test que abre la actividad, así que no llegan a un móvil si los tests de la Bitácora pasan.

## 5. Lo que cuesta

> 🔬 Medido: el APK de release de field pasa de 9,64 MB a 10,34 MB con Hilt, unos 0,7 MB, con R8 activado. Es el runtime de Hilt y Dagger más las clases generadas.

Las alternativas que vas a encontrar:

| | Cuándo se resuelve el grafo | Qué pasa si falta algo |
| --- | --- | --- |
| DI a mano (semana 28) | Lo escribes tú | Depende de ti |
| Hilt / Dagger | Al compilar, generando código | No compila |
| Koin | Al ejecutar, con un DSL de Kotlin | Falla al pedirla (hay un `verify()` para tests) |
| `kotlin-inject`, Metro | Al compilar, como Dagger, pensados para Kotlin y KMP | No compila |

Google recomienda Hilt para apps Android y es lo que usan sus muestras (Now in Android). Para una app con dos dependencias, la DI a mano sigue siendo válida.

## Para recordar

- Hilt genera al compilar lo que la semana 28 escribía a mano: crear, decidir cuánto vive y entregar.
- Dagger es el generador; Hilt le pone los componentes de Android; KSP lo ejecuta en el build.
- `@HiltAndroidApp`, `@AndroidEntryPoint` y `@HiltViewModel` con `@Inject constructor`.
- Un grafo mal hecho no compila; una actividad sin conectar falla en el primer test que la abre.
