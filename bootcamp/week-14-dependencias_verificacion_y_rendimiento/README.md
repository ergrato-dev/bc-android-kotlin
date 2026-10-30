# Semana 14 — Dependencias, verificación y rendimiento

> Tu build descarga y ejecuta código de otros: bibliotecas y plugins, con sus dependencias. Esta semana aprendes a saber qué versión de cada cosa acaba en el classpath y por qué, a decidir tú cuando no te gusta, a comprobar que lo que llega es lo que se publicó, y a medir el build antes de acelerarlo.

Es la última semana de la fase 2. Al terminarla, el build de la Bitácora está listo para recibir Android en la semana 15.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Leer** el grafo de dependencias con `dependencies` y explicar un conflicto con `dependencyInsight`
- **Subir** una dependencia transitiva con una restricción documentada (`constraints`, `because`)
- **Quitar** una transitiva de una configuración concreta con `exclude`
- **Verificar** con sha256 cada artefacto del build, plugins incluidos (`verification-metadata.xml`)
- **Medir** el build con `--profile` y activar la caché de configuración y la ejecución en paralelo
- **Revisar** el build completo de la fase 2 con una lista de comprobación

---

## 📋 Prerrequisitos

- [Semana 13](../week-13-cache_de_build_local_y_remota/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 13 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-14-dependencias_verificacion_y_rendimiento/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit)
├── 3-proyecto/                  # la capa 13 de tu Bitácora: verificada y rápida
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Cómo resuelve Gradle las dependencias](1-teoria/01-como-resuelve-gradle-las-dependencias.md) | Transitivas, conflictos, `dependencies`, `dependencyInsight` | 45 min |
| [02 · Restricciones y exclusiones](1-teoria/02-restricciones-y-exclusiones.md) | `constraints`, `because`, `exclude`, qué usar | 40 min |
| [03 · Verificar lo que descargas](1-teoria/03-verificar-lo-que-descargas.md) | Cadena de suministro, `verification-metadata.xml`, fallos, límites | 50 min |
| [04 · Medir y acelerar el build](1-teoria/04-medir-y-acelerar-el-build.md) | `--profile`, caché de configuración, paralelo, Build Scans | 45 min |
| [05 · El build de la Bitácora, completo](1-teoria/05-el-build-de-la-bitacora-completo.md) | Repaso de la fase 2, lista de comprobación, qué cambia con Android | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Conflictos y restricciones](2-practicas/ejercicio-01-conflictos-y-restricciones/README.md) | `annotations` a la 26.1.0 con una restricción, fuera del classpath de ejecución | 2 h |
| [02 · Verificar lo que descargas](2-practicas/ejercicio-02-verificar-lo-que-descargas/README.md) | `verification-metadata.xml` con sha256 y el ciclo de una dependencia nueva | 2 h |

### Proyecto

La Bitácora verificada y rápida: verificación de todo el build, `build-logic` incluido, caché de configuración y paralelo, mediciones de antes y después, y tests que lo comprueban.
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
| Ver el árbol de dependencias | `./gradlew :app:dependencies --configuration runtimeClasspath` |
| Saber por qué un módulo tiene su versión | `./gradlew :app:dependencyInsight --dependency kotlin-stdlib --configuration runtimeClasspath` |
| Generar o actualizar la verificación | `./gradlew --write-verification-metadata sha256 help` |
| Ver qué cambiaría en la verificación | `./gradlew --write-verification-metadata sha256 --dry-run help` |
| Ver los artefactos verificados | `grep 'artifact name' gradle/verification-metadata.xml` |
| Medir un build | `./gradlew <tareas> --profile` y abre `build/reports/profile` |
| Comprobar la caché de configuración y el paralelo | `./gradlew help --info \| grep "Operational build model"` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con los pasos 1 y 6
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-14` con la verificación, la caché de configuración y el paralelo, mediciones en el README y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-14` → `main` con la salida de `./gradlew test`, las mediciones y el diff de la verificación
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo los dos de la red de seguridad del ejercicio 01: el conflicto de `kotlin-stdlib` y el programa que funciona. Los tests usan las dependencias reales de Maven Central y del Plugin Portal. La primera ejecución descarga lo que no tengas en `~/.gradle`; después, todo sale de ahí.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 13](../week-13-cache_de_build_local_y_remota/README.md) | **Semana 14** | [Semana 15](../week-15-agp_y_la_primera_app/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
