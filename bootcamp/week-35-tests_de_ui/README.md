# Semana 35 — Tests de UI

> La semana 34 ordenó los tests de la Bitácora. Esta se centra en los de la pantalla, más de 200 en la JVM con Robolectric. Repasa lo que ven (el árbol semántico), cómo se escriben para que se lean (un robot) y cómo esperan lo que tarda (el reloj de Compose y `waitUntil`). Añade lo que faltaba: Espresso-Intents para lo que sale de la app, capturas con Roborazzi que comparan cada píxel en cada `./gradlew test`, y los primeros tests instrumentados, con un emulador que Gradle gestiona solo. La primera captura encontró un fallo que ninguna aserción había visto.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Leer** el árbol semántico de una pantalla y **dar semántica** a un control dibujado a mano
- **Encontrar** cualquier nodo combinando matchers, y **escribir** un robot que esconda el «cómo»
- **Sincronizar** un test con el reloj de Compose, `waitUntil` y lo que llega de otros hilos
- **Probar** vistas con Espresso, y lo que sale de la app y lo que vuelve con Espresso-Intents
- **Montar** tests instrumentados con Hilt, el orquestador y un Gradle Managed Device
- **Comparar** capturas con Roborazzi en cada build, y **decidir** si una diferencia es un fallo o un cambio querido

---

## 📋 Prerrequisitos

- [Semana 34](../week-34-estrategia_de_tests/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los tests de Compose de las semanas 21 a 27, y Hilt en los tests de la semana 29.
- Para ejecutar los tests instrumentados (opcional): una máquina con KVM o un móvil con la depuración USB. WSL2 no tiene KVM por defecto: sin ella, se compilan pero no se ejecutan.

---

## 🗂️ Estructura de la Semana

```
week-35-tests_de_ui/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG y una captura
├── 1-teoria/                    # 6 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: semántica y reloj, y vistas con intents y capturas
├── 3-proyecto/                  # la capa 34 de tu Bitácora: los tests de UI
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · El árbol semántico](1-teoria/01-el-arbol-semantico.md) | Qué hay en un nodo; el árbol unido y sin unir; hijos que no son filas; texto, papel o `testTag`; un control sin semántica; `testTagsAsResourceId` | 30 min |
| [02 · Buscar, actuar, comprobar](1-teoria/02-buscar-actuar-comprobar.md) | Las tres reglas; matchers combinados; acciones semánticas y gestos; `assertExists` frente a `assertIsDisplayed`; el robot | 30 min |
| [03 · Sincronización](1-teoria/03-sincronizacion.md) | Qué espera `waitForIdle`; `waitUntil`; el reloj parado y el fotograma que lanza el efecto; lo que llega de otro hilo | 40 min |
| [04 · Espresso y Espresso-Intents](1-teoria/04-espresso-e-intents.md) | `onView`, `perform`, `check`; vistas y Compose juntos; `intended`; `intending`; la barra que tapa la pantalla | 35 min |
| [05 · Tests instrumentados y GMD](1-teoria/05-tests-instrumentados.md) | `src/androidTest`; el runner de Hilt; el orquestador; Gradle Managed Devices; que `check` los compile; qué va dónde | 35 min |
| [06 · Capturas con Roborazzi](1-teoria/06-capturas-con-roborazzi.md) | Grabar, verificar y comparar; `roborazzi.test.verify`; capturas deterministas; lo que encontró la primera; otras herramientas | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La semántica y el reloj](2-practicas/ejercicio-01-la-semantica_y_el_reloj/README.md) | Semántica para una valoración dibujada con `Canvas`, una nota que se guarda sola probada con el reloj parado, y lo que llega de otro hilo | 1 h 45 min |
| [02 · Vistas, intents y capturas](2-practicas/ejercicio-02-vistas_intents_y_capturas/README.md) | Un parte de campo con vistas: Espresso, el correo con `intended`, la cámara con `intending` y capturas con Roborazzi | 1 h 45 min |

### Proyecto

La Bitácora con tests de UI: el tag de la lista y `testTagsAsResourceId`, el robot, compartir con Espresso-Intents, capturas verificadas en cada build, el fallo que encuentra la primera, y `src/androidTest` con Hilt, orquestador y un GMD que compila `check`.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (6 archivos) | 3 h 30 min |
| Ejercicio 01 | 1 h 45 min |
| Ejercicio 02 | 1 h 45 min |
| Proyecto | 5 h |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver el árbol de un nodo | `println(rule.onNodeWithTag("observations").printToString())`, o `printToLog("ARBOL")` |
| Un solo test de la app | `./gradlew :app:testFieldDebugUnitTest --tests '*ScreenshotTest.theScreen'` (`test` no acepta `--tests` en Android) |
| Regrabar las capturas | `./gradlew :app:recordRoborazziFieldDebug`, y mirar el diff de los PNG antes de hacer commit |
| Ver la diferencia sin fallar | `./gradlew :app:compareRoborazziFieldDebug` y `app/build/outputs/roborazzi/*_compare.png` |
| Las tareas del emulador | `./gradlew :app:tasks --all \| grep pixel6Api36` |
| Las imágenes ATD que hay | `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list \| grep atd` |
| Qué compila `check` | `./gradlew :app:check -m \| grep AndroidTest` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 11 de 11 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 7 de 7 y tu captura en verde, las referencias en el repositorio, y `NOTAS.md` con el paso 7
3. ✅ Proyecto: rama `week-35` con los tags, el robot, `ShareTest`, las capturas, `src/androidTest` y el GMD; los tests de la semana 34 y `UiTest` en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-35` → `main` con la salida de `./gradlew test` y las capturas en el diff
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
| [Semana 34](../week-34-estrategia_de_tests/README.md) | **Semana 35** | [Semana 36](../week-36-macrobenchmark_y_baseline_profiles/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
