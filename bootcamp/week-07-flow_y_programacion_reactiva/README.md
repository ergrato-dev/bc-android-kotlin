# Semana 07 — Flow y programación reactiva

> La semana 6 enseñó a esperar **un** resultado sin bloquear. Una app de campo recibe **muchos**, a lo largo del tiempo: fuentes que van terminando, una lista que cambia, un usuario que escribe. Esta semana la Bitácora deja de ser una lista que se consulta y pasa a ser un dato que se **observa**.

Todavía no hay `kotlinx.serialization` ni DSLs (semana 8). `Dispatchers.Main`, `viewModelScope` y la recogida con ciclo de vida llegan en las fases 3 y 4; hoy el entorno es Kotlin puro y tiempo virtual.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Crear** flujos fríos con `flow { }` y encadenar operadores intermedios y terminales
- **Explicar** por qué un flujo es frío y qué cambia con un `StateFlow` o un `SharedFlow`
- **Mover** el origen de un flujo a otro dispatcher con `flowOn` y elegir entre `buffer`, `conflate` y `collectLatest`
- **Capturar** errores con `catch`, cerrar con `onCompletion`, reintentar con `retry` y respetar la transparencia de excepciones
- **Modelar** estado observable con `MutableStateFlow` y `update`, y eventos con `SharedFlow`
- **Combinar** entradas con `combine`, cancelar lo obsoleto con `flatMapLatest` y emitir desde varias coroutines con `channelFlow`
- **Probar** flujos con `runTest`, tiempo virtual y colectores en `backgroundScope`

---

## 📋 Prerrequisitos

- [Semana 06](../week-06-coroutines_y_concurrencia/README.md) completada: `suspend`, scopes, cancelación, `Semaphore`, `runTest`.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 6 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-07-flow_y_programacion_reactiva/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 6 de tu Bitácora: almacén reactivo y sincronización por eventos
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Flujos fríos y operadores](1-teoria/01-flujos-frios-y-operadores.md) | `flow { }`, `emit`, `collect`, `map`, `scan`, terminales | 40 min |
| [02 · Contexto, buffer y velocidad](1-teoria/02-contexto-buffer-y-backpressure.md) | `flowOn`, `buffer`, `conflate`, `collectLatest` | 35 min |
| [03 · Errores y ciclo de vida](1-teoria/03-errores-y-ciclo-de-vida.md) | `catch`, `onStart`, `onCompletion`, `retry`, transparencia | 40 min |
| [04 · StateFlow y SharedFlow](1-teoria/04-stateflow-y-sharedflow.md) | Flujos calientes, `update`, `replay`, `stateIn`, `WhileSubscribed` | 50 min |
| [05 · Combinar y probar](1-teoria/05-combinar-y-probar.md) | `combine`, `debounce`, `flatMapLatest`, `channelFlow`, pruebas | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Flujos fríos](2-practicas/ejercicio-01-flujos-frios/README.md) | `ticker`, `runningSum`, `firstAbove`, `asUiState`, `retryTimes`, `logged`, `flowOn` | 2 h |
| [02 · Flujos calientes y combinación](2-practicas/ejercicio-02-flujos-calientes-y-combinacion/README.md) | `Counter`, `EventBus`, `shareAsState`, `combine`, `debounce`, `flatMapLatest` | 2 h |

### Proyecto

La Bitácora se convierte en un `LogbookStore` que se observa (`StateFlow`), con contadores por nivel y una vista filtrable que se recalcula sola. La sincronización de la semana 6 pasa a emitir un **evento por fuente en cuanto termina**, y cancelar la recogida cancela las fuentes pendientes.
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
| Ver el tipo que sale de una cadena de operadores | Ctrl+Shift+P sobre el último operador |
| Ver qué ejecuta un flujo y cuándo | `onEach { println(it) }` y `onStart`/`onCompletion` temporales |
| Mirar un flujo infinito en una prueba | `backgroundScope.launch { flujo.collect { seen += it } }` con `UnconfinedTestDispatcher` |
| Ejecutar un solo test | `./gradlew test --tests "StoreTest.takingOneEventCancelsTheRest"` |
| Aceptar una API en preview/experimental solo donde se usa | `@OptIn(FlowPreview::class)` en la función |
| Convertir un `collect` bloqueante en uno que no bloquea | `flujo.launchIn(scope)` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con la respuesta del paso 7
3. ✅ Proyecto: rama `week-07` con `Store.kt`, `SyncFlow.kt` y al menos 8 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-07` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y empiezan en rojo (`NotImplementedError`): es lo esperado. Si tu código no compila por `This declaration is in a preview state` o `needs opt-in`, falta el `@OptIn` de la lección 5.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 06](../week-06-coroutines_y_concurrencia/README.md) | **Semana 07** | [Semana 08](../week-08-serialization_y_dsls/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
