# Glosario — Semana 16

| Término | Definición |
| --- | --- |
| **ActivityScenario** | Clase de `androidx.test:core` que arranca una actividad en un test y la mueve entre estados |
| **addObserver** | Registra un observador en un `Lifecycle`; si se registra tarde, recibe al momento los eventos que faltan |
| **android-all** | El Android entero, preparado para la JVM, sobre el que Robolectric ejecuta los tests (`android-all-instrumented`) |
| **Bundle** | Mapa de tipos sencillos que Android guarda por la actividad y le devuelve al recrearla; debe ser pequeño |
| **Cambio de configuración** | Giro, idioma, modo oscuro, tamaño de ventana...: Android destruye la actividad y crea otra |
| **ComponentActivity** | La actividad de AndroidX: `Lifecycle` observable, `ViewModel` y estado guardado |
| **configChanges** | Atributo del manifiesto que evita la recreación para ciertos cambios; no sustituye a guardar el estado |
| **DefaultLifecycleObserver** | Interfaz con un método por evento del ciclo de vida (`onStart`, `onStop`...) |
| **Lifecycle** | El ciclo de vida observable de una actividad: estado actual y observadores |
| **Lifecycle.State** | `INITIALIZED`, `CREATED`, `STARTED`, `RESUMED`, `DESTROYED` |
| **lifecycleScope** | `CoroutineScope` de la actividad; se cancela en `onDestroy` |
| **Muerte del proceso** | Android mata la app en segundo plano sin llamar a `onDestroy`; al volver, solo queda el `Bundle` |
| **onCleared** | Método del `ViewModel` que se llama cuando la pantalla se cierra de verdad, no en un giro |
| **onSaveInstanceState** | Método donde la actividad guarda su estado en el `Bundle`; desde Android 9, después de `onStop` |
| **repeatOnLifecycle** | Ejecuta un bloque mientras el ciclo de vida esté en un estado y lo cancela al bajar |
| **Robolectric** | Biblioteca que ejecuta código Android en la JVM, sin emulador |
| **robolectric.offline** | Propiedad que impide a Robolectric descargar nada; con `robolectric.dependency.dir`, usa los jars de Gradle |
| **SavedStateHandle** | Mapa del `ViewModel` que se guarda en el `Bundle`: sobrevive al giro y a la muerte del proceso |
| **setQualifiers** | `RuntimeEnvironment.setQualifiers("+land")`: cambia la configuración en un test de Robolectric |
| **TransactionTooLargeException** | Fallo al pasar al sistema un `Bundle` demasiado grande |
| **ViewModel** | Objeto que guarda el estado de una pantalla fuera de la actividad; sobrevive al giro, no a la muerte del proceso |
| **viewModels()** | Delegado (`by viewModels()`) que crea o recupera el `ViewModel` de la actividad |
