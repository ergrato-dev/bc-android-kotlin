# Semana 21 — Compose desde cero y estado

> En la fase 3 la pantalla de la Bitácora era un árbol de vistas que había que mantener al día a mano: cambiaba un dato y había que acordarse de cambiar cada `TextView` que lo enseñaba. Con Compose la pantalla es una función de su estado. Esta semana entra Compose en el build, aprendes dónde vive cada estado (en la composición, en el `Bundle` o en el `ViewModel`) y qué sobrevive a un giro y a la muerte del proceso, y la pantalla principal de la Bitácora pasa a Compose sin cambiar lo que hace. Con ella empieza la fase 4.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Añadir** Compose a una app con AGP 9: el plugin del compilador, el BOM y las bibliotecas
- **Escribir** composables y entender la recomposición, `remember` y `rememberSaveable`
- **Subir** el estado: composables sin estado que pintan y avisan, y una sola fuente de verdad
- **Publicar** el estado de una pantalla desde el `ViewModel` como un `StateFlow` sacado del `SavedStateHandle`, y recogerlo con `collectAsStateWithLifecycle`
- **Probar** composables y actividades de Compose en la JVM, con Robolectric y la regla de Compose

---

## 📋 Prerrequisitos

- [Semana 20](../week-20-variantes_flavors_y_firma/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- `StateFlow`, `combine` y `stateIn` de la [semana 7](../week-07-flow_y_programacion_reactiva/README.md); el `ViewModel` y el `SavedStateHandle` de la [semana 16](../week-16-actividades_y_ciclo_de_vida/README.md).

---

## 🗂️ Estructura de la Semana

```
week-21-compose_desde_cero_y_estado/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: estado local, y estado en el ViewModel
├── 3-proyecto/                  # la capa 20 de tu Bitácora: la pantalla principal en Compose
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Compose en el build](1-teoria/01-compose-en-el-build.md) | El plugin del compilador, el BOM, lo que pesa, `setContent` y el primer composable | 40 min |
| [02 · Estado y recomposición](1-teoria/02-estado-y-recomposicion.md) | `mutableStateOf`, la recomposición, `remember` y el error de lint sin él | 40 min |
| [03 · rememberSaveable](1-teoria/03-remembersaveable.md) | Qué sobrevive a qué, el `Bundle`, los `Saver` y cómo probarlo | 40 min |
| [04 · El estado baja, los eventos suben](1-teoria/04-el-estado-baja-los-eventos-suben.md) | State hoisting, dos fuentes de verdad, las reglas de test de Compose y la semántica | 50 min |
| [05 · ViewModel y StateFlow](1-teoria/05-viewmodel-y-stateflow.md) | `getStateFlow`, `combine`, `stateIn`, `collectAsStateWithLifecycle` y las acciones que leen del handle | 50 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Un borrador que no se pierde](2-practicas/ejercicio-01-un-borrador-que-no-se-pierde/README.md) | Un selector sin estado y una pantalla con `rememberSaveable` que sobrevive al giro y a la muerte del proceso | 2 h |
| [02 · El estado en el ViewModel](2-practicas/ejercicio-02-el-estado-en-el-viewmodel/README.md) | La misma pantalla sin estado, con el borrador en el `SavedStateHandle` y publicado como `StateFlow` | 2 h |

### Proyecto

La pantalla principal de la Bitácora en Compose: `BitacoraScreen` sin estado, el `ViewModel` con un `StateFlow` sacado del `SavedStateHandle`, y la actividad solo con lo que es de Android. Los tests de la semana 20, reescritos para Compose, en verde.
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
| Ejecutar un solo test de Compose | `./gradlew :app:testDebugUnitTest --tests '*DraftTest.theDraftSurvivesARotation'` |
| Ver el árbol de semántica cuando un test no encuentra un nodo | `println(rule.onRoot().printToString())` y mira la salida del test (`build/reports/tests`) |
| Ver las reglas de lint de Compose que fallan | `./gradlew :app:lintDebug` y busca `from androidx.compose` |
| Qué trae el BOM | `./gradlew :app:dependencies --configuration debugRuntimeClasspath \| grep compose-bom -A20` |
| Si Compose está en el APK | `apkanalyzer dex packages --defined-only app-debug.apk \| grep Recomposer` |
| Lo que pesa Compose | `apkanalyzer apk file-size app-debug.apk` antes y después |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 9 de 9 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-21` con Compose en el build, `BitacoraScreen` sin estado, el `ViewModel` con su `StateFlow`, los tests de la semana 20 en verde y al menos 6 tests nuevos calculados a mano
4. ✅ Pull request `week-21` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo en el proyecto los de `ObservationActivity`, los que solo miran el título y los de TestKit de la semana 20, que empiezan en verde.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 20](../week-20-variantes_flavors_y_firma/README.md) | **Semana 21** | Semana 22 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
