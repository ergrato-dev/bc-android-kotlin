# Semana 13 — Caché de build local y remota

> `UP-TO-DATE` evita repetir trabajo mientras las salidas siguen en `build/`. Esta semana activas la **caché de build**: un `clean` deja de obligar a recompilar, otra copia del repositorio reutiliza lo que ya compilaste y, con una caché remota, tu máquina descarga lo que construyó el CI.

Para que eso funcione, las tareas tienen que declarar bien sus entradas. Esta semana aparece un fallo que tu Bitácora ya arrastraba, y lo arreglas.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Distinguir** `UP-TO-DATE`, `FROM-CACHE` y `SUCCESS`, y activar la caché de build
- **Explicar** de qué se calcula la clave de caché de una tarea
- **Hacer** cacheable y reubicable una tarea propia (`@CacheableTask`, `@PathSensitive`)
- **Configurar** una caché remota HTTP con push solo desde el CI y credenciales fuera del repositorio
- **Declarar** las entradas que Gradle no puede ver (`inputs.files`)
- **Diagnosticar** un fallo de caché comparando claves (`-Dorg.gradle.caching.debug=true`)

---

## 📋 Prerrequisitos

- [Semana 12](../week-12-plugins_propios_y_extensiones/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 12 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-13-cache_de_build_local_y_remota/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests (TestKit)
├── 3-proyecto/                  # la capa 12 de tu Bitácora: la caché
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Qué es la caché de build](1-teoria/01-que-es-la-cache-de-build.md) | Tres resultados, `org.gradle.caching`, la caché local | 40 min |
| [02 · Tareas cacheables](1-teoria/02-tareas-cacheables.md) | La clave, `@CacheableTask`, `@PathSensitive` | 45 min |
| [03 · Reubicable y reproducible](1-teoria/03-reubicable-y-reproducible.md) | Rutas absolutas, salidas con la hora, entradas que Gradle no ve | 45 min |
| [04 · La caché remota](1-teoria/04-la-cache-remota.md) | `HttpBuildCache`, push desde el CI, credenciales, fallos | 45 min |
| [05 · Diagnosticar la caché](1-teoria/05-diagnosticar-la-cache.md) | Ver y comparar claves, opciones, lista de comprobación | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Tareas cacheables y reubicables](2-practicas/ejercicio-01-tareas-cacheables-y-reubicables/README.md) | `countObservations` `FROM-CACHE` tras `clean` y desde otra carpeta | 2 h |
| [02 · Caché remota con push desde el CI](2-practicas/ejercicio-02-cache-remota-push-desde-ci/README.md) | `HttpBuildCache` contra un servidor de prueba: el CI sube, otra máquina baja | 2 h |

### Proyecto

La Bitácora con caché de build: activada en el repositorio, remota con push solo desde el CI, `:test` con sus entradas declaradas y tests que comprueban qué sale de la caché.
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
| Activar la caché en una ejecución | `./gradlew build --build-cache` |
| Ver qué salió de la caché | `./gradlew clean build --console=plain \| grep FROM-CACHE` |
| Ver la clave de una tarea | `./gradlew <tarea> --info \| grep "cache key"` |
| Ver de qué se calcula la clave | `./gradlew <tarea> --rerun-tasks -Dorg.gradle.caching.debug=true` |
| Construir sin caché | `./gradlew build --no-build-cache` |
| Ver por qué una tarea no está `UP-TO-DATE` | `./gradlew <tarea> --info` y busca `is not up-to-date because` |
| Usar la caché remota del equipo | `bitacora.cacheUrl=https://...` en `~/.gradle/gradle.properties` |
| Ejecutar un solo test | `./gradlew test --tests "RemoteTest.anotherMachineReusesWhatTheCiBuilt"` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde y `NOTAS.md` con el paso 5
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-13` con la caché local y remota, `:test` con sus entradas y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-13` → `main` con la salida de `./gradlew test` y la de `./gradlew clean build` dos veces seguidas
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo los de la red de seguridad: dos en el ejercicio 01 (el build funciona) y uno en el 02 (sin URL no hay caché remota). Cada test arranca uno o varios builds en carpetas temporales, con su propia caché: los del proyecto tardan un par de minutos en total.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 12](../week-12-plugins_propios_y_extensiones/README.md) | **Semana 13** | Semana 14 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
