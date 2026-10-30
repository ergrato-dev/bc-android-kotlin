# Semana 18 — Intents y Activity Result

> Una app de verdad no es una pantalla: es un conjunto de puertas que se abren unas a otras, y también a otras apps. La Bitácora tiene que abrir una observación, compartir el resumen por correo, enseñar un punto en el mapa o pedirle una foto a la galería. Android lo hace con Intents: mensajes que dicen qué quieres hacer. Esta semana aprendes a mandarlos, a recibirlos y a recoger lo que devuelven, aunque Android mate tu app por el camino.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Abrir** otra actividad con un Intent explícito y pasarle datos que lleguen enteros
- **Pedir** a otras apps que compartan, abran un mapa o elijan un archivo, con Intents implícitos y el selector
- **Declarar** qué Intents acepta tu app con `intent-filter`, `exported` y deep links, y validar lo que llega
- **Recibir** un resultado con `registerForActivityResult`, con contratos de AndroidX y propios
- **Probar** en la JVM lo que sale, lo que vuelve y lo que pasa si el proceso muere entre medias

---

## 📋 Prerrequisitos

- [Semana 17](../week-17-recursos_y_configuraciones/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).

---

## 🗂️ Estructura de la Semana

```
week-18-intents_y_activity_result/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con Robolectric
├── 3-proyecto/                  # la capa 17 de tu Bitácora: abre y comparte
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Intents explícitos](1-teoria/01-intents-explicitos.md) | `startActivity`, extras, `@Parcelize`, `IntentCompat`, el límite de Binder | 40 min |
| [02 · Intents implícitos](1-teoria/02-intents-implicitos.md) | Acción, datos y tipo, el selector, `resolveActivity`, `<queries>` | 40 min |
| [03 · Intent filters y deep links](1-teoria/03-intent-filters-y-deep-links.md) | Las tres pruebas, `DEFAULT`, `exported`, `bitacora://`, App Links | 40 min |
| [04 · Activity Result](1-teoria/04-activity-result.md) | Lanzadores, contratos, `setResult`, el registro y la muerte del proceso | 50 min |
| [05 · Probar Intents en la JVM](1-teoria/05-probar-intents-en-la-jvm.md) | `nextStartedActivity`, `receiveResult`, `launchActivityForResult`, `ShadowPackageManager` | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Intents de ida](2-practicas/ejercicio-01-intents-de-ida/README.md) | Una nota `Parcelable` a otra pantalla, compartir, un mapa, `<queries>` y un deep link | 2 h |
| [02 · Resultados que vuelven](2-practicas/ejercicio-02-resultados-que-vuelven/README.md) | Un contrato propio, `setResult`, `GetContent` y el resultado tras la muerte del proceso | 2 h |

### Proyecto

La Bitácora abre y comparte: cada observación se edita en su propia pantalla y el cambio vuelve con Activity Result, aunque el proceso muera; el resumen se comparte con el selector.
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
| Ver el último Intent lanzado en un test | `shadowOf(activity).nextStartedActivity` |
| Devolver un resultado en un test | `shadowOf(activity).receiveResult(request.intent, RESULT_OK, data)` |
| Que un Intent sin destino falle como en un móvil | `shadowOf(app).checkActivities(true)` |
| Ver qué actividades exporta el APK | `apkanalyzer manifest print app-debug.apk \| grep -A2 "<activity"` |
| Abrir un deep link en un móvil conectado | `adb shell am start -a android.intent.action.VIEW -d "bitacora://observation/A-7"` |
| Ver el Intent con el que se abrió una actividad | `adb shell dumpsys activity activities` (busca `intent={...}`) |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 7
3. ✅ Proyecto: rama `week-18` con la pantalla de la observación, el contrato, la lista, compartir y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-18` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo los de la semana 17 en el proyecto, que empiezan en verde.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 17](../week-17-recursos_y_configuraciones/README.md) | **Semana 18** | Semana 19 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
