# Semana 16 — Actividades y ciclo de vida

> Una actividad no decide cuándo empieza ni cuándo acaba: la crea Android, la tapa, la para, la destruye al girar el móvil y, a veces, mata su proceso sin avisar. Esta semana mides ese ciclo con tests, decides dónde vive cada dato para que el usuario no pierda nada, y montas Robolectric en el build para probarlo todo en la JVM, sin emulador.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Predecir** el orden de los métodos del ciclo de vida al arrancar, irse al fondo, girar y cerrar
- **Explicar** qué hace Android en un cambio de configuración y por qué `configChanges` no es la solución
- **Elegir** entre un campo, un `ViewModel` y un `SavedStateHandle` según lo que tiene que aguantar cada dato
- **Observar** el ciclo de vida con `DefaultLifecycleObserver` y `repeatOnLifecycle`
- **Probar** actividades en la JVM con Robolectric: giros y muerte del proceso
- **Configurar** Robolectric en `build-logic` con `android-all` resuelto y verificado por Gradle

---

## 📋 Prerrequisitos

- [Semana 15](../week-15-agp_y_la_primera_app/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Unos 300 MB libres para `android-all` en la caché de Gradle.

---

## 🗂️ Estructura de la Semana

```
week-16-actividades_y_ciclo_de_vida/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (Robolectric)
├── 3-proyecto/                  # la capa 15 de tu Bitácora: la pantalla que no pierde nada
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · La actividad y su ciclo de vida](1-teoria/01-la-actividad-y-su-ciclo-de-vida.md) | Estados, métodos, el orden medido, qué hacer en cada uno | 40 min |
| [02 · Cambios de configuración](1-teoria/02-cambios-de-configuracion.md) | La recreación, qué se pierde, por qué no `configChanges` | 35 min |
| [03 · Guardar el estado](1-teoria/03-guardar-el-estado.md) | Muerte del proceso, `ViewModel`, `SavedStateHandle`, límites del `Bundle` | 50 min |
| [04 · Observar el ciclo de vida](1-teoria/04-observar-el-ciclo-de-vida.md) | `DefaultLifecycleObserver`, orden, `lifecycleScope`, `repeatOnLifecycle` | 35 min |
| [05 · Probar el ciclo de vida en la JVM](1-teoria/05-probar-el-ciclo-de-vida-en-la-jvm.md) | Robolectric, `ActivityScenario`, `--add-opens`, `android-all` en Gradle | 50 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El ciclo de vida, medido](2-practicas/ejercicio-01-el-ciclo-de-vida-medido/README.md) | Métodos y observador que apuntan cada paso; tests de arranque, fondo, giro y cierre | 2 h |
| [02 · Estado que sobrevive](2-practicas/ejercicio-02-estado-que-sobrevive/README.md) | Cada dato en su sitio: giro y muerte del proceso | 2 h |

### Proyecto

La Bitácora que no pierde nada: la primera pantalla con borrador y observaciones de la sesión en el `SavedStateHandle`, Robolectric en el convention plugin y `android-all` dentro de la verificación.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ejecutar solo los tests de la app | `./gradlew :app:testDebugUnitTest` |
| Un test concreto | `./gradlew :app:testDebugUnitTest --tests '*StateTest*'` |
| Ver lo que imprime un test | `--info`, o `testLogging { showStandardStreams = true }` |
| Girar el móvil en un test | `RuntimeEnvironment.setQualifiers("+land")` |
| Simular la muerte del proceso | `buildActivity(...).setup()`, `saveInstanceState(bundle)` y otra con `setup(bundle)` |
| Matar la app en un móvil de verdad | Ábrela, pulsa Inicio y `adb shell am kill dev.bitacora.agro`; vuelve desde Recientes |
| Ver el ciclo de vida en un móvil | `Log.d` en cada método y `adb logcat -s Bitacora` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-16` con la pantalla, Robolectric en el convention plugin, la verificación regenerada y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-16` → `main` con la salida de `./gradlew test` y los componentes de la verificación antes y después
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo `startsEmpty` y `theCacheIsRecalculatedAfterProcessDeath` en el ejercicio 02, que tienen que seguir en verde. La primera ejecución descarga `android-all` (unos 200 MB) a la caché de Gradle; después, todo sale de ahí.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 15](../week-15-agp_y_la_primera_app/README.md) | **Semana 16** | [Semana 17](../week-17-recursos_y_configuraciones/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
