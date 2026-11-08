# Semana 28 — Arquitectura UDF y MVI

> Hasta la semana 27, el ViewModel de la Bitácora tenía una función por cada cosa que pasaba, el estado salía de juntar cuatro flujos, y las observaciones se perdían al cerrar la app. Esta semana se ordena: un solo estado que baja, intents que suben por una sola puerta, un reducer puro que decide, efectos que pasan una vez, y un repositorio que guarda las observaciones en un archivo y llega al ViewModel por el constructor. Abre la fase 5, arquitectura y datos.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** el flujo unidireccional de datos y por qué un solo `UiState` evita estados que no existen
- **Escribir** los intents de una pantalla como una `sealed interface` y sus reglas como un reducer puro, probado en la JVM
- **Elegir** cómo mandar un efecto de un solo uso (`Channel`, `SharedFlow`, `StateFlow` o estado) sabiendo qué pierde cada uno
- **Separar** la fuente de verdad detrás de una interfaz de repositorio y guardar en un archivo sin perder datos
- **Inyectar** dependencias a mano con `viewModelFactory` y un contenedor en la `Application`

---

## 📋 Prerrequisitos

- [Semana 27](../week-27-pantallas_grandes_y_mapa/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Corrutinas y `Flow` de las semanas 6 y 7, el `SavedStateHandle` de la semana 21 y la estabilidad de la semana 26.

---

## 🗂️ Estructura de la Semana

```
week-28-arquitectura_udf_y_mvi/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el reducer de la salida, y el aviso que no se repite
├── 3-proyecto/                  # la capa 27 de tu Bitácora: un estado, intents y un repositorio
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · El flujo unidireccional](1-teoria/01-flujo-unidireccional.md) | Estado abajo, eventos arriba; un solo `UiState`; el estado intermedio de dos `StateFlow`; MVVM y MVI | 35 min |
| [02 · Intents y el reducer](1-teoria/02-intents-y-reducer.md) | La `sealed interface` de intents, el reducer puro, el `when` sin `else`, el mismo estado si no cambia nada | 45 min |
| [03 · Efectos de un solo uso](1-teoria/03-efectos-de-un-solo-uso.md) | Estado o efecto; `Channel` y `LaunchedEffect`; lo que pierden `SharedFlow` y `StateFlow`; el evento como estado | 45 min |
| [04 · El repositorio y la DI a mano](1-teoria/04-repositorio-y-di-manual.md) | La fuente de verdad, el archivo con escritura atómica, inyectar por el constructor, `viewModelFactory` y la `Application` | 50 min |
| [05 · Probar la arquitectura](1-teoria/05-probar-la-arquitectura.md) | Dónde va cada test, `Dispatchers.setMain`, `runTest`, un *fake*, y lo que sigue necesitando Robolectric | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El reducer de la salida](2-practicas/ejercicio-01-el-reducer-de-la-salida/README.md) | Las reglas de la lista de una salida al campo en un reducer puro, en un proyecto de Kotlin sin Android | 2 h |
| [02 · El aviso que no se repite](2-practicas/ejercicio-02-el-aviso-que-no-se-repite/README.md) | El ViewModel de esa lista, con un repositorio por el constructor, avisos por un `Channel` y vuelta atrás si guardar falla, en la JVM | 2 h |

### Proyecto

La Bitácora con un solo estado, intents, un reducer puro, efectos y un repositorio que guarda las observaciones en un archivo, inyectado a mano. La persona ve lo mismo, y sus observaciones siguen ahí al abrir la app otra vez.
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
| Saber si falta un intent | Sin `else` en el `when` del reducer: el compilador dice cuál falta |
| Probar un ViewModel sin Android | `Dispatchers.setMain(UnconfinedTestDispatcher())` en `@Before` y `resetMain()` en `@After` |
| Comprobar que un efecto no llega | `assertNull(withTimeoutOrNull(100) { flow.first() })`, dentro de `runTest` (no espera de verdad) |
| Recoger un flujo mientras el test sigue | `backgroundScope.launch { flow.toList(lista) }` |
| Ver el archivo de la Bitácora en un móvil | `adb shell run-as dev.bitacora.agro.debug cat files/observations.json` (field, debug; `run-as` solo funciona con apps depurables) |
| Empezar de cero en el móvil | `adb shell pm clear dev.bitacora.agro.debug` borra el archivo y todo lo demás |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 9 de 9 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-28` con un solo `UiState`, `onIntent`, el reducer puro, los efectos por un `Channel`, el repositorio del archivo inyectado a mano, los tests de la semana 27 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-28` → `main` con la salida de `./gradlew test`
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
| [Semana 27](../week-27-pantallas_grandes_y_mapa/README.md) | **Semana 28** | Semana 29 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
