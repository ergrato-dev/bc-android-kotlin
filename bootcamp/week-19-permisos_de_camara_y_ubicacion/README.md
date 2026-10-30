# Semana 19 — Permisos de cámara y ubicación

> Una observación de campo sin lugar y sin foto vale la mitad. Pero la ubicación y la cámara son de lo más privado que tiene un móvil, y Android no las da sin preguntar: la app pide permiso cuando lo necesita, el usuario decide, y puede cambiar de opinión cuando quiera. Esta semana aprendes a pedir bien, a funcionar con un "no", a hacer fotos sin pedir la cámara, y a controlar qué permisos acaban en tu APK aunque los traiga una biblioteca.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Distinguir** los permisos normales, peligrosos y de firma, y comprobar cuáles tiene la app
- **Pedir** permisos con Activity Result y recorrer el flujo completo: explicar, pedir, denegado y denegado para siempre
- **Ubicar** con la precisión que conceda el usuario, sin Google Play Services
- **Hacer** fotos con la app de la cámara y `FileProvider`, sin el permiso de la cámara
- **Controlar** los permisos y las funciones de hardware del APK, también los que traen las bibliotecas

---

## 📋 Prerrequisitos

- [Semana 18](../week-18-intents_y_activity_result/README.md) completada.
- El SDK de Android y `ANDROID_HOME`, con `build-tools` 36.0.0 ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).

---

## 🗂️ Estructura de la Semana

```
week-19-permisos_de_camara_y_ubicacion/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: Robolectric, y TestKit con apkanalyzer y aapt2
├── 3-proyecto/                  # la capa 18 de tu Bitácora: sabe dónde y lo ve
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · El modelo de permisos](1-teoria/01-el-modelo-de-permisos.md) | Normales, peligrosos y de firma, `checkSelfPermission`, el APK | 30 min |
| [02 · Pedir un permiso](1-teoria/02-pedir-un-permiso.md) | Los contratos, el flujo, la explicación, los ajustes, `MissingPermission` | 50 min |
| [03 · Ubicación](1-teoria/03-ubicacion.md) | Aproximada y precisa, `LocationManagerCompat`, `ShadowLocationManager` | 40 min |
| [04 · Cámara y fotos](1-teoria/04-camara-y-fotos.md) | `TakePicture`, `FileProvider`, por qué no `CAMERA`, la muerte del proceso | 45 min |
| [05 · Permisos en el build](1-teoria/05-permisos-en-el-build.md) | La fusión, `tools:node="remove"`, funciones implícitas, `aapt2 dump badging` | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Pedir permiso](2-practicas/ejercicio-01-pedir-permiso/README.md) | El flujo completo del permiso de ubicación, probado con Robolectric | 2 h |
| [02 · Permisos en el APK](2-practicas/ejercicio-02-permisos-en-el-apk/README.md) | Quitar el permiso de un SDK, funciones no obligatorias, `FileProvider` y lint, comprobados en el APK | 2 h |

### Proyecto

La Bitácora sabe dónde y lo ve: cada observación puede llevar su ubicación, con el permiso pedido cuando hace falta, y una foto de la app de la cámara que sobrevive a la muerte del proceso.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 20 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 1 h 10 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver los permisos del APK | `apkanalyzer manifest permissions app-debug.apk` |
| Ver lo que ve Google Play (funciones implícitas) | `$ANDROID_HOME/build-tools/36.0.0/aapt2 dump badging app-debug.apk` |
| Saber quién trajo un permiso | `app/build/intermediates/manifest_merge_blame_file/debug/processDebugMainManifest/manifest-merger-blame-debug-report.txt` |
| Conceder un permiso en un test | `shadowOf(app).grantPermissions(ACCESS_FINE_LOCATION)` |
| Simular el GPS en un test | `shadowOf(locationManager).simulateLocation(location)` y `shadowOf(Looper.getMainLooper()).idle()` |
| Quitar un permiso en un móvil conectado | `adb shell pm revoke dev.bitacora.agro android.permission.ACCESS_FINE_LOCATION` |
| Mover el GPS del emulador | `adb emu geo fix -58.3816 -34.6037` (longitud primero) |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde, `lintDebug` en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 7
3. ✅ Proyecto: rama `week-19` con la ubicación, la foto, el `FileProvider`, los permisos justos y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-19` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo `lintStopsALocationCallWithoutACheck` en el ejercicio 02 y los tests de las semanas anteriores en el proyecto, que empiezan en verde.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 18](../week-18-intents_y_activity_result/README.md) | **Semana 19** | Semana 20 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
