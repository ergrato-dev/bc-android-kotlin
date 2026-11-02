# Rúbrica de Evaluación — Semana 22: Layouts y listas

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la lista de observaciones |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué baja y qué sube cuando Compose mide un layout? ¿Cuántas veces se mide cada hijo, y qué no se puede hacer por eso? | 10 |
| 2 | Tres botones con `weight(1f)` y `Arrangement.spacedBy(12.dp)` en una fila de 320 dp con 16 dp de margen a cada lado. ¿Dónde empieza y cuánto mide cada uno? | 10 |
| 3 | En una `Row`, la insignia mide 40 dp y la descripción lleva `weight(1f)`. ¿Por qué la descripción y no la insignia? ¿Qué pasa con una descripción larga si se quita el `weight`? | 10 |
| 4 | `Box(Modifier.padding(16.dp).clickable {}.size(40.dp))` y `Box(Modifier.clickable {}.padding(16.dp).size(40.dp))`: ¿cuánto mide la zona que se pulsa en cada uno? ¿Cuál quieres en la fila de una lista, y por qué? | 10 |
| 5 | Da las tres reglas del parámetro `modifier` de un composable. ¿Qué pasa si `LevelBadge` escribe `Modifier.size(40.dp).then(modifier)` y quien la llama le pasa `padding(8.dp)`? | 10 |
| 6 | Un `Checkbox` se ve de 24 dp. ¿Cuánto ocupa en la fila, y por qué? ¿Qué tienes que añadir a un botón propio hecho con `clickable`? | 10 |
| 7 | Una pantalla de ajustes con 12 opciones fijas y la lista de observaciones de una sesión de campo: ¿`Column` o `LazyColumn` para cada una? ¿Qué diferencia mide un test entre las dos con 200 elementos? | 10 |
| 8 | `Column(Modifier.verticalScroll(...)) { LazyColumn { ... } }` compila. ¿Qué pasa al ejecutarlo, y por qué? Da dos formas de arreglarlo. | 10 |
| 9 | La lista empieza con `b` y `c`, el usuario abre `b`, y llega `a` delante. ¿Qué fila se ve abierta sin clave, y cuál con `key = { it }`? ¿Por qué vale el código de la observación como clave y no su posición ni la observación entera? | 10 |
| 10 | `if (state.firstVisibleItemIndex > 0) UpButton()`: ¿qué dice lint, y qué cuesta? ¿Cómo se arregla? ¿Por qué `animateScrollToItem` necesita `rememberCoroutineScope`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Bajan las restricciones (ancho y alto mínimos y máximos), suben los tamaños que elige cada hijo; con ellos el padre decide su tamaño y coloca. Cada hijo se mide una sola vez por pasada: por eso anidar layouts no multiplica el coste, y por eso no se puede meter una lista que se desplaza dentro de otra en la misma dirección (pregunta 8).
2. Quedan 320 - 16 - 16 = 288 dp; menos dos huecos de 12, 264 para tres botones: 88 cada uno. Empiezan en 16, 116 (16 + 88 + 12) y 216.
3. La insignia mide lo que mide; la descripción se queda con lo que sobra. El hijo con peso se mide el último, con lo que dejan los demás. Sin `weight`, cada hijo se mide en orden con lo que queda: una descripción larga se lleva todo el ancho y la casilla de detrás se queda con 0 dp en el borde (304 dp en el ejercicio 01); con una corta, la casilla queda pegada al texto (113 dp en vez de 268).
4. El primero: 40 × 40, los de dentro del margen (de 16 a 56). El segundo: 72 × 72 (16 + 40 + 16), el margen también responde. En la fila de una lista, el segundo orden: el usuario espera que la fila entera responda, también el borde. Por eso la fila es `fillMaxWidth().clickable(...).padding(16.dp)`.
5. Se llama `modifier`, vale `Modifier` por defecto y es el primer parámetro opcional; se aplica una vez, al elemento de fuera y primero en la cadena. Con `then(modifier)` al final, el `size(40.dp)` va primero y el margen de 8 queda dentro del cuadrado: el contenido mide 40 - 8 - 8 = 24 dp (`Actual width is 24.0.dp, expected 40.0.dp`).
6. 48 dp: los componentes de Material llevan `Modifier.minimumInteractiveComponentSize()`, porque un dedo no acierta en menos (recomendación de accesibilidad de Android). En el ejercicio 01 la casilla ocupa de 256 a 304 y se ve de 268 a 292. A un botón propio hay que añadírselo.
7. Ajustes: `Column` (con `verticalScroll` si no caben). Observaciones: `LazyColumn`, porque crece con los datos. Con 200 textos, la `Column` tiene los 200 nodos en el árbol de semántica; el `LazyColumn`, 8, y sigue teniendo 8 después de bajar a la 150.
8. Al medir, no al compilar: `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints, which is disallowed...`. Dentro de una `Column` con `verticalScroll` el alto máximo es infinito, y el `LazyColumn` necesita un alto máximo para saber cuántos elementos caben. Arreglos: lo de encima de la lista como `item { }` dentro del `LazyColumn`, o fuera de la lista, en una `Column` sin desplazamiento, con la lista en `Modifier.weight(1f)`.
9. Sin clave, `a` (`a: true, b: false, c: false`): el estado se queda en la posición. Con clave, `b`. El código es único, estable (no cambia aunque el usuario edite la descripción) y es un `String`, que cabe en un `Bundle`. La posición cambia cada vez que llega una delante. La observación entera es una `data class` que no es `Parcelable`: `IllegalArgumentException: Type of the key Observation(...) is not supported. On Android you can only use types which can be stored inside the Bundle.`
10. `Warning: Reading a value annotated with @FrequentlyChangingValue inside composition [FrequentlyChangingValue from androidx.compose.runtime]`. Recompone con cada fila que sale por arriba: bajando de la 1 a la 20, 20 recomposiciones en vez de 2. Se arregla con `val scrolled by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }`. `animateScrollToItem` es `suspend` y un `onClick` no es una corrutina; `rememberCoroutineScope` da un ámbito que se cancela cuando el composable sale de la composición. Lanzarla durante la composición la repetiría en cada recomposición.

**Ejercicio 01, paso 5**: (a) `theWholeRowIsTouchable`: `Actual left is 16.0.dp, expected 0.0.dp`. Los 16 dp de cada borde de la fila no responden al dedo. (b) `theCheckboxIsAtTheEnd` (`expected:<268.0.dp> but was:<113.0.dp>`) y `aLongDescriptionDoesNotPushTheCheckboxOut` (`... but was:<304.0.dp>`). Con la descripción corta, la casilla queda pegada al texto: 72 + 29 caracteres + 12 = 113. Con la larga, el texto se mide antes que la casilla y se lleva todo el ancho; la casilla se queda con 0 dp en el borde. (c) `theCallersModifierGoesOutside`: `Actual width is 24.0.dp, expected 40.0.dp`. El `size(40.dp)` va primero y el margen de 8 del test queda dentro: 40 - 8 - 8. (d) `Warning: Modifier parameter should be named modifier [ModifierParameter from androidx.compose.ui]`. Es un aviso: `lintDebug` termina con `BUILD SUCCESSFUL`. (e) `theTextsStartAfterTheBadge`: `Actual left is 56.0.dp, expected 72.0.dp`. 16 de margen + 40 de la insignia = 56.

**Ejercicio 02, paso 6**: (a) Fallan 5. `scrollingToAKeyComposesIt`, `theHeaderStaysOnTop`, `theScrollSurvives` y `anOpenRowSurvives`, con `IllegalArgumentException: Failed to scroll to the item identified by "A-300", couldn't find the key.`: son del test, que necesita la clave para llegar a la fila. `anOpenRowFollowsItsObservation` (no encuentra `A-2 · -34.6, -58.3`) es el que vería el usuario: al llegar la A-1 delante, la abierta pasa a ser la A-1. (b) `anOpenRowSurvives`: tras la muerte del proceso, la fila vuelve cerrada. Sin girar: al bajar hasta que la fila sale de la pantalla y volver, también se cierra. El `LazyColumn` suelta la composición de las filas que salen, y con ella lo que guardaba `remember`; lo de `rememberSaveable` lo guarda por la clave. (c) Ningún test falla. Lint: `Warning: Reading a value annotated with @FrequentlyChangingValue inside composition [FrequentlyChangingValue from androidx.compose.runtime]`. Recompone la lista con cada fila que sale por arriba, en vez de solo cuando cambia de `false` a `true`. (d) Los 8 tests: `IllegalArgumentException: Type of the key Observation(code=A-1, ...) is not supported. On Android you can only use types which can be stored inside the Bundle.` Compose guarda el estado de las filas por su clave en el `Bundle`, y una `data class` sin `@Parcelize` no cabe. (e) `theHeaderStaysOnTop`: tras bajar a la A-300, `Alta · 500` ya no existe. Con `item`, la cabecera es una fila más y se va con las de arriba. (f) Con la `Column`, 500 nodos con "observación"; con el `LazyColumn`, 6.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La fila de una observación (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| El `modifier` primero en la cadena; `clickable` antes del margen; `weight` en el texto; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Una lista larga (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `LazyColumn` con clave y `contentType`; `rememberSaveable` en la fila; `derivedStateOf` dentro de `remember`; la corrutina con `rememberCoroutineScope` (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La lista de observaciones

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 21 y los nuevos del starter en verde, y mínimo 6 nuevos, esperados escritos a mano con la cuenta: dónde empieza la línea, el alto de la fila, "Ubicar" y "Compartir" en su sitio, los niveles en una fila, las filas sin papel de botón | 25 |
| Layout | Lo fijo fuera de la lista; la lista en un `LazyColumn` con `weight(1f)` y el código como clave; los niveles repartidos con `weight`; la fila pulsable entera, con el margen dentro de la zona que se pulsa; `LevelBadge` con el `modifier` primero | 25 |
| Estado | La pantalla sigue sin estado de la app; la posición de la lista en el `LazyListState`, no en el `ViewModel` | 10 |
| Adaptación al dominio | Las letras de la insignia en el idioma del dominio y en el segundo idioma; README con lo que pide el paso 8 | 20 |
| Entrega | Rama `week-22`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| La lista en una `Column` con `verticalScroll`, o sin clave | -10 |
| "Compartir" o el campo dentro de la lista | -10 |
| La posición de la lista en el `SavedStateHandle` | -5 |
| Un test de la semana 21 o del starter borrado o debilitado para que pase | -20 |
| `core` o `data` dependen de Android o de Compose | -10 |
| Una dependencia nueva que no hacía falta, o un sha256 añadido a mano | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-22` → `main` en el repositorio del aprendiz.
