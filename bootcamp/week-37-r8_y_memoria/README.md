# Semana 37 — R8 y memoria

> La Bitácora de la semana 36 se medía en release, pero esa release llevaba todo lo que traen sus bibliotecas: 13,2 MB de APK y 4 dex. Esta semana pasa por **R8**, que quita lo que nadie usa, renombra lo que queda y la deja en 2,25 MB, sin una sola regla propia. Se ve por qué no hace falta ninguna, cuándo sí, dónde va cuando la necesita una biblioteca, y cómo se lee una traza de la release con el mapping. Después, la memoria: cuánto ocupa la Bitácora en el móvil, **LeakCanary** buscando fugas también en los tests del dispositivo, y **StrictMode** diciendo lo que la app hace en el hilo principal y no debería.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Encender** R8 y la reducción de recursos en la release, y **explicar** qué hace cada uno
- **Escribir** la regla keep más pequeña que funciona, y **comprobarla** con `seeds.txt`, `usage.txt` y el mapping
- **Poner** las reglas de una biblioteca donde viajan con ella, y **leer** las que traen las de la Bitácora
- **Recuperar** una traza de la release con `retrace` y **guardar** el mapping de cada versión
- **Medir** un APK con `apkanalyzer` y la memoria con `dumpsys meminfo`
- **Detectar** fugas con LeakCanary, también en los tests del dispositivo, y **leer** lo que encuentra StrictMode

---

## 📋 Prerrequisitos

- [Semana 36](../week-36-macrobenchmark_y_baseline_profiles/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)), con `cmdline-tools` (`apkanalyzer`, `retrace`) y `build-tools;36.0.0`.
- **Para ver la app reducida funcionando, las fugas y la memoria**: KVM o un móvil, como en la semana 36. El ejercicio 01 y los tests de la semana no lo necesitan.

---

## 🗂️ Estructura de la Semana

```
week-37-r8_y_memoria/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 6 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: reglas keep, y una biblioteca con una fuga
├── 3-proyecto/                  # la capa 36 de tu Bitácora: reducida
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · R8](1-teoria/01-r8.md) | Quitar, optimizar, renombrar y convertir; encenderlo en release; el modo completo; lo que deja; los números de la Bitácora | 35 min |
| [02 · Reglas keep](1-teoria/02-reglas-keep.md) | Lo que R8 no ve; `-keep`, `-keepclassmembers`, `-keepnames` y sus modificadores; la más pequeña; comprobarlo; por qué la Bitácora no tiene | 40 min |
| [03 · Las reglas de las bibliotecas](1-teoria/03-reglas-de-las-bibliotecas.md) | AAR y JAR; `consumerProguardFiles`; `configuration.txt`; lo que traen serialization, Ktor, Room, Hilt y WorkManager | 30 min |
| [04 · El mapping y retrace](1-teoria/04-mapping-y-retrace.md) | Una traza de la release; el formato del mapping; `retrace`; el `pg_map_id`; guardarlo para Play | 30 min |
| [05 · APK Analyzer](1-teoria/05-apk-analyzer.md) | Tamaño de archivo y de descarga; los dex; comparar dos APK; los recursos y `tools:keep` | 30 min |
| [06 · La memoria](1-teoria/06-la-memoria.md) | `dumpsys meminfo`; las fugas; LeakCanary y `DetectLeaksAfterTestSuccess`; StrictMode; el Memory Profiler | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Reglas keep](2-practicas/ejercicio-01-reglas_keep/README.md) | R8 sobre un programa de la JVM con reflexión: las reglas que lo arreglan, y ni una más | 1 h 30 min |
| [02 · Una biblioteca y una fuga](2-practicas/ejercicio-02-una_biblioteca_y_una_fuga/README.md) | R8 en una app con una biblioteca que usa reflexión, su regla en el AAR, y LeakCanary encontrando una fuga | 2 h |

### Proyecto

La Bitácora reducida: R8 en el plugin de convención, lo que deja leído, una traza recuperada, LeakCanary en la debug y en `DeviceTest`, StrictMode, y tus números.
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
| Lo que deja R8 | `app/build/outputs/mapping/fieldRelease/`: `mapping.txt`, `seeds.txt`, `usage.txt`, `configuration.txt`, `configanalyzer.html` |
| De dónde sale cada regla | `grep "following section" configuration.txt` |
| ¿Qué nombre tiene ahora? | `grep "^dev.bitacora.SyncWorker -> " mapping.txt` |
| Recuperar una traza | `$ANDROID_HOME/cmdline-tools/latest/bin/retrace mapping.txt traza.txt` |
| La traza de un fallo | `adb logcat -d -b crash` |
| Tamaños | `apkanalyzer apk file-size app.apk` y `apkanalyzer apk download-size app.apk` |
| Quién ocupa en el dex | `apkanalyzer dex packages --defined-only --proguard-mappings mapping.txt app.apk` |
| Qué ha crecido | `apkanalyzer apk compare --different-only antes.apk despues.apk` |
| La memoria | `adb shell dumpsys meminfo dev.bitacora.agro` |
| Los avisos de StrictMode | `adb logcat --pid=$(adb shell pidof dev.bitacora.agro.debug) \| grep "StrictMode policy violation"` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 6 de 6 en verde, `./gradlew build` en verde, y `NOTAS.md` con el paso 7
3. ✅ Proyecto: rama `week-37` con R8 en la release, LeakCanary y StrictMode en debug, `DetectLeaksAfterTestSuccess` en `DeviceTest`; los tests de la semana 36 y `ShrinkTest` en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-37` → `main` con la salida de `./gradlew test` y tus números
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`. Ver la app reducida funcionando y las fugas necesita un dispositivo; los tests, no.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 36](../week-36-macrobenchmark_y_baseline_profiles/README.md) | **Semana 37** | [Semana 38](../week-38-seguridad/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
