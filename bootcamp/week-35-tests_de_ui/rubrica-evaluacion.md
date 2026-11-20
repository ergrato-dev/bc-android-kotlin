# Rúbrica de Evaluación — Semana 35: Tests de UI

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora con tests de UI |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué ve un test de Compose de la pantalla? ¿Qué diferencia hay entre el árbol unido y el sin unir, y cuál usan los finders por defecto? ¿Por qué `onChildren()` de la lista de la Bitácora da 4 nodos con 2 filas, y cómo se cuentan las filas? | 10 |
| 2 | ¿En qué orden prefieres buscar un nodo (texto, descripción, papel, `testTag`) y por qué? ¿Para qué sirve `testTagsAsResourceId`, y quién lo usa? | 10 |
| 3 | La valoración del ejercicio 01 se toca bien en los tests sin tener semántica. ¿Por qué? ¿Qué no funciona sin ella, y qué propiedades le diste? ¿Por qué `steps = 3`? | 10 |
| 4 | ¿Qué diferencia hay entre `assertExists` y `assertIsDisplayed`? ¿Qué es un robot, qué hace y qué no hace? | 10 |
| 5 | ¿Qué espera `waitForIdle` y qué no? ¿Qué pasó con el `delay(1000)` del guardado y con la carga en `Dispatchers.IO`? ¿Cómo espera `waitUntil`, y su límite es real o del reloj de Compose? | 10 |
| 6 | Con el reloj parado, se escribe en la nota. ¿En qué milisegundo empieza el `LaunchedEffect`, y en cuál se guarda? ¿Qué hace `advanceTimeBy(999)` sin `ignoreFrameDuration`? ¿Por qué hace falta otro fotograma para ver «Guardado»? | 10 |
| 7 | ¿Qué graba `IntentsRule`? ¿Por qué el `intended` de compartir busca un `ACTION_CHOOSER` y mira dentro de `EXTRA_INTENT`? ¿Qué hace `intending`, y qué pasa sin él al pedir una foto? | 10 |
| 8 | ¿Qué cambia entre `src/test` y `src/androidTest`? ¿Para qué son el runner de Hilt, el orquestador y `clearPackageData`? ¿Qué es un Gradle Managed Device y qué necesita para ejecutarse? | 10 |
| 9 | ¿Compila `./gradlew build` los tests instrumentados? ¿Cómo lo arregla la Bitácora? ¿Qué pasa si falta `kspAndroidTest`? ¿Qué va en la JVM y qué en un dispositivo? | 10 |
| 10 | ¿Qué hace `captureRoboImage()` con `./gradlew test` a secas, y qué cambia `roborazzi.test.verify=true`? ¿Qué haces ante un `is changed`? ¿Qué tiene que quedar fijo en una captura? ¿Qué encontró la primera captura de la Bitácora, y por qué no lo vio ningún test? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. El árbol semántico: los nodos que también lee TalkBack, con texto, papel, estado y acciones. No ve píxeles ni funciones. El unido junta en un nodo a los descendientes de lo que es `clickable`, un `Button` o `mergeDescendants`; el sin unir deja cada pieza aparte. Los finders usan el unido. La lista da 4 hijos porque cada fila va en un `SwipeToDismissBox`, que deja dos nodos: el fondo de «Borrar» y el `Surface`. Las filas se cuentan por lo que las define: `onAllNodes(hasAnyAncestor(hasTestTag("observations")) and hasClickAction())`.
2. Texto, descripción y papel primero, porque es como encuentra las cosas una persona y lo que comprueba que la pantalla se pueda usar. `testTag`, para lo que no tiene ninguno de los tres (la lista). `testTagsAsResourceId = true` convierte los tags en `resource-id`, que es lo que buscan UiAutomator y Macrobenchmark desde fuera de la app (semana 36).
3. El toque llega por coordenadas (`performTouchInput { click(...) }`) al `pointerInput`, sin pasar por la semántica. Sin ella, `performSemanticsAction(SetProgress)` falla con `the node is missing [SetProgress]`, y `hasStateDescription` no encuentra nada; tampoco TalkBack. Propiedades: `contentDescription`, `stateDescription` («3 de 5»), `progressBarRangeInfo` y `setProgress`. `steps` cuenta los valores entre los extremos: 2, 3 y 4.
4. `assertExists` mira si el nodo está en el árbol; `assertIsDisplayed`, si además se ve. Una fila que el `LazyColumn` no ha compuesto no existe, y una tapada por los botones existe sin verse. Un robot traduce el «qué» del test al «cómo» de la pantalla (qué texto, qué tag, qué acción). No guarda estado, y no comprueba nada fuera de sus métodos `assert…`.
5. Espera a Compose: recomposiciones, fotogramas y animaciones. Medido: tras `waitForIdle`, el `delay(1000)` del efecto no había terminado (reloj en 288 ms), y la carga en `IO` no había llegado (volvió en 1 ms real). `waitUntil` repite `waitForIdle` y la condición. Mientras tanto adelanta el reloj de Compose más deprisa que el real (642 ms reales, reloj en 1296). Su límite es de tiempo real.
6. Empieza en 16 ms, el fotograma siguiente a la tecla (`advanceTimeByFrame()`), y guarda en 1016. `advanceTimeBy(999)` redondea a fotogramas y avanza 1008 ms. «Guardado» sale de un estado que cambia el efecto, y con el reloj parado la recomposición que lo pinta necesita otro fotograma.
7. Todos los `Intent` que salen de la app mientras dura el test. Compartir abre el selector del sistema, y el `ACTION_SEND` con el texto va dentro, en `EXTRA_INTENT`: los matchers se anidan. `intending` contesta en lugar de la otra app: el `ActivityResult` que recibirá el callback. Sin él, la foto no llega, el callback no se llama y la pantalla sigue en «Sin foto». No hay error de cámara: la petición sale y no vuelve nada.
8. `src/test` corre en la JVM; `src/androidTest`, en un dispositivo, dentro de un APK de test que se instala con la app, con `androidTestImplementation` y `kspAndroidTest`. El runner crea `HiltTestApplication` en lugar de `BitacoraApp`, porque en un dispositivo no hay `@Config`. El orquestador ejecuta cada test en su proceso, y `clearPackageData` borra los datos de la app entre uno y otro, para que ningún test dependa del anterior. Un GMD es un emulador descrito en el build que Gradle crea, arranca, usa y apaga (`pixel6Api36FieldDebugAndroidTest`). Necesita aceleración por hardware (KVM).
9. No: `build -m` solo pasa lint por `androidTest`. La Bitácora hace que `check` dependa de `assembleFieldDebugAndroidTest`. Sin `kspAndroidTest`, el APK de test compila sin error (medido), y el fallo solo podría verse al ejecutar en el dispositivo. En la JVM, la lógica y los estados de las pantallas, Intents, capturas, navegación y muerte del proceso. En el dispositivo, lo que solo existe en un Android de verdad: el arranque real, el teclado, los permisos del sistema, el mapa con Play Services, el rendimiento.
10. Con `test` a secas, nada: ni graba ni compara. Con la propiedad, cada `test` compara y falla si cambia o falta la referencia; `record...` sigue grabando. Ante un `is changed`, se mira el `_compare`: si es un fallo, se arregla la app; si es un cambio querido, se regraba y la imagen nueva va al pull request. Fijo: sin color dinámico, con `Surface`, idioma, tamaño de pantalla y estado fijos, y sin animaciones a medias. Encontró «Media» partida en dos líneas a 320 dp: el relleno de 24 dp de `Button` dejaba 40 dp para el texto. Los tests comprobaban que los tres botones miden lo mismo y que dicen lo que tienen que decir, y las dos cosas eran ciertas.

**Ejercicio 01, paso 6**: (a) El nodo con `Tag: 'rating'`, `ContentDescription = '[Estado de la parcela]'`, `ProgressBarRangeInfo = 'ProgressBarRangeInfo(current=3.0, range=1.0..5.0, steps=3)'`, `StateDescription = '3 de 5'` y `Actions = [SetProgress]`: una línea por cada propiedad del `semantics { }`. (b) Fallan `itSavesAfterASecond` y `typingAgainStartsTheWaitAgain`, con `saves == []`. Con el reloj parado, la tecla cambia el estado pero nadie recompone, y el `LaunchedEffect` no empieza hasta un fotograma. (c) Falla con `expected: <[]> but was: <[riego]>`: el reloj avanza 16 + 1008 = 1024 ms, más allá de 1016. (d) Con el reloj parado, el cambio de `saved` no se pinta hasta el siguiente fotograma. (e) Falla: `waitForIdle` vuelve en ~1 ms real, porque el trabajo de `IO` no es de Compose. Inyectando el dispatcher (o el `load` ya en el hilo del test), `Plots` no saldría del reloj.

**Ejercicio 02, paso 7**: (a) Falla `withText("Con foto")`: la petición sale, nadie contesta y el callback no se llama. No hay error porque Robolectric no comprueba que exista una cámara. (b) Falla `aReportWithText` con `... .png is changed`: los tests de Espresso buscan por id y no les afecta el texto. `compareRoborazziDebug` no falla y deja `_actual` y `_compare`, con la referencia, la diferencia en rojo y la nueva. (c) La barra de acción tapa el campo en la captura, y la nota sale vacía: `typeText` pulsa sobre la barra y el campo no recibe el foco, sin error. (d) `NullPointerException: Intents not initialized. Did you forget to call init()?`. (e) `ReportTest` no mira píxeles. Sin `NATIVE`, Roborazzi guarda un volcado de la jerarquía de vistas (cajas de colores con sus propiedades) en lugar de la pantalla (medido).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La semántica y el reloj (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| La semántica en el `Modifier` del `Canvas`, con `setProgress` que redondea y limita; el `LaunchedEffect` con `text` como clave y sin guardar vacío; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado, con la salida de (a) y el cálculo de (c) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Vistas, intents y capturas (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 y `anEmptyReport` en verde, con las dos referencias en `src/test/screenshots` | 13 |
| «Enviar» con `isNullOrBlank`; el `Intent` con `mailto:`, asunto y texto recortado; la foto con `TakePicturePreview`; la captura de `anEmptyReport` con «Enviar» apagado (revisión) | 3 |
| `NOTAS.md`: paso 7 explicado, con los mensajes de (b) y (d) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora con tests de UI

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada; `check` compila `androidTest` | 10 |
| Tests | Los de la semana 34 y `UiTest` en verde, y mínimo 5 nuevos con el valor esperado escrito a mano (paso 11) | 25 |
| Semántica, robot e Intents | El tag de la lista y `testTagsAsResourceId`; el robot usado en `TagsTest` y `ShareTest`, que cuenta filas y no hijos; `intended` sobre el selector y el texto de dentro | 20 |
| Capturas y dispositivo | Al menos 4 capturas deterministas en el repositorio, verificadas por `test`; el fallo de la primera arreglado en la app; `src/androidTest` con Hilt, orquestador, `clearPackageData` y el GMD; la restricción de guava en `kspAndroidTest` | 20 |
| Adaptación al dominio | README con lo que pide el paso 12: la captura de antes y la de después, tus números, y la ejecución en un dispositivo (o por qué no) | 15 |
| Entrega | Rama `week-35`, pull request con `test` y las capturas en el diff | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una captura regrabada para que pase sin explicar la diferencia en el pull request | -15 |
| El fallo de la captura «arreglado» cambiando el test, el tamaño de la pantalla o la referencia | -15 |
| Capturas con color dinámico, la hora o algo animándose, que cambian solas | -10 |
| `Thread.sleep` en un test de UI | -10 |
| Un `testTag` donde había texto o papel con el que buscar | -5 |
| Roborazzi, Espresso o el runner en `implementation` (llegan al APK) | -10 |
| Un resultado de un dispositivo que no se ha ejecutado | -20 |
| Un test de la semana 34 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-35` → `main` en el repositorio del aprendiz.
