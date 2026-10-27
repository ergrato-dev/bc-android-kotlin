# Semana 12 — Plugins propios: clases, extensiones y pruebas

> Los convention plugins aplican decisiones fijas. Esta semana escribes un plugin **de verdad**, como una clase: con una extensión que el usuario configura, una tarea propia, dos niveles de pruebas y una versión publicada que otro build puede usar.

La tarea `generateBuildInfo` de la semana 9 se convierte en el plugin `bitacora.build-info`. En la fase 3, la Bitácora Android lo seguirá usando para saber qué versión y qué dominio está ejecutando.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Escribir** un plugin binario (`Plugin<Project>`) y registrarlo con un id en `gradlePlugin { }`
- **Crear** una extensión con `Property<T>` y valores por defecto perezosos (`convention`, `provider`)
- **Conectar** la extensión con una tarea propia y reaccionar a otros plugins con `withPlugin`
- **Probar** un plugin en dos niveles: `ProjectBuilder` y TestKit con `withPluginClasspath()`
- **Publicar** un plugin en un repositorio Maven y consumirlo con `id(...) version "..."`
- **Explicar** qué es el marcador de un plugin y por qué una versión publicada no cambia

---

## 📋 Prerrequisitos

- [Semana 11](../week-11-convention_plugins_y_build_logic/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 11 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-12-plugins_propios_y_extensiones/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 11 de tu Bitácora: su plugin
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · De script a clase](1-teoria/01-de-script-a-clase.md) | `Plugin<Project>`, `gradlePlugin { }`, clase o script | 45 min |
| [02 · Extensiones y propiedades perezosas](1-teoria/02-extensiones-y-propiedades-perezosas.md) | `extensions.create`, `convention`, `provider`, el orden del build | 45 min |
| [03 · Tareas del plugin y otros plugins](1-teoria/03-tareas-del-plugin-y-otros-plugins.md) | `register`, `withPlugin`, caché de configuración | 45 min |
| [04 · Probar un plugin](1-teoria/04-probar-un-plugin.md) | `ProjectBuilder`, `withPluginClasspath`, `validatePlugins` | 45 min |
| [05 · Publicar y consumir un plugin](1-teoria/05-publicar-y-consumir-un-plugin.md) | `maven-publish`, el marcador, `pluginManagement` | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Un plugin con extensión y tarea](2-practicas/ejercicio-01-plugin-con-extension-y-tarea/README.md) | `bitacora.build-info`: extensión, tarea, recursos, tests de dos niveles | 2 h |
| [02 · Publicar y consumir](2-practicas/ejercicio-02-publicar-y-consumir/README.md) | Publicarlo en `build/repo` y usarlo desde otro build | 2 h |

### Proyecto

La Bitácora con su plugin: `bitacora.build-info` en `build-logic`, aplicado por `bitacora.kotlin-application`, con tests unitarios propios y `app` que dice su dominio y su versión.
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
| Ver las tareas de un plugin | `./gradlew tasks --group bitacora` |
| Ver la descripción de una tarea | `./gradlew help --task generateBuildInfo` |
| Comprobar las anotaciones de las tareas | `./gradlew validatePlugins` |
| Ejecutar solo los tests unitarios | `./gradlew test --tests "PluginTest.theVersionDefaultsToTheProjectVersion"` |
| Ver qué publicaría `publish` | `./gradlew publish --dry-run` |
| Ver lo publicado | `find build/repo -name '*.pom' -o -name '*.jar'` |
| Ver qué clase implementa un id | `unzip -p <jar> META-INF/gradle-plugins/<id>.properties` |
| Ejecutar los tests del plugin de `build-logic` | `./gradlew -p build-logic test` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew build` con 7 de 7 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 4
3. ✅ Proyecto: rama `week-12` con `bitacora.build-info` en `build-logic`, al menos 2 tests unitarios nuevos y 6 del build, calculados a mano, todos en verde
4. ✅ Pull request `week-12` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo; en el ejercicio 02, el de `validatePlugins` ya pasa: el plugin del ejercicio 01 viene resuelto. Los tests unitarios tardan milisegundos; los funcionales arrancan Gradle y tardan segundos cada uno.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 11](../week-11-convention_plugins_y_build_logic/README.md) | **Semana 12** | Semana 13 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
