# Semana 17 — Recursos y configuraciones

> La misma app tiene que verse bien en español y en inglés, en un móvil y en una tablet, de día y en modo oscuro, y con el icono recortado como quiera cada fabricante. Android lo resuelve con recursos: el código pide `R.string.title` y el sistema elige qué versión usar según la configuración del móvil. Esta semana aprendes cómo elige, pruebas esa elección con Robolectric y controlas desde el build qué recursos acaban en el APK.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Organizar** textos, plurales, colores y números en `res/` y usarlos desde Kotlin con `R`
- **Predecir** qué carpeta de recursos elige Android según idioma, región, tamaño, orientación y modo oscuro
- **Traducir** una app con formato, plurales y variantes regionales, y encontrar lo que falta con lint
- **Construir** un icono adaptativo con versión monocroma
- **Controlar** desde el build los idiomas del APK y la lista de idiomas por app
- **Explicar** cómo se fusionan los recursos de los source sets y las bibliotecas

---

## 📋 Prerrequisitos

- [Semana 16](../week-16-actividades_y_ciclo_de_vida/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).

---

## 🗂️ Estructura de la Semana

```
week-17-recursos_y_configuraciones/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: Robolectric, y TestKit con apkanalyzer
├── 3-proyecto/                  # la capa 16 de tu Bitácora: la app habla tu idioma
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Los recursos y la clase R](1-teoria/01-los-recursos-y-la-clase-r.md) | `res/`, aapt2, `R` no transitiva, el valor por defecto | 35 min |
| [02 · Calificadores: cómo elige Android](1-teoria/02-calificadores.md) | Los calificadores, su orden, el algoritmo, los tests con `@Config` | 45 min |
| [03 · Idiomas](1-teoria/03-idiomas.md) | Formato, plurales, regiones, lint, `generateLocaleConfig` | 50 min |
| [04 · El icono](1-teoria/04-el-icono.md) | Icono adaptativo, capas, `monochrome`, `mipmap-anydpi` | 30 min |
| [05 · Recursos en el build](1-teoria/05-recursos-en-el-build.md) | La fusión, `localeFilters`, lint y verificación en la Bitácora | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Cada recurso en su carpeta](2-practicas/ejercicio-01-cada-recurso-en-su-carpeta/README.md) | Idioma, región, plurales, modo oscuro, horizontal y tablet, probados con Robolectric | 2 h |
| [02 · Idiomas e icono en el APK](2-practicas/ejercicio-02-idiomas-e-icono-en-el-apk/README.md) | `localeFilters`, `generateLocaleConfig`, icono adaptativo, nombre de debug y lint, comprobados en el APK | 2 h |

### Proyecto

La Bitácora habla tu idioma: ningún texto en Kotlin, una traducción con plurales, el icono adaptativo, los idiomas declarados en el convention plugin y filtrados en el APK, y lint en los tests.
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
| Ver para qué configuraciones hay textos en el APK | `apkanalyzer resources configs --type string app-debug.apk` |
| Leer un texto del APK en un idioma | `apkanalyzer resources value --type string --name app_name --config es app-debug.apk` |
| Leer un XML compilado del APK | `apkanalyzer resources xml --file res/mipmap-anydpi-v21/ic_launcher.xml app-debug.apk` |
| Pasar lint | `./gradlew :app:lintDebug`; informe en `app/build/reports/lint-results-debug.html` |
| Un test en otro idioma | `@Config(qualifiers = "en")` en el test o en la clase |
| Girar o cambiar a mitad de test | `RuntimeEnvironment.setQualifiers("+land")` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 7
3. ✅ Proyecto: rama `week-17` con la pantalla traducida, el icono, los idiomas en el build y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-17` → `main` con la salida de `./gradlew test` y lo que ha cambiado en la verificación
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo `theDefaultIsEnglish` en el ejercicio 01 y los tests de Robolectric de la semana 16 en el proyecto, que empiezan en verde.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 16](../week-16-actividades_y_ciclo_de_vida/README.md) | **Semana 17** | [Semana 18](../week-18-intents_y_activity_result/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
