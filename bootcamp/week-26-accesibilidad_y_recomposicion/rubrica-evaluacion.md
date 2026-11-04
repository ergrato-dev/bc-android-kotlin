# Rúbrica de Evaluación — Semana 26: Accesibilidad y recomposición

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: que se lea bien y recomponga poco |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre el árbol de semántica unido y el sin unir? ¿Cuál llega a TalkBack, y qué es una "parada"? | 10 |
| 2 | ¿Qué leía TalkBack de una fila de la Bitácora en la semana 25? ¿Por qué `hideFromAccessibility()` en la caja de la insignia no lo arregla y `clearAndSetSemantics {}` sí? | 10 |
| 3 | Un dibujo de 24 dp que se pulsa: ¿qué le falta para TalkBack, y cómo se le da? ¿Qué `contentDescription` lleva un icono que solo decora? | 10 |
| 4 | Una fila con un texto y un `Switch`: ¿cuántas paradas son, qué lee TalkBack en cada una, y cómo se hace una sola? | 10 |
| 5 | ¿Cómo dice un botón que está seleccionado? ¿Para qué sirven los encabezados? ¿Cuándo una región viva `Polite` y cuándo `Assertive`? | 10 |
| 6 | ¿Por qué `assertTouchWidthIsAtLeast(48.dp)` pasa con un botón de 24 dp? ¿Dónde va `minimumInteractiveComponentSize()`, y qué se comprueba en su lugar? | 10 |
| 7 | ¿Qué hace `enableAccessibilityChecks()` en un test de Robolectric? ¿Qué hace falta en un test con `fontScale = 2f` para que el texto se mida? | 10 |
| 8 | ¿Con qué compara Compose un parámetro estable y uno inestable, con *strong skipping*? ¿Por qué `Observation` salía `unstable` en el informe, y cómo se arregla sin tocar `core`? | 10 |
| 9 | En la Bitácora, ¿por qué teclear no recomponía las filas de la demo antes de borrar, y sí después? ¿Qué dos arreglos lleva, y qué hace cada uno? | 10 |
| 10 | ¿Para qué sirve `derivedStateOf`, y cuándo solo añade trabajo? ¿Por qué hay que contar antes de optimizar? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. El unido junta en un nodo a los descendientes de lo que une (`clickable`, `toggleable`, los botones de Material); el sin unir tiene un nodo por composable. El unido es el que se convierte en los `AccessibilityNodeInfo` de TalkBack, y cada nodo unido que se enfoca es una parada: TalkBack lee los textos de todos sus hijos juntos.
2. "A, A-1 · Alta · riego roto en el sector norte, -34.6037, -58.3816": la letra de la insignia es un hijo de la fila. `hideFromAccessibility()` esconde la caja, pero el `Text` de dentro sigue llegando a TalkBack (`isImportantForAccessibility=true`). `clearAndSetSemantics {}` borra la semántica del nodo y de todos sus hijos.
3. No tiene nombre ni papel, y no tiene sitio para el dedo. `clickable(role = Role.Button, ...)` o `toggleable(..., role = Role.Checkbox)`, `semantics { contentDescription = ... }` con el nombre de lo que hace, de los recursos, y `minimumInteractiveComponentSize()` antes del `size`. Un icono que decora, `contentDescription = null`.
4. Dos: "Urgente", sin papel ni estado, y "interruptor, desactivado", sin nombre; y el texto no se pulsa. Una sola: la `Row` con `toggleable(urgent, role = Role.Switch, ...)` y el `Switch` con `onCheckedChange = null`. En el ejercicio 01, con el `Switch` pulsable había tres nodos con estado en vez de dos.
5. Con `semantics { selected = ... }`: en el detalle, los nodos llevan `stateDescription` "Seleccionado" o "No seleccionado". Los encabezados (`heading()`) son el gesto de TalkBack para saltar de parte en parte de una pantalla. `Polite` espera a que TalkBack termine lo que lee (el resumen); `Assertive` lo interrumpe, para lo urgente.
6. Porque Compose agranda solo el área de toque hasta 48 dp (en la sonda, de -12 a 36), pero no hace sitio: dos botones juntos se pisan (en el ejercicio 01, 236-284 con 272-320). `minimumInteractiveComponentSize()` va antes del `size`: reserva 48 dp en la fila; después, no hace nada. Se comprueba que las áreas no se pisen (`overlaps`).
7. Nada: mira si el dispositivo es Robolectric, escribe `Accessibility checks are currently not supported by Robolectric` en el log y deja pasar todo (en la sonda, un botón de 20 dp, uno sin etiqueta y un texto gris claro pasaban). Sirve en tests instrumentados. Con letra grande, `@GraphicsMode(GraphicsMode.Mode.NATIVE)`: sin él, Robolectric no mide el texto, y la línea sale cortada aunque no tenga `maxLines`.
8. Estable con `equals`; inestable con `===` (la misma instancia). `Observation` está en `core`, que no pasa por el compilador de Compose: para él no se sabe si es inmutable. Con un archivo de estabilidad (`stabilityConfigurationFiles`, `dev.bitacora.core.*`) desde `app`: el informe dice `stable observation: Observation`.
9. Antes de borrar, el handle no tenía `"observations"` y el estado usaba siempre `Samples.observations`, las mismas instancias. Después, `combine` volvía a ejecutar `decode` con cada tecla y sacaba instancias nuevas e iguales: 5 teclas, 10 recomposiciones (2 filas). El `map` antes de `combine` no saca instancias nuevas si no cambia el JSON (0); el archivo de estabilidad hace que las nuevas e iguales se comparen con `equals` (también 0, solo).
10. Para leer en la composición algo que cambia mucho menos que lo que lee: `firstVisibleItemIndex > 0` (en el ejercicio 02, de 8 recomposiciones de la pantalla a 1). Si el resultado cambia tanto como lo que lee, solo añade trabajo. Porque una recomposición de más no se ve, y un arreglo a ciegas (un `remember` o un `@Stable` de más) no hace nada o esconde un fallo si la promesa es falsa.

**Ejercicio 01, paso 6**: (a) `theBadgeIsNotRead`: la "A" es una parada propia, y TalkBack la lee antes del código. (b) `followAndShareDoNotOverlap`: `Rect.fromLTRB(236.0, 0.0, 284.0, 48.0) se pisa con Rect.fromLTRB(272.0, 0.0, 320.0, 48.0)`. El área de toque se agranda sola a 48; lo que no hay es sitio. (c) `urgentIsOneStop`: `Expected '2' nodes but found '3'`. La fila y el `Switch` vuelven a ser dos nodos con estado para la misma cosa: dos paradas, la segunda sin nombre. (d) Falla: sin los gráficos nativos, Robolectric no mide el texto, y la línea sale cortada aunque quepa.

**Ejercicio 02, paso 4**: (a) Antes, `runtime class dev.bitacora.Item` (`Uncertain(List)`) y `row: Item`; después, `stable class` y `stable row: Item`. La compilación de los tests escribe el mismo archivo después, y gana. (b) `aNewLevelRecomposesOnlyItsRow`: `[1, 1, 1, 1]`. Con `remember`, el borrador no saca `Item` nuevos; un nivel nuevo cambia la lista, el `map` saca cuatro `Item` nuevos, y sin `@Immutable` se comparan con `===`. (c) `scrollingRecomposesTheScreenOnce`: `expected:<1> but was:<8>`. Cada fila que sale por arriba cambia el índice, que se lee en la composición. (d) El Layout Inspector de Android Studio, con las cuentas de recomposición.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La ficha que lee TalkBack (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `heading()`; la insignia con `clearAndSetSemantics {}`; los botones con papel, nombre de los recursos y `minimumInteractiveComponentSize()` antes del `size`; la fila `toggleable` con el `Switch` sin `onCheckedChange`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado, con los números del apartado (b) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — La lista que no recompone (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `@Immutable` en `Item`; `derivedStateOf` dentro de `remember`; ningún test tocado (revisión) | 3 |
| `NOTAS.md`: paso 4 explicado, con el informe de antes y de después | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Que se lea bien y recomponga poco

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 25 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: la fila en inglés, la letra al doble, el nivel Baja, el error del campo, la instancia nueva tras `replace`, el resumen tras borrar | 25 |
| Accesibilidad | La insignia callada; los dos encabezados; el resumen como región viva; el nivel seleccionado; ningún texto de accesibilidad escrito en Kotlin | 25 |
| Recomposición | `decode` en un `map` antes de `combine`; el archivo de estabilidad; los informes detrás de `-PcomposeReports` | 10 |
| Adaptación al dominio | Los textos de tu dominio, en los dos idiomas; README con lo que pide el paso 9 | 20 |
| Entrega | Rama `week-26`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Algo que se pulsa sin nombre para TalkBack, o un texto de accesibilidad escrito en Kotlin y no en los recursos | -15 |
| `@Stable`, `@Immutable` o el archivo de estabilidad sobre una clase con `var` o con algo que cambia por dentro | -10 |
| `core` o `data` dependen de Android o de Compose (por ejemplo, para poner `@Immutable` en `Observation`) | -10 |
| Un test de la semana 25 o del starter borrado o debilitado para que pase | -20 |
| Una dependencia nueva (por ejemplo, `ui-test-junit4-accessibility`, que en Robolectric no comprueba nada), o un sha256 añadido a mano | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-26` → `main` en el repositorio del aprendiz.
