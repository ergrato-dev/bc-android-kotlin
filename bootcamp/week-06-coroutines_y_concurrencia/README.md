# Semana 06 — Coroutines y concurrencia

> Hasta ahora, tu Bitácora lee datos que ya están en memoria. Una app de campo los pide a servidores lentos, con cobertura intermitente, y la pantalla no puede quedarse esperando. Esta semana la Bitácora aprende a **esperar sin bloquear**: pide varias fuentes a la vez, aguanta que una se cuelgue y se puede cancelar en cualquier momento.

Todavía no hay `Flow` (semana 7): aquí cada operación devuelve **un** resultado. Las coroutines de Android (`viewModelScope`, `Dispatchers.Main`) llegan en la fase 3; hoy el entorno es Kotlin puro y tiempo virtual.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Escribir** funciones `suspend` y lanzar trabajo con `launch` y `async`
- **Explicar** la concurrencia estructurada: quién espera a quién y quién cancela a quién
- **Elegir** el `Dispatcher` adecuado y cambiar con `withContext`
- **Cancelar** de forma cooperativa y limpiar recursos con `NonCancellable`
- **Distinguir** `coroutineScope` de `supervisorScope` y saber qué hace un `CoroutineExceptionHandler`
- **Proteger** estado compartido con `Mutex`, limitar con `Semaphore` y comunicar con `Channel`
- **Probar** código asíncrono con `runTest` y tiempo virtual, sin esperas reales

---

## 📋 Prerrequisitos

- [Semana 05](../week-05-enums_sealed_y_genericos/README.md) completada: `enum`, `sealed`, `value class`, genéricos, `inline`.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 5 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-06-coroutines_y_concurrencia/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 5 de tu Bitácora: sincronización asíncrona
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Suspend y scopes](1-teoria/01-suspend-y-scopes.md) | `suspend`, `launch`, `async`, concurrencia estructurada | 45 min |
| [02 · Dispatchers y contexto](1-teoria/02-dispatchers-y-contexto.md) | `Default`, `IO`, `Main`, `withContext`, `CoroutineContext` | 35 min |
| [03 · Cancelación y excepciones](1-teoria/03-cancelacion-y-excepciones.md) | Cancelación cooperativa, tiempos límite, `supervisorScope` | 50 min |
| [04 · Estado compartido y canales](1-teoria/04-estado-compartido-y-canales.md) | `Mutex`, `Semaphore`, `Channel` | 40 min |
| [05 · Probar coroutines](1-teoria/05-probar-coroutines.md) | `runTest`, tiempo virtual, dispatchers de prueba | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Suspend y scopes](2-practicas/ejercicio-01-suspend-y-scopes/README.md) | `fetchAll`, `race`, `retry`, `orDefault` y una cuenta atrás cancelable | 2 h |
| [02 · Cancelación y estado compartido](2-practicas/ejercicio-02-cancelacion-y-estado-compartido/README.md) | `Mutex`, `Channel`, `supervisorScope`, `NonCancellable`, `Semaphore`, `ensureActive` | 2 h |

### Proyecto

La Bitácora se sincroniza con varias fuentes remotas simuladas: las pide a la vez (con un máximo de paralelas), pone tiempo límite a cada una, anota las que fallan con su motivo y junta lo recibido. Todo probado en tiempo virtual.
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
| Ver qué funciones son de suspensión | El gutter de IntelliJ marca con una flecha cada llamada `suspend` |
| Ver el nombre de la coroutine en una traza | Añade `CoroutineName("x")` al contexto y arranca la JVM con `-Dkotlinx.coroutines.debug` |
| Ejecutar un solo test | `./gradlew test --tests "SyncTest.maxParallelOneAddsTheTimes"` |
| Ver la salida de `println` dentro de un test | Ya está activada en `build.gradle.kts` (`showStandardStreams`) |
| Convertir un bloque en `coroutineScope` | Alt+Enter → "Surround with" |
| Aceptar una API experimental solo donde se usa | `@file:OptIn(ExperimentalCoroutinesApi::class)` en la cabecera del archivo |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con la respuesta del paso 8
3. ✅ Proyecto: rama `week-06` con `Sync.kt` y al menos 8 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-06` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y empiezan en rojo (`NotImplementedError`): es lo esperado. Si el proyecto no compila por `This declaration needs opt-in`, falta el `@file:OptIn` de la lección 5.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 05](../week-05-enums_sealed_y_genericos/README.md) | **Semana 06** | [Semana 07](../week-07-flow_y_programacion_reactiva/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
