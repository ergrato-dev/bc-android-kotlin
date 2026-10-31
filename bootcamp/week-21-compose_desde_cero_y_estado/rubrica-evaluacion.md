# Rúbrica de Evaluación — Semana 21: Compose desde cero y estado

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la pantalla principal en Compose |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué hace el plugin del compilador de Compose? ¿Qué error da el compilador si falta, y qué versión lleva? | 10 |
| 2 | ¿Qué es la recomposición? ¿Qué tiene que pasar para que un `Text` vuelva a pintarse con otro valor? | 10 |
| 3 | Un contador con `var n by mutableIntStateOf(0)` dentro de un composable no pasa de 0. ¿Por qué? ¿Qué dice lint? | 10 |
| 4 | Completa: `remember`, `rememberSaveable`, una propiedad del `ViewModel` y el `SavedStateHandle`, ¿qué sobrevive a la recomposición, al giro y a la muerte del proceso? | 10 |
| 5 | `rememberSaveable { mutableStateOf(Draft("", null)) }` falla. ¿Con qué error, y cuándo? Da dos formas de arreglarlo. | 10 |
| 6 | ¿Qué es un composable sin estado? ¿Qué gana la Bitácora con que `BitacoraScreen` lo sea? | 10 |
| 7 | `LevelPicker` guarda su propio nivel con `remember { mutableStateOf(selected) }`. ¿Qué falla, y por qué? | 10 |
| 8 | ¿Para qué es cada regla: `createComposeRule`, `createAndroidComposeRule` y `createEmptyComposeRule`? ¿Por qué `ui-test-manifest` va en `debugImplementation`, y qué consecuencia tiene? | 10 |
| 9 | ¿Por qué las acciones del `ViewModel` leen del `SavedStateHandle` y no de `state.value`? | 10 |
| 10 | ¿Qué diferencia hay entre `collectAsState()` y `collectAsStateWithLifecycle()`? ¿Y qué hace `WhileSubscribed(5_000)` durante un giro? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Transforma las funciones `@Composable`: les añade los parámetros con los que Compose sabe dónde está cada una y qué ha cambiado. Sin él, el compilador da un error interno: `Back-end (JVM) Internal error: Couldn't inline method call: CALL 'public final fun remember ...`. Viene con Kotlin y lleva su versión (`org.jetbrains.kotlin.plugin.compose` 2.4.21).
2. Volver a ejecutar los composables que leyeron un estado que ha cambiado. El valor tiene que estar en un estado de Compose (`mutableStateOf` y familia), el `Text` tiene que leerlo, y alguien tiene que escribir un valor nuevo.
3. Cada recomposición vuelve a ejecutar `mutableIntStateOf(0)`: sumar 1 programa una recomposición, y la recomposición crea otro estado a 0. Lint: `Creating a state object during composition without using remember [UnrememberedMutableState from androidx.compose.runtime]`. Se arregla con `remember { ... }`.
4. `remember`: la recomposición. `rememberSaveable`: las tres. Una propiedad del `ViewModel`: la recomposición y el giro, no la muerte del proceso. El `SavedStateHandle`: las tres.
5. `IllegalArgumentException: MutableState containing Draft(text=, level=null) cannot be saved using the current SaveableStateRegistry`, al pintar la pantalla por primera vez, no al girarla. Un `Bundle` solo guarda tipos sencillos, `Parcelable` y `Serializable`. Arreglos: cada campo en su `rememberSaveable`, `@Parcelize`, o un `Saver` (`listSaver`).
6. Uno que recibe todo lo que pinta y avisa de los eventos con lambdas, sin decidir nada. `BitacoraScreen` se prueba sola, con un `BitacoraUiState` hecho a mano y sin actividad, y el estado está en un solo sitio, el `ViewModel`.
7. Fallan `thePickerDoesNotChangeOnItsOwn` y `savingCountsAndClearsTheDraft`. El `remember` usa `selected` solo la primera vez: al guardar, la pantalla deja su nivel en `null` y el selector sigue marcando "Alta". Hay dos copias del mismo dato.
8. `createComposeRule`: un composable suelto, con `setContent`. `createAndroidComposeRule<A>`: la actividad `A` entera. `createEmptyComposeRule`: no pinta nada y encuentra lo que pinten las actividades que lances tú, por ejemplo con `Robolectric.buildActivity` para matar el proceso. `ui-test-manifest` pone la actividad vacía de `createComposeRule` en el manifiesto, y los tests de la JVM usan el de la variante: en `testImplementation` no llega. Consecuencia: el APK de debug lleva `androidx.activity.ComponentActivity` exportada; el de release, no.
9. `state` es un `StateFlow` de `stateIn`: lo calcula una corrutina en el hilo principal, solo mientras alguien lo recoge (`WhileSubscribed`). Sin pantalla, o justo después de un cambio, `state.value` va por detrás. El handle es lo que hay. En el ejercicio 02, `save` con `state.value` hace fallar `onlyACompleteDraftIsSaved`.
10. `collectAsStateWithLifecycle` solo recoge mientras la actividad está al menos en `STARTED`; `collectAsState` sigue recogiendo con la app detrás. `WhileSubscribed(5_000)` mantiene el flujo de arriba 5 segundos después del último suscriptor: en un giro, la actividad nueva vuelve a recoger antes, y el flujo no se reinicia.

**Ejercicio 01, paso 5**: (a) Fallan `theDraftSurvivesARotation` y `theDraftSurvivesProcessDeath`. El usuario pierde lo que escribía al girar el móvil, o al volver a la app después de que Android la matara. (b) Fallan `theDraftSurvivesARotation`, `savingNeedsATextAndALevel`, `savingCountsAndClearsTheDraft` y `theDraftSurvivesProcessDeath`; lint: `Error: Creating a state object during composition without using remember [UnrememberedMutableState ...]`. El clic escribe el nivel, eso programa una recomposición, y la recomposición crea otro estado con `null`. (c) `IllegalArgumentException: MutableState containing Draft(text=, level=null) cannot be saved using the current SaveableStateRegistry...`, en los 6 tests que pintan `DraftScreen`. Se guarda con un `Saver` (`listSaver`), con `@Parcelize`, o separando los campos. (d) `thePickerDoesNotChangeOnItsOwn` y `savingCountsAndClearsTheDraft`: el selector se queda con su copia, "Alta", cuando la pantalla ya ha vaciado la suya. (e) Fallan los 8 de `DraftTest` con `Unable to resolve activity for Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=dev.bitacora.agro/androidx.activity.ComponentActivity }`; pasa `theDraftSurvivesProcessDeath`, que usa `MainActivity`, que sí está en el manifiesto.

**Ejercicio 02, paso 6**: (a) `""`. Nadie recoge `state` y, con `WhileSubscribed`, `stateIn` ni siquiera empieza. Con `Eagerly` empieza al crear el `ViewModel`, pero el valor nuevo llega en una corrutina del hilo principal: hace falta que el hilo avance (`shadowOf(Looper.getMainLooper()).idle()`) para verlo. (b) `onlyACompleteDraftIsSaved`: es el único que usa el `ViewModel` sin pantalla; ahí `state.value` es el `DraftUiState()` inicial y `save` no guarda nada. (c) `theDraftSurvivesProcessDeath` (y los tres que miran el handle). `theViewModelSurvivesARotation` pasa porque un giro no destruye el `ViewModel`. (d) Ningún test lo nota, ni lint. En el móvil, con la app detrás, `collectAsState` seguiría recogiendo y el flujo de arriba no pararía nunca. (e) Para poder probarla sola: sin la separación, `itPaintsTheState` e `itOnlySendsEvents` necesitarían un `ViewModel` y su handle. También es lo que hace que la pantalla sirva con otro origen del estado.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Un borrador que no se pierde (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| `LevelPicker` sin `remember`; el estado de `DraftScreen` en `rememberSaveable`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — El estado en el ViewModel (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| `DraftScreen` sin estado ni `ViewModel`; las acciones leen del handle; `collectAsStateWithLifecycle` (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La pantalla principal en Compose

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 20 en verde y mínimo 6 nuevos, esperados escritos a mano: la pantalla sola, sus eventos, la ubicación, el `ViewModel` con un handle hecho a mano, la actividad de prueba solo en debug | 25 |
| Estado | `BitacoraScreen` sin estado; el estado en el `SavedStateHandle`, publicado con `stateIn`; las acciones leen del handle; nada de vistas en `MainActivity` | 25 |
| Build | Compose en el raíz (`apply false`) y en la app; `verification-metadata.xml` regenerado, con los artefactos nuevos revisados en OSV | 10 |
| Adaptación al dominio | Textos de la pantalla en el idioma del dominio y en el segundo idioma; README con lo que pide el paso 9 | 20 |
| Entrega | Rama `week-21`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `BitacoraScreen` recibe el `ViewModel` o guarda estado propio | -10 |
| Una acción del `ViewModel` lee de `state.value` | -10 |
| `collectAsState()` en vez de `collectAsStateWithLifecycle()` | -5 |
| Un test de la semana 20 borrado o debilitado para que pase | -20 |
| `core` o `data` dependen de Android o de Compose | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-21` → `main` en el repositorio del aprendiz.
