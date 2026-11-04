# Semana 25 — Animaciones y gestos

> Hasta la semana 24, la Bitácora saltaba de un estado a otro: la insignia cambiaba de color de golpe, y una observación equivocada no se podía quitar. Esta semana se mueve: un valor que se anima con su reloj, contenidos que aparecen y se van, filas que se apartan, y el gesto de deslizar para borrar con su deshacer. Con dos reglas que van con todo: cada gesto tiene su acción para TalkBack, y una animación se lee al dibujar para no recomponer en cada fotograma. Todo probado en la JVM, parando el reloj de Compose.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Animar** un valor con `animate*AsState`, eligiendo entre `tween` y `spring`
- **Probar** una animación a medias con `rule.mainClock`
- **Hacer** que algo aparezca, se vaya o se mueva con `AnimatedVisibility`, `AnimatedContent` y `animateItem`
- **Recoger** gestos con la capa más alta que sirva, y darles su acción de accesibilidad
- **Borrar** deslizando con `SwipeToDismissBox` y deshacer con un `Snackbar`, sin los fallos que trae
- **Leer** un valor animado en la fase de dibujo para no recomponer

---

## 📋 Prerrequisitos

- [Semana 24](../week-24-navegacion_y_enlaces/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Las listas con clave de la [semana 22](../week-22-layouts_y_listas/README.md) y los píxeles en los tests de la [semana 23](../week-23-material_3_y_tema/README.md).

---

## 🗂️ Estructura de la Semana

```
week-25-animaciones_y_gestos/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la tarjeta que se mueve, y deslizar para borrar
├── 3-proyecto/                  # la capa 24 de tu Bitácora: borrar, deshacer y moverse
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Animar un valor, y el reloj](1-teoria/01-animar-un-valor-y-el-reloj.md) | `animate*AsState`, `tween` o `spring`, el reloj de los fotogramas y `mainClock` en los tests | 45 min |
| [02 · Aparecer, irse y moverse](1-teoria/02-aparecer-irse-y-moverse.md) | `AnimatedVisibility`, `AnimatedContent`, `animateItem` y las transiciones de `NavDisplay` | 45 min |
| [03 · Gestos](1-teoria/03-gestos.md) | Las capas de gestos, `combinedClickable` con su etiqueta, `performTouchInput` y un gesto, una acción | 40 min |
| [04 · Deslizar para borrar](1-teoria/04-deslizar-para-borrar.md) | `SwipeToDismissBox`, lo que lee TalkBack, deshacer con un `Snackbar`, la fila que vuelve deslizada y los códigos | 50 min |
| [05 · Animar sin recomponer](1-teoria/05-animar-sin-recomponer.md) | Las tres fases, leer al dibujar con `drawBehind` y `graphicsLayer`, y qué no animar | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La tarjeta que se mueve](2-practicas/ejercicio-01-la-tarjeta-que-se-mueve/README.md) | Un toque abre con `AnimatedVisibility`, una pulsación larga cambia el nivel con su etiqueta, y la insignia anima su color al dibujar | 2 h |
| [02 · Deslizar para borrar](2-practicas/ejercicio-02-deslizar-para-borrar/README.md) | Borrar hacia la izquierda y con TalkBack, deshacer durante 10 s, y las de debajo que suben | 2 h |

### Proyecto

La Bitácora borra una observación deslizando o con TalkBack, la deshace desde el `ViewModel`, mueve las filas, anima la insignia sin recomponer y no repite códigos.
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
| Ver una animación a medias | `rule.mainClock.autoAdvance = false` y `rule.mainClock.advanceTimeBy(150)` |
| Que la animación empiece sin avanzar el reloj | `rule.waitForIdle()` con el reloj parado |
| Deslizar o mantener pulsado en un test | `performTouchInput { swipeLeft() }`, `performTouchInput { longClick() }` |
| Lo que haría TalkBack | `fetchSemanticsNode().config[SemanticsActions.CustomActions]` y `action()` en `runOnIdle` |
| Ver las acciones de un nodo | `rule.onRoot(useUnmergedTree = true).printToLog("arbol")` |
| Ver las animaciones | El inspector de animaciones de Android Studio, con el `label` de cada una |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 8 de 8 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-25` con borrar y deshacer, la acción de TalkBack, las filas que se mueven, la insignia animada, los códigos que no se repiten, los tests de la semana 24 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-25` → `main` con la salida de `./gradlew test`
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
| [Semana 24](../week-24-navegacion_y_enlaces/README.md) | **Semana 25** | [Semana 26](../week-26-accesibilidad_y_recomposicion/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
