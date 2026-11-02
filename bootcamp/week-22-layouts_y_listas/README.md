# Semana 22 — Layouts y listas

> En la semana 21 la pantalla de la Bitácora pasó a Compose, pero seguía siendo una `Column` con todo dentro, un elemento debajo de otro: con una sesión de campo larga, "Compartir" se quedaba fuera de la pantalla. Esta semana aprendes cómo mide Compose y cómo se reparte el espacio con `Row`, `Column`, `Box` y los modifiers, y cómo se enseña una lista de cientos de elementos sin componerlos todos: `LazyColumn`, las claves que llevan el estado de cada fila, y el estado de la propia lista. La pantalla principal pasa a tener una parte fija y una lista de observaciones que se desplaza.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** cómo mide Compose: las restricciones bajan, los tamaños suben, y cada hijo se mide una vez
- **Repartir** el espacio con `Row`, `Column` y `Box`, `Arrangement`, `Alignment` y `weight`
- **Encadenar** modifiers en el orden correcto y escribir composables que acepten un `modifier`
- **Enseñar** listas largas con `LazyColumn`, `items`, `stickyHeader` y claves estables
- **Guardar** el estado de la lista y de cada fila, y leerlo sin recomponer de más con `derivedStateOf`
- **Probar** en la JVM dónde queda cada cosa, en dp, y una lista que no compone lo que no se ve

---

## 📋 Prerrequisitos

- [Semana 21](../week-21-compose_desde_cero_y_estado/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- `remember`, `rememberSaveable` y las reglas de test de Compose de la semana 21.

---

## 🗂️ Estructura de la Semana

```
week-22-layouts_y_listas/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la fila de una observación, y una lista larga
├── 3-proyecto/                  # la capa 21 de tu Bitácora: la lista de observaciones
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Column, Row y Box](1-teoria/01-column-row-y-box.md) | Cómo mide Compose, `Arrangement`, `Alignment`, `weight` y cómo comprobar posiciones en dp | 45 min |
| [02 · Modifiers](1-teoria/02-modifiers.md) | El orden de la cadena, los modifiers de tamaño, el tamaño mínimo de toque y el parámetro `modifier` | 45 min |
| [03 · Listas perezosas](1-teoria/03-listas-perezosas.md) | `Column` o `LazyColumn`, `item`, `items`, `stickyHeader`, dos listas en la misma dirección y cómo probarlas | 45 min |
| [04 · Claves y estado de los elementos](1-teoria/04-claves-y-estado-de-los-items.md) | Qué pasa sin clave, qué vale como clave, `contentType` y `key` fuera de una lista | 40 min |
| [05 · El estado de la lista](1-teoria/05-el-estado-de-la-lista.md) | `rememberLazyListState`, `derivedStateOf`, `rememberCoroutineScope` y dónde va cada estado | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La fila de una observación](2-practicas/ejercicio-01-la-fila-de-una-observacion/README.md) | Una fila que ocupa todo el ancho, se pulsa entera y no saca la casilla de la pantalla, comprobada en dp | 2 h |
| [02 · Una lista larga](2-practicas/ejercicio-02-una-lista-larga/README.md) | 500 observaciones agrupadas por nivel, filas que se abren y siguen abiertas, y un botón "Arriba" | 2 h |

### Proyecto

La pantalla principal de la Bitácora con una parte fija arriba y abajo y, en medio, la lista de observaciones en un `LazyColumn` con clave, que solo compone lo que se ve y conserva su posición tras la muerte del proceso. Cada fila con la insignia del nivel y su ubicación.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 40 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 50 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver dónde ha quedado un nodo | `println(rule.onNodeWithText("Ubicar").getUnclippedBoundsInRoot())` y mira la salida del test |
| Ver el árbol de semántica con sus límites | `rule.onRoot(useUnmergedTree = true).printToLog("arbol")` |
| Contar lo que compone una lista | `rule.onAllNodesWithText("observación", substring = true).fetchSemanticsNodes().size` |
| Encontrar la lista en un test | `rule.onNode(hasScrollAction())` |
| Ejecutar un solo test | `./gradlew :app:testDebugUnitTest --tests '*ObservationListTest.theHeaderStaysOnTop'` |
| Ver los avisos de Compose de lint | `./gradlew :app:lintDebug` y busca `from androidx.compose` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-22` con la lista perezosa, lo fijo fuera de ella, la fila con su insignia, los tests de la semana 21 y los nuevos del starter en verde, y al menos 6 tests nuevos calculados a mano
4. ✅ Pull request `week-22` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests empiezan en rojo, salvo en el proyecto los de la semana 21 que no miran la lista, que empiezan en verde. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 21](../week-21-compose_desde_cero_y_estado/README.md) | **Semana 22** | Semana 23 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
