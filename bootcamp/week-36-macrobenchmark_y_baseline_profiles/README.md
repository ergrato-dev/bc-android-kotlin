# Semana 36 — Macrobenchmark y Baseline Profiles

> Las semanas 34 y 35 probaron que la Bitácora hace lo que debe. Esta mide cuánto tarda: el arranque en frío hasta el primer fotograma y hasta la lista, y los fotogramas al recorrerla. Macrobenchmark la maneja desde fuera, en un dispositivo, y deja todas las medidas en un JSON. Con lo medido llega el **Baseline Profile**: una lista de clases y métodos que viaja en el APK para que ART los compile antes del primer arranque. Se genera en un emulador que Gradle gestiona y va al repositorio. Al final, los números del antes y el después, y lo que se puede decir con ellos (en un emulador, menos de lo que parece).

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Medir** el arranque (TTID y TTFD) y los fotogramas, y **leer** una serie con su mediana, su CV y sus percentiles
- **Montar** un módulo `com.android.test` con Macrobenchmark y UiAutomator, y **ejecutarlo** sobre `benchmarkRelease`
- **Explicar** cómo compila ART y **escribir** reglas de un perfil a mano
- **Generar** un Baseline Profile y un Startup Profile en un Gradle Managed Device y **llevarlos** al APK
- **Avisar** de que la app está lista con `ReportDrawnWhen`, y **comprobarlo** donde se puede
- **Comparar** sin perfil, con perfil y todo compilado, y **decidir** si una diferencia es una mejora o ruido

---

## 📋 Prerrequisitos

- [Semana 35](../week-35-tests_de_ui/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los Gradle Managed Devices y `testTagsAsResourceId` de la semana 35.
- **Para generar y medir**: KVM (en WSL2, la virtualización anidada encendida y tu usuario en el grupo `kvm`: `ls -l /dev/kvm` y `id`) o un móvil con la depuración USB y API 33 o más. El ejercicio 01 y los tests de la semana no lo necesitan.

---

## 🗂️ Estructura de la Semana

```
week-36-macrobenchmark_y_baseline_profiles/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 6 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: leer un benchmark, y un perfil con su benchmark
├── 3-proyecto/                  # la capa 35 de tu Bitácora: medida
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Medir el rendimiento](1-teoria/01-medir-el-rendimiento.md) | Frío, tibio y caliente; TTID y TTFD; jank; mediana, CV y percentiles; release y dispositivo; Microbenchmark | 30 min |
| [02 · Macrobenchmark](1-teoria/02-macrobenchmark.md) | El módulo `com.android.test`; `benchmarkRelease`; `measureRepeated`; UiAutomator por `resource-id`; ejecutar y lo que deja | 40 min |
| [03 · ART y los perfiles](1-teoria/03-art-y-los-perfiles.md) | Intérprete, JIT y AOT; tres fuentes de perfil; el formato de las reglas; del archivo al APK; `profileinstaller` | 35 min |
| [04 · Generar el Baseline Profile](1-teoria/04-generar-el-perfil.md) | `BaselineProfileRule`; el plugin a los dos lados; un GMD; dónde queda; cuándo regenerarlo | 35 min |
| [05 · Startup Profiles y `reportFullyDrawn`](1-teoria/05-startup-profiles-y-reportfullydrawn.md) | El Startup Profile y `dexLayoutOptimization`; `ReportDrawnWhen`; dónde se comprueba | 30 min |
| [06 · Medir antes y después](1-teoria/06-medir-antes-y-despues.md) | `None`, `Partial` y `Full`; lo que dio el emulador; mejora o ruido; qué hace falta para decidir; CI | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Leer un benchmark](2-practicas/ejercicio-01-leer_un_benchmark/README.md) | Mediana, CV y percentiles comprobados contra el JSON real de Macrobenchmark, y una regla para decidir si hay mejora | 1 h 30 min |
| [02 · Un perfil y su benchmark](2-practicas/ejercicio-02-un_perfil_y_su_benchmark/README.md) | Reglas escritas a mano que llegan al APK, un módulo `:benchmark` con arranque y generador, y la variante que se mide | 2 h |

### Proyecto

La Bitácora medida: el módulo `:baselineprofile` con el arranque (tres compilaciones) y la lista, el generador en un GMD, el perfil en el repositorio, `ReportDrawnWhen`, y tus números con su `context`.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (6 archivos) | 3 h 30 min |
| Ejercicio 01 | 1 h 30 min |
| Ejercicio 02 | 2 h |
| Proyecto | 5 h |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| ¿Hay KVM? | `ls -l /dev/kvm` y `$ANDROID_HOME/emulator/emulator -accel-check` |
| Un emulador sin ventana | `$ANDROID_HOME/emulator/emulator -avd <nombre> -no-window -no-audio -no-snapshot` |
| Medir en un emulador | `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR` |
| Una sola clase de benchmarks | `-Pandroid.testInstrumentationRunnerArguments.class=dev.bitacora.baselineprofile.ScrollBenchmark` |
| Ver qué ve UiAutomator | `adb shell uiautomator dump /sdcard/u.xml && adb shell cat /sdcard/u.xml` |
| El perfil que llega al APK, en texto | `app/build/intermediates/merged_art_profile/fieldRelease/mergeFieldReleaseArtProfile/baseline-prof.txt` |
| El tamaño del perfil en el APK | `unzip -l app-field-release-unsigned.apk \| grep dexopt` |
| Las medianas de un JSON | `python3 -c "import json; [print(b['name'], {k: v['median'] for k, v in b['metrics'].items()}) for b in json.load(open('...benchmarkData.json'))['benchmarks']]"` |
| Ver una traza | Abre el `.perfetto-trace` en [ui.perfetto.dev](https://ui.perfetto.dev) |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 11 de 11 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 7 de 7 en verde, `./gradlew build` en verde, y `NOTAS.md` con el paso 8
3. ✅ Proyecto: rama `week-36` con `:baselineprofile`, los benchmarks, el generador, `ReportDrawnWhen` y el perfil generado; los tests de la semana 35 y `PerformanceTest` en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-36` → `main` con la salida de `./gradlew test`, el diff del perfil resumido y tus números con su `context`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`. Generar el perfil y medir necesitan un dispositivo; los tests, no.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 35](../week-35-tests_de_ui/README.md) | **Semana 36** | [Semana 37](../week-37-r8_y_memoria/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
