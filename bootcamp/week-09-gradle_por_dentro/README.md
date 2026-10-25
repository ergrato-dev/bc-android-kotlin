# Semana 09 — Gradle por dentro

> Empieza la fase 2. Durante ocho semanas copiaste `build.gradle.kts` sin leerlo; ahora **es** el objeto de estudio. Esta semana aprende qué hace Gradle y por qué: las fases de un build, las tareas y sus relaciones, por qué una tarea no se repite, y qué guardan las dos cachés.

Todavía hay un solo proyecto y un solo módulo. El `core` de tu Bitácora sigue siendo el de la semana 8; lo que cambia es **cómo se construye**.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** las tres fases de un build y decir en cuál se ejecuta cada línea de un `build.gradle.kts`
- **Definir** tareas con `register`, con grupo y descripción, y relacionarlas con `dependsOn`, `finalizedBy` y dependencias implícitas
- **Escribir** una clase de tarea propia con entradas y salidas declaradas
- **Diagnosticar** por qué una tarea se ejecuta de más o de menos (`UP-TO-DATE`, `FROM-CACHE`)
- **Hacer** que una tarea sea compatible con la caché de resultados y con la caché de configuración
- **Probar** un build con TestKit

---

## 📋 Prerrequisitos

- [Semana 08](../week-08-serialization_y_dsls/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 8 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-09-gradle_por_dentro/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit)
├── 3-proyecto/                  # la capa 8 de tu Bitácora: su build
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Un build de Gradle por dentro](1-teoria/01-un-build-de-gradle-por-dentro.md) | Piezas, las tres fases, `register` | 45 min |
| [02 · Tareas y dependencias](1-teoria/02-tareas-y-dependencias.md) | `Copy`, `Zip`, `dependsOn`, `finalizedBy`, `providers` | 45 min |
| [03 · Entradas, salidas y up-to-date](1-teoria/03-entradas-salidas-y-up-to-date.md) | Tareas propias, anotaciones, `UP-TO-DATE` | 45 min |
| [04 · Cachés de Gradle](1-teoria/04-caches-de-gradle.md) | Caché de resultados, de configuración, archivos reproducibles | 45 min |
| [05 · Diseñar y probar un build](1-teoria/05-disenar-y-probar-un-build.md) | TestKit, lista de comprobación, depuración | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Tareas y dependencias](2-practicas/ejercicio-01-tasks-y-dependencias/README.md) | `hello`, `stage`, `bundle`, `announce`, `release`, `reset` | 2 h |
| [02 · Tareas con entradas y salidas](2-practicas/ejercicio-02-tareas-con-entradas-y-salidas/README.md) | Una clase de tarea propia, `skipHeader`, `@CacheableTask` | 2 h |

### Proyecto

La Bitácora tiene un **build con identidad**: versión y dominio en `gradle.properties`, una tarea propia que genera `build-info.properties`, un `dist` reproducible y compatible con las dos cachés.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h |
| Recursos y glosario | 1 h 30 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver el orden de las tareas sin ejecutarlas | `./gradlew <tarea> --dry-run` |
| Ver qué hace una tarea | `./gradlew help --task <tarea>` |
| Ver todas las tareas con grupo | `./gradlew tasks` |
| Forzar una tarea aunque esté al día | `./gradlew <tarea> --rerun` |
| Saber por qué se ejecutó | `./gradlew <tarea> --info` |
| Ejecutar un build de ejemplo dentro de `starter/` | `./gradlew -p sandbox <tarea>` |
| Ejecutar un solo test | `./gradlew test --tests "BuildTest.theZipIsReproducible"` |
| Probar la caché de configuración en un build | `./gradlew <tarea> --configuration-cache` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-09` con `gradle.properties`, `GenerateBuildInfo`, `dist`, `printVersion` y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-09` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo: es lo esperado. Los tests arrancan Gradle de verdad, así que tardan unos segundos cada uno; la primera vez descargan el plugin de Kotlin.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 08](../week-08-serialization_y_dsls/README.md) | **Semana 09** | Semana 10 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
