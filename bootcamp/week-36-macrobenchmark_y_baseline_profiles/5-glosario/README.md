# Glosario — Semana 36

| Término | Definición |
| --- | --- |
| **AOT** (*ahead of time*) | Compilar el bytecode a código de máquina antes de ejecutarlo. ART lo hace con `dex2oat`, con el móvil sin usar o al instalar, siguiendo el perfil |
| **Arranque en frío** | Abrir la app sin proceso: Android lo crea, carga las clases, crea `Application` y la actividad, y pinta. El más lento |
| **Arranque tibio** | Abrir la app con el proceso vivo y sin la actividad |
| **Arranque caliente** | Traer delante una actividad que estaba en segundo plano |
| **ART** (*Android Runtime*) | La máquina que ejecuta el bytecode de la app: intérprete, JIT y AOT |
| **Baseline Profile** | Lista de clases y métodos que viaja en el APK (`assets/dexopt/baseline.prof`) para que ART los compile desde la primera instalación |
| **`BaselineProfileRule`** | Regla de JUnit que recorre la app varias veces y apunta lo usado: el generador del perfil |
| **`benchmarkRelease`** | Tipo de build que añade el plugin de Baseline Profiles: release, firmada con la clave de debug y trazable desde la shell. Es la que se mide |
| **Coeficiente de variación** (CV) | Desviación típica entre media. Mide el ruido de una serie de medidas: 0,10 es un 10 % |
| **`CompilationMode`** | Cómo deja Macrobenchmark compilada la app antes de medir: `None`, `Partial` (con el perfil) o `Full` |
| **`com.android.test`** | Plugin de módulo que solo tiene tests, en `src/main`, y apunta a otra app con `targetProjectPath` |
| **FrameTimingMetric** | Métrica de Macrobenchmark que da la duración de cada fotograma (`frameDurationCpuMs`) y cuánto se pasó del plazo (`frameOverrunMs`) |
| **HRF** (*human readable format*) | El formato de texto de los perfiles: una regla por línea, `L...;` para clases y `->` para métodos, con las marcas `H`, `S` y `P` |
| **Jank** | Un fotograma que sale tarde y se nota como un salto. A 60 Hz, cada uno tiene 16,7 ms |
| **JIT** (*just in time*) | Compilar mientras la app corre lo que se repite. Gasta CPU en el momento y apunta lo usado en un perfil |
| **Macrobenchmark** | Biblioteca que mide la app entera desde fuera (arranques, recorridos) con trazas del sistema |
| **`MacrobenchmarkRule`** | Regla de JUnit con `measureRepeated`: métricas, compilación, modo de arranque, preparación y el bloque medido |
| **Mediana** | El valor del medio de las medidas ordenadas; con un número par, la media de los dos del medio |
| **Microbenchmark** | Biblioteca que mide una función dentro del proceso, miles de veces |
| **`nonMinifiedRelease`** | Tipo de build que añade el plugin para generar el perfil: release sin R8, con los nombres del código |
| **Percentil** (P90, P99) | El valor por debajo del cual queda ese porcentaje de las medidas. Para los fotogramas, lo que se ve en los peores momentos |
| **Perfetto** | El sistema de trazas de Android. Cada iteración de Macrobenchmark deja un `.perfetto-trace` |
| **`profileable`** | Atributo del manifiesto (`<profileable android:shell="true">`) que deja trazar una app no depurable desde la shell |
| **`profileinstaller`** | Biblioteca de AndroidX que pasa el perfil del APK a ART cuando la app no se instala desde Play |
| **`reportFullyDrawn`** | La app avisando de que está lista para usarse. Marca el final de TTFD |
| **`ReportDrawnWhen`** | El composable de `activity-compose` que llama a `reportFullyDrawn` cuando su condición es cierta |
| **Startup Profile** | Perfil del arranque (`startup-prof.txt`) que usa R8 para poner esas clases juntas en el primer dex |
| **TTFD** (*time to full display*) | `timeToFullDisplayMs`: desde que se pide la actividad hasta `reportFullyDrawn` |
| **TTID** (*time to initial display*) | `timeToInitialDisplayMs`: desde que se pide la actividad hasta su primer fotograma |
| **UiAutomator** | Biblioteca que maneja cualquier app desde fuera, con el árbol de accesibilidad: `UiDevice`, `By`, `Until` |
