# Semana 10 — Multi-módulo y version catalogs

> Tu Bitácora crece y ya no cabe en un solo proyecto. Esta semana la divides en **módulos** (`core`, `data` y `app`) con fronteras que Gradle hace cumplir, y dejas las versiones en **un solo archivo**. También aprendes a probar el propio build.

Sigue sin haber Android: todo es Kotlin y Gradle. La estructura que montas es la que usarás en la fase 3 (el módulo `app` pasará a ser una aplicación Android) y en la fase 5 (la modularización completa).

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Dividir** un proyecto en módulos y declararlos en `settings.gradle.kts`
- **Elegir** entre `api` e `implementation` y explicar la consecuencia
- **Explicar** por qué un cambio interno no recompila a los demás módulos
- **Escribir** un catálogo de versiones y usarlo desde los módulos y el raíz
- **Centralizar** los repositorios y hacer que Gradle rechace los sueltos
- **Probar** la estructura de un build con TestKit y los informes de dependencias

---

## 📋 Prerrequisitos

- [Semana 09](../week-09-gradle_por_dentro/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 9 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-10-multimodulo_y_version_catalogs/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit)
├── 3-proyecto/                  # la capa 9 de tu Bitácora: tres módulos
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Por qué módulos y cómo se declaran](1-teoria/01-por-que-modulos-y-como-se-declaran.md) | `include`, rutas, tareas por módulo | 45 min |
| [02 · Dependencias entre módulos](1-teoria/02-dependencias-entre-modulos.md) | `api`, `implementation`, recompilación, ciclos | 45 min |
| [03 · Version catalogs](1-teoria/03-version-catalogs.md) | `libs.versions.toml`, `alias`, `apply false` | 45 min |
| [04 · Configuración compartida](1-teoria/04-configuracion-compartida.md) | Repositorios centralizados, lo que sigue repetido | 45 min |
| [05 · Diseñar y probar un build de varios módulos](1-teoria/05-disenar-y-probar-un-build-multi-modulo.md) | TestKit, informes de dependencias, lista de comprobación | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Módulos y dependencias](2-practicas/ejercicio-01-modulos-y-dependencias/README.md) | Tres módulos, `api`/`implementation`, ciclos, recompilación | 2 h |
| [02 · Version catalog y repositorios](2-practicas/ejercicio-02-version-catalog-y-repositorios/README.md) | Migrar un build al catálogo y a repositorios centrales | 2 h |

### Proyecto

La Bitácora en tres módulos (`core`, `data`, `app`), con catálogo de versiones, repositorios centralizados y tests del propio build.
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
| Ver los módulos que reconoce Gradle | `./gradlew projects` |
| Ejecutar solo un módulo | `./gradlew :core:test` |
| Ver qué ejecutaría una tarea | `./gradlew :data:test --dry-run` |
| Ver qué dependencias tiene un módulo | `./gradlew :app:dependencies --configuration runtimeClasspath` |
| Ver por qué está una biblioteca | `./gradlew :app:dependencyInsight --dependency <nombre> --configuration runtimeClasspath` |
| Probar un build sin tocar el tuyo | `./gradlew -p sandbox <tarea>` (en los ejercicios) |
| Ejecutar un solo test | `./gradlew test --tests "BuildTest.aCycleBetweenModulesIsRejected"` |
| Ver qué hace subir una versión | Cambia una sola línea de `libs.versions.toml` y `./gradlew build` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 4
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-10` con tres módulos, catálogo, repositorios centrales y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-10` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo; en el ejercicio 02, tres tests ya pasan: son la red de seguridad de la migración. Los tests arrancan Gradle de verdad, así que tardan unos segundos cada uno.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 09](../week-09-gradle_por_dentro/README.md) | **Semana 10** | Semana 11 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
