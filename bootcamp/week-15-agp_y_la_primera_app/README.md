# Semana 15 — AGP y la primera app

> Empieza la fase 3. Seis semanas de Gradle desembocan aquí: el plugin de Android es un plugin más, con su versión en el catálogo, su verificación y su sitio en `build-logic`. Esta semana instalas el SDK, construyes tu primera app desde la terminal, la abres por dentro con `apkanalyzer` y conviertes la Bitácora en una aplicación Android sin tocar el Kotlin puro de `core` y `data`.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Instalar** el SDK de Android sin Android Studio y explicar `compileSdk`, `minSdk` y `targetSdk`
- **Configurar** un módulo de aplicación: `namespace`, `applicationId`, versión y manifiesto
- **Inspeccionar** un APK con `apkanalyzer`
- **Explicar** cómo compila Kotlin AGP 9 y cómo se elige su versión
- **Añadir** AGP a un build con `build-logic`, catálogo y verificación
- **Conectar** una tarea propia a las variantes con la Variant API

---

## 📋 Prerrequisitos

- [Semana 14](../week-14-dependencias_verificacion_y_rendimiento/README.md) completada: la fase 2 cerrada.
- El SDK de Android y `ANDROID_HOME`, según [docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android).
- Opcional: un móvil Android con la depuración USB activada, para instalar la app. El emulador necesita virtualización por hardware (KVM en Linux); si tu máquina no la tiene, todo lo evaluable funciona sin él.

---

## 🗂️ Estructura de la Semana

```
week-15-agp_y_la_primera_app/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit + apkanalyzer)
├── 3-proyecto/                  # la capa 14 de tu Bitácora: la app Android
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · El SDK de Android y AGP](1-teoria/01-el-sdk-de-android-y-agp.md) | Qué pone cada uno, `ANDROID_HOME`, `sdkmanager`, `compileSdk`/`minSdk`/`targetSdk` | 40 min |
| [02 · Anatomía de una app](1-teoria/02-anatomia-de-una-app.md) | `namespace`, `applicationId`, manifiesto, actividad, el APK por dentro | 45 min |
| [03 · Kotlin en AGP 9](1-teoria/03-kotlin-en-agp-9.md) | Kotlin integrado, la versión de KGP, módulos de Kotlin puro | 35 min |
| [04 · AGP en el build de la Bitácora](1-teoria/04-agp-en-el-build-de-la-bitacora.md) | Repositorios, `apply false`, convention plugin, verificación y `aapt2` | 50 min |
| [05 · Variantes y la Variant API](1-teoria/05-variantes-y-la-variant-api.md) | Tipos de build, `androidComponents`, `addGeneratedSourceDirectory` | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La primera app con AGP](2-practicas/ejercicio-01-primera-app-con-agp/README.md) | `android { }`, manifiesto y actividad de arranque; el APK, inspeccionado | 2 h |
| [02 · Kotlin puro y la Variant API](2-practicas/ejercicio-02-kotlin-puro-y-variant-api/README.md) | Un módulo `core` sin Android y `build-info.properties` en los assets de cada variante | 2 h |

### Proyecto

La Bitácora, app Android: AGP en el catálogo y en `build-logic`, el convention plugin `bitacora.android-application`, `bitacora.build-info` con la Variant API, la verificación regenerada y tests que inspeccionan el APK.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Preparar el SDK ([setup](../../docs/setup.md#fase-3-el-sdk-de-android)) | 30 min |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver los paquetes del SDK instalados | `sdkmanager --list_installed` |
| Construir el APK de debug | `./gradlew :app:assembleDebug` |
| Leer el manifiesto final de un APK | `apkanalyzer manifest print app/build/outputs/apk/debug/app-debug.apk` |
| Ver las clases que hay en el dex | `apkanalyzer dex packages app/build/outputs/apk/debug/app-debug.apk` |
| Saber qué Kotlin compila tu app | `./gradlew buildEnvironment \| grep kotlin-gradle-plugin` |
| Instalar en un móvil conectado | `./gradlew :app:installDebug` o `adb install -r <apk>` |
| Regenerar la verificación con AGP | `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-15` con la app Android, la verificación regenerada, mediciones en el README y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-15` → `main` con la salida de `./gradlew test`, las mediciones y los componentes de la verificación antes y después
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo `coreTestsRunOnTheJvm` en el ejercicio 02, que tiene que seguir en verde. Los tests construyen APK de verdad y los inspeccionan con `apkanalyzer`: la primera ejecución descarga AGP y sus dependencias de `google()`; después, todo sale de `~/.gradle`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 14](../week-14-dependencias_verificacion_y_rendimiento/README.md) | **Semana 15** | Semana 16 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
