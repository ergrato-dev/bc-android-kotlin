# Semana 32 — WorkManager y offline-first

> En la semana 31, la Bitácora aprendió a pedir al servidor las observaciones del equipo, pero lo que se apuntaba en el campo nunca subía, y «Actualizar» podía pisarlo. Esta semana la Bitácora es offline-first: cada cambio se guarda al momento en Room y deja una fila en una cola, y una sincronización en WorkManager espera a que haya red, baja lo del servidor, decide los conflictos con la regla «gana el último» y sube lo que queda. Sobrevive a que se cierre la app y a que se reinicie el móvil, reintenta con espera exponencial, y se prueba sin esperar: en los tests, la red llega cuando el test lo dice.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Elegir** entre una corrutina, un servicio en primer plano y WorkManager según lo que tenga que sobrevivir el trabajo
- **Escribir** un `CoroutineWorker` que devuelve `success`, `retry` o `failure`, y crearlo con una `WorkerFactory` inyectada por Hilt
- **Encolar** trabajo con condiciones, backoff y nombre único, eligiendo la política, y leer su estado con `WorkInfo`
- **Diseñar** una cola de cambios en Room con lápidas, una sincronización que baja, decide y sube, y la regla «gana el último», sabiendo lo que puede perder
- **Probar** workers y colas con `work-testing` y la sincronización con `MockEngine`, sin esperar de verdad

---

## 📋 Prerrequisitos

- [Semana 31](../week-31-red_con_ktor/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Hilt de la semana 29, Room y sus migraciones de la semana 30, y la red de la semana 31.

---

## 🗂️ Estructura de la Semana

```
week-32-workmanager_y_offline_first/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la copia del cuaderno, y gana el último
├── 3-proyecto/                  # la capa 31 de tu Bitácora: lo que se apunta sin red
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Trabajo en segundo plano y WorkManager](1-teoria/01-trabajo-en-segundo-plano.md) | Doze y la app que no está en pantalla; corrutina, servicio o WorkManager; `CoroutineWorker` y su `Result`; `Data`; la `WorkerFactory` | 35 min |
| [02 · Peticiones, condiciones y trabajo único](1-teoria/02-peticiones-y-condiciones.md) | Una vez o periódico; condiciones; backoff y sus mínimos; trabajo único y sus políticas; cadenas; `WorkInfo` | 45 min |
| [03 · Offline-first: la cola y los conflictos](1-teoria/03-offline-first-y-conflictos.md) | La pantalla no espera a la red; la cola y las lápidas; el reloj; bajar, decidir, subir; «gana el último» y sus relojes; idempotencia | 45 min |
| [04 · La sincronización en la Bitácora](1-teoria/04-la-sincronizacion-en-la-bitacora.md) | La versión 2 y su migración; el DAO; `refresh()`; el worker y su fábrica con `Provider`; `Configuration.Provider` y el manifiesto; `SyncingRepository` | 40 min |
| [05 · Probar el trabajo en segundo plano](1-teoria/05-probar-el-trabajo-en-segundo-plano.md) | `TestListenableWorkerBuilder`; `WorkManagerTestInitHelper`, `SynchronousExecutor` y el `TestDriver`; WorkManager en Robolectric; la cola con `MockEngine` y SQL | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La copia del cuaderno](2-practicas/ejercicio-01-la-copia-del-cuaderno/README.md) | Un worker que sube un archivo con wifi y batería, reintenta lo que tiene arreglo, no se duplica y se repite cada día, probado en Robolectric sin esperar | 2 h |
| [02 · Gana el último](2-practicas/ejercicio-02-gana-el-ultimo/README.md) | La regla de los conflictos en Kotlin puro: quién gana, un desempate que no depende del orden, lápidas y un reloj que no repite ni retrocede | 1 h 30 min |

### Proyecto

La Bitácora sin red: cada cambio se guarda al momento y entra en una cola de Room; una sincronización de WorkManager, con red y espera exponencial, baja lo del servidor, decide con «gana el último» y sube lo que queda. La pantalla no cambia.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 1 h 30 min |
| Proyecto | 5 h |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver los trabajos de la app en marcha | En Android Studio, *App Inspection* → *Background Task Inspector*, con la app en debug |
| Ver lo que WorkManager tiene programado en un móvil | `adb shell dumpsys jobscheduler \| grep dev.bitacora` |
| Forzar que corra un trabajo que espera | `adb shell cmd jobscheduler run -f dev.bitacora.agro.debug <job-id>` (el id sale en el `dumpsys`) |
| Mirar la cola de la Bitácora en un móvil | *App Inspection* → *Database Inspector*, tabla `pending` |
| El estado de un trabajo en un test | `workManager.getWorkInfosForUniqueWork("sync").get()` |
| Un test de la sincronización, solo | `./gradlew :data:test --tests '*SyncTest.aNewerLocalChangeWins'` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 11 de 11 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 11 de 11 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-32` con la versión 2 de la base de datos y su migración, la cola y «gana el último» en `data`, `refresh()` que baja, decide y sube, `SyncWorker` con su fábrica en Hilt, WorkManager arrancado con la configuración de la app, los tests de la semana 31 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-32` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida para el ejercicio 01 y el proyecto:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 31](../week-31-red_con_ktor/README.md) | **Semana 32** | Semana 33 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
