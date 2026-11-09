# Semana 29 — Hilt y DataStore

> En la semana 28, la Bitácora recibió sus repositorios por el constructor, y `BitacoraApp` los creaba a mano. Esta semana lo hace Hilt: un módulo dice cómo se crea cada cosa y cuántas hay, el compilador comprueba el grafo, y la actividad y el ViewModel piden lo que necesitan. Y la lista tiene filtros que se quedan al cerrar la app, guardados en DataStore.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** qué automatiza Hilt de la DI a mano, qué genera Dagger y qué hace KSP en el build
- **Escribir** bindings con `@Inject constructor`, `@Binds` y `@Provides`, en módulos con `@InstallIn`
- **Elegir** el componente y el scope de cada binding, sabiendo qué quiere decir `@Singleton`
- **Guardar** preferencias en DataStore, con un solo DataStore por archivo, un `corruptionHandler` y una migración
- **Probar** con y sin Hilt: el constructor en la JVM, `@HiltAndroidTest` y `@TestInstallIn` con Robolectric

---

## 📋 Prerrequisitos

- [Semana 28](../week-28-arquitectura_udf_y_mvi/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- La DI a mano y los tests del ViewModel de la semana 28, y los `@Config` de Robolectric de la semana 17.

---

## 🗂️ Estructura de la Semana

```
week-29-hilt_y_datastore/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el grafo de la salida, y las preferencias que se quedan
├── 3-proyecto/                  # la capa 28 de tu Bitácora: Hilt y los filtros en DataStore
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Por qué Hilt](1-teoria/01-por-que-hilt.md) | Lo que repite la DI a mano; Dagger, Hilt y KSP; `@HiltAndroidApp`, `@AndroidEntryPoint` y `@HiltViewModel`; lo que cuesta | 40 min |
| [02 · Módulos y bindings](1-teoria/02-modulos-y-bindings.md) | `@Inject`, `@Provides` y `@Binds`; `@InstallIn`; `@BindsInstance` y `@ApplicationContext`; *qualifiers*; los errores del grafo | 45 min |
| [03 · Componentes y scopes](1-teoria/03-componentes-y-scopes.md) | Los componentes de Hilt y cuánto vive cada uno; qué es `@Singleton`; cuándo poner un scope y cuándo no | 40 min |
| [04 · DataStore](1-teoria/04-datastore.md) | Frente a `SharedPreferences`; Preferences y Proto; claves, `data` y `edit`; uno por archivo; archivos rotos y migraciones | 45 min |
| [05 · Probar con Hilt](1-teoria/05-probar-con-hilt.md) | Sin Hilt primero; `@HiltAndroidTest`, la regla y `HiltTestApplication`; `@TestInstallIn`; `"+es"`; DataStore en la JVM | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El grafo de la salida](2-practicas/ejercicio-01-el-grafo-de-la-salida/README.md) | Un grafo de Dagger sin Android: qué implementación recibe quien pide la interfaz, una sola por componente, y los errores al romperlo | 2 h |
| [02 · Las preferencias que se quedan](2-practicas/ejercicio-02-las-preferencias-que-se-quedan/README.md) | Las preferencias de una salida en DataStore, con un archivo roto y una migración desde un `.properties`, en la JVM | 2 h |

### Proyecto

La Bitácora con Hilt en lugar de la DI a mano, y dos filtros de la lista ("Primero las recientes" y "Solo altas") que se guardan en DataStore. La persona ve los filtros nuevos, y siguen puestos al abrir la app otra vez.
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
| Ver lo que genera Hilt | `app/build/generated/ksp/fieldDebug/java/`: busca `Hilt_MainActivity` y `BitacoraViewModel_HiltModules` |
| Encontrar el error del grafo | En la salida de `kspFieldDebugKotlin`, la primera línea con `[Dagger/` |
| Probar un ViewModel con Hilt sin Hilt | Llama al constructor: `@Inject` no cambia nada para quien lo usa a mano |
| No perder `robolectric.properties` en un test | `@Config(qualifiers = "+es")`, con el `+` |
| Ver el archivo de DataStore en un móvil | `adb shell run-as dev.bitacora.agro.debug ls files/datastore` (field, debug); es protobuf, no texto |
| Empezar sin filtros | `adb shell pm clear dev.bitacora.agro.debug`, o borrar `files/datastore/settings.preferences_pb` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 6 de 6 en verde y `NOTAS.md` con el paso 4
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-29` con Hilt (`@HiltAndroidApp`, `AppModule`, `@HiltViewModel`, `@AndroidEntryPoint`), los filtros en DataStore, los tests de la semana 28 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-29` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida para el proyecto:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 28](../week-28-arquitectura_udf_y_mvi/README.md) | **Semana 29** | Semana 30 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
