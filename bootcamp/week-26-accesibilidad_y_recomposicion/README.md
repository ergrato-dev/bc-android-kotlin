# Semana 26 — Accesibilidad y recomposición

> Hasta la semana 25, la Bitácora se veía bien, pero TalkBack leía cada fila empezando por la letra de la insignia, el título no era un encabezado y nadie avisaba de que el resumen había cambiado. Y con cada tecla del borrador, cada fila de la lista se volvía a componer sin haber cambiado. Esta semana mira dos cosas que no se ven: lo que recibe el lector de pantalla y lo que se recompone. Las dos se miden en la JVM, con el árbol de semántica, los informes del compilador de Compose y un contador.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Leer** el árbol de semántica unido y saber qué recibe TalkBack de cada nodo
- **Dar** a cada elemento su nombre, su papel y su estado: encabezados, interruptores, casillas, seleccionados y avisos
- **Probar** la accesibilidad en la JVM: etiquetas, áreas de toque y letra grande, y saber qué no comprueba Robolectric
- **Leer** los informes del compilador de Compose y arreglar un parámetro inestable
- **Contar** recomposiciones y quitar las que sobran, sin optimizar a ciegas

---

## 📋 Prerrequisitos

- [Semana 25](../week-25-animaciones_y_gestos/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- La acción "Borrar" para TalkBack de la [semana 25](../week-25-animaciones_y_gestos/1-teoria/04-deslizar-para-borrar.md) y el tamaño de letra de la [semana 23](../week-23-material_3_y_tema/README.md).

---

## 🗂️ Estructura de la Semana

```
week-26-accesibilidad_y_recomposicion/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la ficha que lee TalkBack, y la lista que no recompone
├── 3-proyecto/                  # la capa 25 de tu Bitácora: que se lea bien y recomponga poco
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Lo que lee TalkBack](1-teoria/01-lo-que-lee-talkback.md) | El árbol de semántica, unido y sin unir, lo que llega al lector de pantalla y cómo quitar lo que sobra | 40 min |
| [02 · Nombre, papel y estado](1-teoria/02-nombre-papel-y-estado.md) | `contentDescription`, `Role`, `toggleable`, `selected`, `heading` y `liveRegion` | 40 min |
| [03 · Probar la accesibilidad](1-teoria/03-probar-la-accesibilidad.md) | Los matchers de semántica, el área de toque, la letra grande y lo que no hace `enableAccessibilityChecks` en Robolectric | 45 min |
| [04 · Estabilidad y saltarse una composición](1-teoria/04-estabilidad.md) | Qué se salta Compose, estable o inestable, el *strong skipping*, los informes del compilador y el archivo de estabilidad | 50 min |
| [05 · Recomponer menos](1-teoria/05-recomponer-menos.md) | Instancias nuevas, `derivedStateOf`, contar recomposiciones y no optimizar a ciegas | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La ficha que lee TalkBack](2-practicas/ejercicio-01-la-ficha-que-lee-talkback/README.md) | Encabezado, insignia callada, dos botones dibujados con nombre y sitio para el dedo, un interruptor de una sola parada y la letra al doble | 2 h |
| [02 · La lista que no recompone](2-practicas/ejercicio-02-la-lista-que-no-recompone/README.md) | Una lista de cien que se recompone con cada tecla y con cada fila que pasa: `@Immutable`, `derivedStateOf` y el informe del compilador | 2 h |

### Proyecto

La Bitácora se lee bien con TalkBack (sin la insignia, con encabezados, el resumen que avisa y el nivel seleccionado) y deja de recomponer las filas con cada tecla.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 35 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 55 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver lo que lee TalkBack de un nodo | `rule.onNodeWithText(...).fetchSemanticsNode().config` en el árbol unido (el de por defecto) |
| Ver todo el árbol | `rule.onRoot().printToLog("arbol")`, y con `useUnmergedTree = true` el de cada composable |
| Mirar un papel o un estado | `assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))`, `assert(isHeading())` |
| La letra al doble | `@Config(fontScale = 2f)` y `@GraphicsMode(GraphicsMode.Mode.NATIVE)` para que el texto se mida |
| Los informes del compilador | `composeCompiler { reportsDestination = ... }` y `./gradlew :app:compileDebugKotlin`; se leen `*-composables.txt` y `*-classes.txt` |
| En el móvil | TalkBack con el gesto de encabezados; el Layout Inspector de Android Studio cuenta las recomposiciones |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 8 de 8 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 4
3. ✅ Proyecto: rama `week-26` con la insignia callada, los encabezados, el resumen que avisa, el nivel seleccionado, el `ViewModel` que no saca instancias nuevas, el archivo de estabilidad, los tests de la semana 25 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-26` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 25](../week-25-animaciones_y_gestos/README.md) | **Semana 26** | [Semana 27](../week-27-pantallas_grandes_y_mapa/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
