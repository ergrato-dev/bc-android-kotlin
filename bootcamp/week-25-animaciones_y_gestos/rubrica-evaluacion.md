# Rúbrica de Evaluación — Semana 25: Animaciones y gestos

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: borrar, deshacer y moverse |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué devuelve `animateColorAsState` mientras su destino no cambia, y cuando cambia? ¿Qué diferencia hay entre `tween(300)` y `spring()`? | 10 |
| 2 | ¿De dónde saca el tiempo una animación de Compose, en el móvil y en un test? ¿Por qué un test que pulsa y mira el color siempre ve el final? | 10 |
| 3 | Escribe un test que mire una animación a los 150 ms. Si el estado cambia desde el test y no con un clic, ¿qué hace falta antes de avanzar el reloj? | 10 |
| 4 | `if (expanded)` o `AnimatedVisibility(expanded)`: ¿qué cambia en pantalla y en el árbol de semántica al cerrar? | 10 |
| 5 | ¿Qué necesita una `LazyColumn` para animar sus filas, y qué anima `animateItem()`? Con el reloj parado, tras navegar, un test encuentra textos de dos pantallas: ¿por qué? | 10 |
| 6 | Ordena de más alta a más baja las capas de gestos. ¿Por qué empezar por la más alta? ¿Qué lee TalkBack de una pulsación larga? | 10 |
| 7 | ¿Qué dos cosas no hace `SwipeToDismissBox` para TalkBack, y cómo se arreglan? ¿En qué nodo va la acción? | 10 |
| 8 | Con `actionLabel` y sin `duration`, ¿cuánto dura un `Snackbar`? ¿Por qué la fila devuelta con "Deshacer" se volvía a borrar, y cómo se arregla? | 10 |
| 9 | `background(color)` o `drawBehind { drawRect(color.value) }` con un color animado: ¿en qué fase se lee cada uno, y qué cuesta? ¿Lo ven los tests? | 10 |
| 10 | ¿Por qué el código de la siguiente observación se repetía al borrar, qué pasaba, y dónde se arregla? ¿Qué no debe ser nunca una animación? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Mientras no cambia, el mismo valor. Cuando cambia, en cada fotograma uno intermedio hasta llegar. `tween(300)` llega en 300 ms con una curva fija; `spring()` (el de por defecto) se mueve como un muelle, sin duración, y se puede interrumpir sin saltos. En el ejercicio 01, con el `spring` por defecto, a los 150 ms la insignia ya tenía el color de Baja.
2. Del reloj de fotogramas que le da el `Recomposer`: en el móvil lo marca la pantalla; en un test, `rule.mainClock`. Con `autoAdvance = true`, cada espera del test (`performClick`, `assert...`) avanza hasta que no quedan animaciones: en la sonda, tras el clic, `currentTime` estaba en 400 ms.
3. `rule.mainClock.autoAdvance = false`, el clic, `advanceTimeBy(150)` y la comprobación; después `advanceTimeBy(250)` y el valor final. Si el estado cambia desde el test, `rule.waitForIdle()` antes de avanzar: con el reloj parado recompone sin avanzarlo. Sin él, la recomposición llega al final del avance y la animación empieza ahí (en el proyecto, a los 150 ms la insignia seguía en el color de Alta).
4. Con `if`, el contenido salta y desaparece del árbol en el acto: en el ejercicio 01, A-2 ya estaba en su sitio final a los 50 ms (`64.0 < 88.0 < 88.0`). Con `AnimatedVisibility`, crece y se va poco a poco, y mientras se va sigue en el árbol: a los 50 ms de cerrar, la descripción existe; a los 1050, no.
5. Una clave por fila. Anima la que aparece, la que se va (fundidos) y las que cambian de sitio (con un `spring`): en la sonda, la de debajo pasaba de 228 a 214 px a los 100 ms y a 204 a los 300. `NavDisplay` anima con un `AnimatedContent`: durante la transición están las dos pantallas en el árbol (en la sonda, hasta los 550 ms).
6. `clickable`, `combinedClickable`, `draggable`/`anchoredDraggable`, `pointerInput { detectTapGestures }`, `pointerInput` con cada evento. Las altas traen la semántica hecha (`OnClick`, `OnLongClick`); `pointerInput` no trae nada. TalkBack ofrece la pulsación larga como una acción, con `onLongClickLabel` como nombre; sin él, `label` es `null`.
7. No añade ninguna acción para borrar, y su fondo está siempre compuesto: TalkBack leía `"borrar, fila A-1"`. El fondo, con `clearAndSetSemantics {}`; y una `CustomAccessibilityAction("Borrar")` en `customActions`. La acción va en el nodo que enfoca TalkBack, el que tiene el texto de la fila: en la Bitácora, el de `ObservationRow`.
8. `Indefinite`: no se va nunca. Con `SnackbarDuration.Long`, 10 s. `rememberSwipeToDismissBoxState()` guarda el estado, y la `LazyColumn` conserva el estado guardado de cada clave: la fila devuelta volvía "deslizada" y `SwipeToDismissBox` llamaba otra vez a `onDismiss` (`remove`, `restore`, `remove`). Se arregla con `remember { SwipeToDismissBoxState(Settled, threshold) }`.
9. `background(color)` lee el color en la composición: en la sonda, 19 recomposiciones en 300 ms. `drawBehind { drawRect(color.value) }` lo lee al dibujar: ninguna. Los tests no lo ven: en el ejercicio 01, con `background`, los 8 siguen en verde.
10. Era `"A-${size + 1}"`: con A-1 y A-3, la siguiente era otra A-3, y la `LazyColumn` cerraba la app con `IllegalArgumentException: Key "A-3" was already used`. Es una regla del dominio: `nextCode()` en `core`, uno más que el mayor. Una animación no puede ser la única señal de que algo ha pasado: con "Quitar animaciones", se salta al final.

**Ejercicio 01, paso 5**: (a) Ninguno falla. Con `background`, la insignia se recompone en cada fotograma de la animación; con `drawBehind`, solo al cambiar de nivel (en la sonda, 19 frente a 0). Los tests comprueban lo que se ve, no lo que cuesta. (b) `theLongPressHasALabel`: `expected:<Cambiar el nivel> but was:<null>`. TalkBack ofrece la pulsación larga sin decir qué hace. (c) `theBadgeChangesColorIn300Milliseconds`: `Values should be different. Actual: Color(0.76862746, 0.93333334, 0.8156863, ...)`: a los 150 ms ya es el de Baja. (d) `theDescriptionGrowsIn` (`64.0 < 88.0 < 88.0`: A-2 ya está abajo a los 50 ms) e `itGoesOutSlowlyToo` (a los 50 ms la descripción ya no existe). (e) Falla con `Values should be different`: con el reloj automático, la pulsación espera a que Compose esté en reposo, y eso avanza hasta el final de la animación.

**Ejercicio 02, paso 5**: (a) `undoPutsItBackInItsPlace`. Se imprime `remove A-2`, `restore A-2` y otra vez `remove A-2`: la fila vuelve con el estado guardado, deslizada, y se borra otra vez. (b) `swipingRightDoesNothing`: hacia la derecha también borra. Quien desliza para otra cosa (volver, o nada) borra sin querer. (c) `talkBackReadsOnlyTheRow`: `but was:<[Borrar, A-1 · Alta · riego roto en el sector norte]>`. TalkBack diría "Borrar" antes de cada fila. (d) `theSnackbarGoesAwayAfterTenSeconds`: a los 11 s sigue "Deshacer". Con una acción, la duración por defecto es `Indefinite`. (e) `theRowsBelowMoveUpSlowly`: `76.0 < 76.0 < 136.0`, A-3 ya está arriba a los 50 ms.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La tarjeta que se mueve (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `combinedClickable` con `onLongClickLabel`; `AnimatedVisibility`; el color leído en `drawBehind`, sin `by`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado, con el contador del apartado (a) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Deslizar para borrar (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| Un solo sentido; el estado en `remember`; el fondo sin semántica; la acción "Borrar"; `SnackbarDuration.Long` (revisión) | 3 |
| `NOTAS.md`: paso 5 explicado, con lo que imprime el apartado (a) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Borrar, deshacer y moverse

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 24 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: `A-2` y `A-10`, el aviso en inglés, dos borradas seguidas, deshacer tras la muerte del proceso, la fila a los 50 y a los 150 ms, los códigos en `field` | 25 |
| Borrar y deshacer | Un solo sentido; el estado en `remember`; el fondo sin semántica; la acción "Borrar" en la fila; `delete` y `undo` en el `ViewModel`, con el handle; el aviso de 10 s que cierra el anterior | 25 |
| Movimiento | `animateItem()` en las filas; la insignia animada en `drawBehind`; `nextCode()` en `core` | 10 |
| Adaptación al dominio | Los textos de tu dominio, en los dos idiomas; README con lo que pide el paso 8 | 20 |
| Entrega | Rama `week-25`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Borrar sin deshacer, o un gesto sin su acción de accesibilidad | -15 |
| Borrar en los dos sentidos | -5 |
| Lo borrado se guarda en la pantalla y no en el `ViewModel` | -10 |
| Un valor animado leído en la composición donde basta el dibujo | -5 |
| El cálculo del código fuera de `core`, o sin test | -10 |
| Un test de la semana 24 o del starter borrado o debilitado para que pase | -20 |
| Una dependencia nueva (una biblioteca de animaciones o de gestos), o un sha256 añadido a mano | -20 |
| `core` o `data` dependen de Android o de Compose | -10 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-25` → `main` en el repositorio del aprendiz.
