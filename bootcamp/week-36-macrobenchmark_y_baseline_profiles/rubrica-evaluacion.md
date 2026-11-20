# Rúbrica de Evaluación — Semana 36: Macrobenchmark y Baseline Profiles

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora medida |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre un arranque en frío, tibio y caliente? ¿Qué miden TTID y TTFD, y por qué en la Bitácora están tan lejos uno de otro? | 10 |
| 2 | ¿Por qué se compara con la mediana y no con la media? ¿Qué es el coeficiente de variación, cómo lo calcula Macrobenchmark y para qué sirve al comparar? ¿Con qué fotogramas calcula un P90? | 10 |
| 3 | ¿Por qué no se mide la debug, ni en Robolectric? ¿Qué tiene de especial `benchmarkRelease`? ¿Qué pasa si ejecutas un benchmark en un emulador, y qué valen esos números? | 10 |
| 4 | ¿Qué es un módulo `com.android.test`? ¿A qué paquete apunta la instrumentación de su APK, y cómo maneja entonces la app? ¿Para qué sirve `targetProjectPath`, y `missingDimensionStrategy` en la Bitácora? | 10 |
| 5 | En `measureRepeated`, ¿qué hacen `setupBlock`, `startupMode` y el bloque final? ¿Por qué el recorrido de la lista falla con `StartupMode.COLD`? ¿Cómo encuentra UiAutomator la lista, y por qué no la encuentra si está vacía? | 10 |
| 6 | ¿Qué hacen el intérprete, el JIT y el AOT de ART? ¿Qué problema resuelve un Baseline Profile que no resuelven los perfiles de la nube? ¿Qué significan `H`, `S`, `P` y `L...;` en una regla? | 10 |
| 7 | Sigue un perfil desde `baseline-prof.txt` hasta el móvil. ¿Por qué la Bitácora ya tenía `assets/dexopt/baseline.prof` antes de generar nada? ¿Qué hace `profileinstaller`, y por qué ya estaba? | 10 |
| 8 | ¿Cómo se genera el perfil y dónde queda? ¿Por qué en un GMD y con `useConnectedDevices = false`? ¿Por qué va al repositorio, y cuándo se regenera? | 10 |
| 9 | ¿En qué se diferencia el Startup Profile del Baseline Profile? ¿Qué cambió `dexLayoutOptimization` en la Bitácora, y por qué? ¿Dónde va `ReportDrawnWhen`, y dónde se puede comprobar que funciona? | 10 |
| 10 | ¿Qué simulan `None`, `Partial(Require)` y `Full`? Con los números del emulador, ¿mejora el arranque con el perfil? ¿Y los fotogramas? ¿Qué haría falta para decidirlo de verdad? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. En frío no hay proceso: Android lo crea, carga clases, crea `Application` y la actividad y pinta. En tibio el proceso existe y falta la actividad; en caliente la actividad solo vuelve delante. TTID va hasta el primer fotograma, y lo mide Android. TTFD va hasta que la app llama a `reportFullyDrawn`. En la Bitácora, el primer fotograma sale vacío mientras Room lee la lista: en el emulador, 695,6 ms frente a 1 324,2 ms de mediana.
2. Un arranque raro (el móvil ocupado) mueve mucho la media y casi nada la mediana. El CV es la desviación típica entre la media, y mide el ruido. Macrobenchmark usa la desviación típica muestral, dividida entre n − 1 (medido en el ejercicio 01). Si el ruido es mayor que la diferencia, no se puede concluir nada. El P90 se calcula con los fotogramas de todas las iteraciones juntos, interpolando entre vecinos.
3. Una app depurable no corre como la que se publica, y Robolectric no tiene ni ART ni GPU. `benchmarkRelease` es release, firmada con la clave de debug, sin `debuggable` y con `<profileable android:shell="true">`: Macrobenchmark puede trazarla desde la shell. En un emulador falla con `ERRORS (not suppressed): EMULATOR`; con `suppressErrors=EMULATOR` mide, pero con la CPU compartida y la GPU por software: sirve para montar y aprender, no para decidir.
4. Un módulo que solo tiene tests, con su código en `src/main`. Su APK se instrumenta a sí mismo (`targetPackage="dev.bitacora.baselineprofile"`) y maneja la app desde fuera, como la shell: la arranca, la mata, la toca. `targetProjectPath` dice qué app se mide, y Gradle la empaqueta antes. La app tiene el flavor `environment` y el módulo no: `missingDimensionStrategy` elige `field`.
5. `setupBlock` prepara cada iteración sin medir, `startupMode` dice cómo arrancar, y el bloque final es lo que se mide. Con `COLD`, Macrobenchmark mata la app al terminar `setupBlock`, y el bloque medido empieza sin app (`No processes for name ...`). UiAutomator la encuentra por `By.res("observations")`, el `testTag` que `testTagsAsResourceId` convierte en `resource-id`. Un `LazyColumn` sin filas no aparece en su árbol.
6. El intérprete ejecuta el bytecode tal cual; el JIT compila lo que se repite mientras la app corre, y apunta lo usado; el AOT (`dex2oat`) compila por adelantado lo que dice el perfil. Los perfiles de la nube tardan días en formarse tras cada versión y solo llegan con Play. El Baseline Profile va en el APK: está desde la primera instalación. `H` es caliente, `S` arranque y `P` después del arranque; `L...;` es una clase.
7. `baseline-prof.txt` de la app (a mano o generado) más el de cada biblioteca se juntan en `merged_art_profile`, se compilan a `assets/dexopt/baseline.prof` y `.profm`, y en el móvil Play o `profileinstaller` se lo pasan a ART. Compose, Room, Ktor y las demás traen su perfil: 14 969 bytes sin ninguno propio, 26 302 con el generado (medido). `profileinstaller` copia el perfil a donde ART lo busca cuando la app no viene de Play. La 1.4.0 llegaba con Compose.
8. `BaselineProfileRule.collect` recorre la app; `./gradlew :app:generateBaselineProfile` lo ejecuta y el plugin copia el resultado a `app/src/fieldRelease/generated/baselineProfiles`. Con un GMD sale igual en cualquier máquina, y `useConnectedDevices = false` evita que dependa de lo que haya enchufado. Va al repositorio porque el build lo necesita sin dispositivo y su diff se revisa. Se regenera cuando cambia el código del recorrido o sus bibliotecas, y antes de cada versión.
9. El Baseline Profile lo lee ART, para compilar; el Startup Profile, R8, para poner las clases del arranque juntas en el primer dex. Sin R8, `dexLayoutOptimization` no cambió nada (mismos dex, mismas clases: medido); se ve en la semana 37. `ReportDrawnWhen` va antes del `return` que espera la lista. En Robolectric no llega nunca a avisar (medido): se comprueba en `DeviceTest` y en el JSON del benchmark (`timeToFullDisplayMs`).
10. `None`: recién instalada sin perfil. `Partial(Require)`: con el perfil, como desde Play, y falla si no lo hay. `Full`: todo compilado, el techo. En el emulador, TTID cambia un −1,8 % con un CV del 8 al 10 %: ruido; `Full` salió más lento que `None`. TTFD −6,6 % con un CV del 11,7 %: no se puede decir. Los fotogramas mejoraron en la mediana en las dos ejecuciones (un 6 a 8 %): la pista más clara, pero en un emulador. Haría falta un móvil real, siempre el mismo, con la CPU bloqueada y el `context` junto a los números.

**Ejercicio 01, paso 6**: (a) TTID con `Full` frente a `None`: +1,2 %, `NOISE`. Todo compilado no puede ser más lento de verdad: es ruido. (b) Con `maxNoise = 0.12`, `FASTER` con un −6,6 %. No es una mejora demostrada: se ha cambiado la regla después de ver los datos. (c) P90 de 53,9 a 48,9 ms, un −9,2 %. Sin CV de los fotogramas, la regla no sabe cuánto ruido hay. (d) Hay un 1 826,5 ms; los demás van de 1 169 a 1 465. Sin él, la media baja de 1 341,8 a 1 307,2 ms (−34,6) y la mediana de 1 324,2 a 1 311,9 (−12,3). (e) El ruido de la mediana baja con la raíz del número de iteraciones: para la mitad de ruido hacen falta cuatro veces más.

**Ejercicio 02, paso 8**: (a) Falla `compileReleaseArtProfile` con `Error parsing baseline-prof.txt : baseline-prof.txt:NNNN:N error: Illegal token 'd'`. La línea es la del perfil fusionado, con las reglas de las bibliotecas delante. (b) Sin tus reglas, 11 474 bytes; con ellas, 11 509 (medido con la solución). La app tiene muy pocas clases: casi todo es Compose. (c) Compila. Con `UseIfAvailable`, una app sin perfil se mediría sin él, sin fallar. (d) La app no tiene flavors. (e) A sí mismo, `dev.bitacora.benchmark`: la maneja desde fuera, desde la shell.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Leer un benchmark (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| La mediana sin modificar la lista, el CV muestral, el percentil con interpolación y sin salirse, `compare` mirando el ruido antes que el cambio; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado, con los números de (a), (c) y (d) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Un perfil y su benchmark (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Reglas con comodines y comentadas; `:benchmark` con `targetProjectPath` y la restricción de `wire-runtime`; los dos benchmarks con `None` y `Partial(Require)` y el generador que recorre la lista (revisión) | 3 |
| `NOTAS.md`: paso 8 explicado, con el mensaje de (a) y los tamaños de (b); el paso 7 hecho o dicho que no | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora medida

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada; los benchmarks ensamblados por `build`, sin avisos del plugin | 10 |
| Tests | Los de la semana 35 y `PerformanceTest` en verde, y mínimo 5 nuevos con el valor esperado escrito a mano (paso 12) | 25 |
| Benchmarks | El módulo con `targetProjectPath` y el GMD; el arranque con tres compilaciones; la lista con UiAutomator por `resource-id` y sin `startupMode`; `ReportDrawnWhen` antes del `return`, comprobado en `DeviceTest` | 20 |
| Perfiles | Baseline Profile y Startup Profile generados y en el repositorio, en el APK de release; las variantes de demo solo debug; la restricción de `wire-runtime` | 20 |
| Adaptación al dominio | README con lo que pide el paso 13: el perfil de antes y el tuyo, tus números con su `context`, y una conclusión que no diga más de lo que dicen (o por qué no hay números) | 15 |
| Entrega | Rama `week-36`, pull request con `test`, el diff del perfil resumido y tus números | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Números de un dispositivo que no se ha ejecutado, o del emulador presentados como de un móvil | -20 |
| Una mejora afirmada con un CV mayor que la diferencia | -10 |
| `suppressErrors=EMULATOR` en el build en lugar de la línea de comandos | -5 |
| El perfil generado editado a mano, o generado en la variante de demo | -15 |
| Macrobenchmark o UiAutomator en la app (llegan al APK) | -10 |
| `Thread.sleep` en un benchmark | -10 |
| Un test de la semana 35 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-36` → `main` en el repositorio del aprendiz.
