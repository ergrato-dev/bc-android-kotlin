# Semana 11 — Convention plugins y build-logic

> La semana pasada dejaste la misma configuración copiada en tres módulos, a propósito. Esta semana la mueves a **plugins tuyos**, en un build aparte llamado **`build-logic`**, y cada módulo pasa a decir en una línea qué es: una biblioteca o un programa.

Sigue sin haber Android. Lo que montas es la carpeta donde, en la fase 3, vivirán los convention plugins de Android (`bitacora.android-application`), igual que en los proyectos Android de referencia.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** qué es un convention plugin y por qué es mejor que `subprojects { }`
- **Montar** un build incluido `build-logic` con `kotlin-dsl` y conectarlo desde `settings.gradle.kts`
- **Escribir** precompiled script plugins y componerlos: una base y un plugin por tipo de módulo
- **Usar** el mismo catálogo de versiones en el build principal y en `build-logic`
- **Explicar** por qué el raíz sigue declarando el plugin de Kotlin con `apply false`
- **Probar** el efecto de un convention plugin con TestKit

---

## 📋 Prerrequisitos

- [Semana 10](../week-10-multimodulo_y_version_catalogs/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 10 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-11-convention_plugins_y_build_logic/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit)
├── 3-proyecto/                  # la capa 10 de tu Bitácora: build-logic
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · De la configuración repetida a un plugin](1-teoria/01-de-la-configuracion-repetida-a-un-plugin.md) | Plugins, `buildSrc` frente a `build-logic`, `includeBuild` | 45 min |
| [02 · Precompiled script plugins](1-teoria/02-precompiled-script-plugins.md) | `kotlin-dsl`, id por nombre de archivo, el plugin de Kotlin como biblioteca | 45 min |
| [03 · El catálogo dentro de build-logic](1-teoria/03-el-catalogo-dentro-de-build-logic.md) | Un catálogo para dos builds, `versionCatalogs`, `findLibrary` | 45 min |
| [04 · Componer convention plugins](1-teoria/04-componer-convention-plugins.md) | Base y tipos de módulo, qué va dónde, la línea del raíz | 45 min |
| [05 · Diseñar y probar build-logic](1-teoria/05-disenar-y-probar-build-logic.md) | TestKit, lista de comprobación, errores frecuentes | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Primer convention plugin](2-practicas/ejercicio-01-primer-convention-plugin/README.md) | `build-logic`, `bitacora.kotlin-base`, los tres módulos sin repetir nada | 2 h |
| [02 · Catálogo y composición](2-practicas/ejercicio-02-catalogo-y-composicion/README.md) | `bitacora.kotlin-library` y `bitacora.kotlin-application` con el catálogo | 2 h |

### Proyecto

La Bitácora con `build-logic`: tres convention plugins, módulos de una línea más lo suyo, y tests que comprueban el efecto de los plugins.
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
| Compilar solo los convention plugins | `./gradlew -p build-logic build` |
| Ver qué plugins aporta build-logic | `ls build-logic/src/main/kotlin`: un archivo, un id |
| Ver por qué una tarea no está `UP-TO-DATE` | `./gradlew :core:compileKotlin --info` y busca `is not up-to-date because` |
| Ver los repositorios de los que salen los plugins | `./gradlew -p build-logic build --refresh-dependencies --info` y busca `Cached resource` |
| Probar un build sin tocar el tuyo | `./gradlew -p sandbox <tarea>` (en los ejercicios) |
| Ejecutar un solo test | `./gradlew test --tests "BuildTest.changingTheConventionDoesNotRecompileTheModules"` |
| Ver la versión de un `.class` | `xxd -s 6 -l 2 core/build/classes/kotlin/main/Level.class` (`0041` = Java 21) |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 4
3. ✅ Proyecto: rama `week-11` con `build-logic`, tres convention plugins y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-11` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo los de la red de seguridad: en cada ejercicio, cuatro de los siete ya pasan porque el build de partida funciona, y tienen que seguir pasando mientras mueves la configuración. El primer test de cada ejecución tarda más: compila `build-logic`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 10](../week-10-multimodulo_y_version_catalogs/README.md) | **Semana 11** | [Semana 12](../week-12-plugins_propios_y_extensiones/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
