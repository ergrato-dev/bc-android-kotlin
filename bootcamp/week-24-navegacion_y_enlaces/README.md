# Semana 24 — Navegación y enlaces

> Hasta la semana 23, la Bitácora tenía dos actividades: la sesión, en Compose, y la observación, en Views, que se abría con un `Intent` y devolvía el nivel con `setResult`. Esta semana es una sola actividad con dos destinos de Navigation 3: el back stack es una lista de claves que es tuya, `NavDisplay` pinta la última, atrás la quita, y todo sobrevive a la muerte del proceso porque las claves son serializables y llevan un código, no la observación. El resultado de un destino se guarda en el `ViewModel` en vez de viajar. Y un enlace `bitacora://observation/A-2` abre la observación desde fuera, con la sesión debajo, después de comprobar que existe.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** el modelo de Navigation 3: claves, back stack, `NavDisplay` y `entryProvider`
- **Escribir** claves serializables que sobreviven a la muerte del proceso, y decidir qué llevan
- **Responder** a atrás: volver, cerrar en la primera pantalla y devolver un resultado sin `setResult`
- **Abrir** un destino desde un enlace, con su `intent-filter`, `UriDeepLinkMatcher` y la sesión debajo
- **Tratar** un enlace como entrada que no es de fiar
- **Dar** a cada destino su estado con los decoradores, y saber cuándo se borra

---

## 📋 Prerrequisitos

- [Semana 23](../week-23-material_3_y_tema/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los `Intent` y el Activity Result de la [semana 18](../week-18-intents_y_activity_result/README.md) y la muerte del proceso de la [semana 16](../week-16-actividades_y_ciclo_de_vida/README.md).

---

## 🗂️ Estructura de la Semana

```
week-24-navegacion_y_enlaces/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el back stack, y los enlaces
├── 3-proyecto/                  # la capa 23 de tu Bitácora: una actividad y sus destinos
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · De actividades a destinos](1-teoria/01-de-actividades-a-destinos.md) | Una actividad con destinos, el back stack como lista, `NavDisplay` y lo que entra en el build | 40 min |
| [02 · Claves y muerte del proceso](1-teoria/02-claves-y-muerte-del-proceso.md) | `rememberNavBackStack`, claves `@Serializable`, un código y no la observación, y el primer fotograma | 45 min |
| [03 · Atrás](1-teoria/03-atras.md) | `onBack`, la última clave, un resultado sin `setResult` y atrás en un test | 40 min |
| [04 · Enlaces](1-teoria/04-enlaces.md) | El `intent-filter`, `UriDeepLinkMatcher` y `withBackStack`, y lo que viene de fuera | 50 min |
| [05 · El estado de cada destino](1-teoria/05-el-estado-de-cada-destino.md) | Los decoradores: `rememberSaveable` y `ViewModel` por entrada, y cuándo se borran | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El back stack](2-practicas/ejercicio-01-el-back-stack/README.md) | Dos destinos en una actividad: abrir, apilar, elegir y volver, con la nota de cada uno y la muerte del proceso | 2 h |
| [02 · Los enlaces](2-practicas/ejercicio-02-los-enlaces/README.md) | `bitacora://observation/{código}` en el manifiesto y en el back stack, con la sesión debajo y los malos a la sesión | 2 h |

### Proyecto

La Bitácora en una sola actividad: la observación pasa a Compose como destino, con su foto, el nivel se guarda en el `ViewModel`, el back stack sobrevive a la muerte del proceso y un enlace abre una observación que existe.
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
| Matar el proceso en un test | `controller.pause().stop().saveInstanceState(bundle).destroy()` y `Robolectric.buildActivity(...).setup(bundle)` |
| Atrás en un test, tras un clic | `rule.waitForIdle()` y `activity.onBackPressedDispatcher.onBackPressed()` |
| Abrir la actividad con un enlace en un test | `Robolectric.buildActivity(MainActivity::class.java, Intent(ACTION_VIEW, Uri.parse(...)))` |
| ¿Quién abre este enlace? | `packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)` |
| Abrir un enlace en el emulador | `adb shell am start -W -a android.intent.action.VIEW -d "bitacora://observation/A-1"` |
| Ver qué hay en pantalla | `rule.onRoot().printToLog("arbol")` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-24` con una sola actividad, la observación en Compose, el back stack que sobrevive a la muerte del proceso, el enlace comprobado, los tests de la semana 23 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-24` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo, salvo los que esperan la sesión, que ya está. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 23](../week-23-material_3_y_tema/README.md) | **Semana 24** | Semana 25 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
