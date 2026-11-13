# Ejercicio 01 — La copia del cuaderno

⏱️ 2 h · Teoría previa: 01, 02 y 05

## Objetivo

El cuaderno de campo de la finca es un archivo (`campo.db`) que conviene copiar al servidor. No corre prisa, pero no se puede perder, y pesa: mejor con wifi y sin la batería baja. Al terminar, `BackupWorker` sube el archivo y dice cuánto ha guardado; un servidor ocupado se reintenta hasta tres veces, cada vez con 20 s más de espera, y uno que rechaza la copia no se reintenta. Pedir la copia dos veces mientras espera hace una sola, y hay una copia diaria que solo corre con wifi y cargando. Todo se prueba en Robolectric sin esperar: el test decide cuándo hay wifi.

## Cómo está montado

`starter/` es una app de Android sin pantalla, con WorkManager 2.12.0 y, en los tests, `work-testing` y Robolectric. En `app/src/main/kotlin/dev/bitacora/Copia.kt` están ya hechos `BackupServer`, `Upload` (lo que contesta el servidor), las constantes y `BackupFactory` (la fábrica que le da a `BackupWorker` su servidor). Con comentarios `Paso N`: `BackupWorker.doWork()`, `backupRequest`, `backUp` y `backUpEveryDay`. Compila.

Los tests (`app/src/test/kotlin/dev/bitacora/CopiaTest.kt`, no los toques) usan un servidor de mentira que contesta lo que haya en `answers` (si no queda nada, `Stored(2048)`) y apunta las llamadas en `calls`. Los seis primeros prueban el worker solo, con `TestListenableWorkerBuilder`; los demás, la cola, con el WorkManager de los tests. Al empezar, fallan 10 de 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué `aStoredCopyIsASuccessWithItsSize` falla, si el starter ya devuelve `success`? (Compara los dos `Success` del mensaje.) ¿Por qué `withoutWifiItWaits` ve `SUCCEEDED` nada más encolar, sin que el test haya dicho que hay wifi? ¿Por qué `twoNotebooksTwoCopies` pasa ya? (Teoría 05, apartado 2.)
2. **El worker** (`doWork`): sin `KEY_FILE` en `inputData`, `failure()`. Si no, `server.upload(file)`: `Stored`, `success` con `KEY_BYTES` en los datos de salida; `Busy`, `retry` mientras no sea el intento `MAX_TRIES` (cuidado: `runAttemptCount` empieza en 0), y si lo es, `failure`; `Rejected`, `failure` (teoría 01, apartados 3 y 4).
3. **La petición** (`backupRequest`): condiciones de red `UNMETERED` y `requiresBatteryNotLow`, y backoff `LINEAR` de 20 s (teoría 02, apartados 2 y 3).
4. **Una copia por cuaderno** (`backUp`): `enqueueUniqueWork` con el nombre `"backup-$file"` y la política que deja la que ya espera (teoría 02, apartado 4).
5. **La copia diaria** (`backUpEveryDay`): cada 24 h, con wifi y cargando, con `enqueueUniquePeriodicWork("daily-$file", ...)` y la política que no la duplica.
6. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
7. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `.setWorkerFactory(factory)` de la configuración de `testWorkManager()`. ¿Qué tests fallan? ¿En qué estado queda la copia? ¿Salta alguna excepción? ¿Qué le pasaría a una app de verdad que se olvida de la fábrica? Restáuralo.
   - (b) Cambia la política de `backUp` por `REPLACE`. ¿Qué test falla? ¿Cuándo sería `REPLACE` la buena para una copia? Restáuralo.
   - (c) Pon el backoff a 5 s e imprime `backupRequest("x").workSpec.backoffDelayDuration`. ¿Qué sale, y por qué? Haz lo mismo con un periódico de 10 minutos y `periodicityInfo.repeatIntervalMillis`.
   - (d) En `aBusyServerWaitsAndTriesAgain`, cambia el segundo `conditionsMet(...)` por `WorkManagerTestInitHelper.getTestDriver(context)!!.setInitialDelayMet(copies().single().id)`. ¿Qué pasa, y por qué? Restáuralo.
   - (e) ¿Por qué la copia diaria no lleva backoff, y qué pasaría si el servidor está ocupado justo esa noche?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `theThirdBusyGivesUp` da `retry` con `attempt = 2` | `runAttemptCount` empieza en 0: el tercer intento es el 2 |
| `withoutWifiItWaits` da `SUCCEEDED` antes de `conditionsMet` | La petición no tiene condiciones (paso 3) |
| `askingTwiceMakesOneCopy` ve dos ids, o uno distinto del primero | `backUp` usa `enqueue`, o `REPLACE` |
| `everyDayIsEvery24HoursWhileCharging` ve dos trabajos | Falta `enqueueUniquePeriodicWork`, o la política no es `KEEP` |
| `theRequestWaitsForWifiAndBattery` da `10000` en el backoff | Has puesto menos de 10 s: WorkManager lo sube al mínimo |
| `./gradlew test --tests ...` da `Unknown command-line option '--tests'` | Con AGP 9: `./gradlew :app:testDebugUnitTest --tests '*CopiaTest.withoutWifiItWaits'` |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 7.
