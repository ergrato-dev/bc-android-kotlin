# Rúbrica de Evaluación — Semana 32: WorkManager y offline-first

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: lo que se apunta sin red |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué le pasa a una corrutina de `viewModelScope` cuando la persona sale de la app? ¿Cuándo usarías una corrutina, un servicio en primer plano y WorkManager? ¿Qué hace WorkManager para sobrevivir a la muerte del proceso y al reinicio? | 10 |
| 2 | ¿Qué significan `success`, `retry` y `failure`? ¿Qué criterio usa la Bitácora para elegir, y por qué `runAttemptCount + 1 >= MAX_ATTEMPTS`? | 10 |
| 3 | ¿Por qué un worker con dependencias necesita una `WorkerFactory`? ¿Qué pasa si falta? ¿Por qué `SyncWorkerFactory` recibe un `Provider` y no el repositorio? | 10 |
| 4 | ¿Qué condiciones pone la Bitácora y cuáles la copia del ejercicio 01, y por qué distintas? ¿Qué pasa si una condición deja de cumplirse mientras el trabajo corre? ¿Cuáles son los mínimos del backoff y de un periódico, y qué hace WorkManager si pides menos? | 10 |
| 5 | ¿Qué hacen `KEEP`, `REPLACE`, `APPEND` y `APPEND_OR_REPLACE`? ¿Cuál usa la Bitácora para sincronizar y por qué no es peligroso cancelar a mitad? | 10 |
| 6 | ¿Por qué la cola es una tabla, en la misma transacción que el cambio, con una fila por código? ¿Qué es una lápida y qué pasa sin ella? | 10 |
| 7 | ¿En qué orden sincroniza la Bitácora, y por qué? ¿Por qué `sent` borra «la A-1 de las 10:00» y no «la A-1»? ¿Qué hace con un 409 y con un 404? | 10 |
| 8 | ¿Qué es «gana el último»? ¿Para qué hace falta un desempate, y por qué tiene que ser el mismo en todas partes? ¿Qué puede perder con relojes desajustados, y qué alternativas hay? | 10 |
| 9 | ¿Por qué hay que quitar `WorkManagerInitializer` del manifiesto y no el proveedor de `androidx.startup` entero? ¿Qué pasa en Robolectric con el WorkManager de verdad? | 10 |
| 10 | ¿Qué prueba `TestListenableWorkerBuilder` y qué `WorkManagerTestInitHelper`? ¿Qué hace `SynchronousExecutor`? Tras un `retry`, ¿cómo vuelves a ejecutar el trabajo en el test? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Se cancela con el ViewModel, y si el proceso muere, se pierde. Corrutina: lo que la persona espera delante de la pantalla («Actualizar»). Servicio en primer plano: lo largo que la persona sabe que está pasando, con una notificación (una ruta con GPS). WorkManager: lo que tiene que acabar alguna vez, aunque la app se cierre (subir los cambios). Guarda cada trabajo en su base de datos (Room 2) antes de hacerlo, y tras un reinicio un receptor de `BOOT_COMPLETED` vuelve a programar lo pendiente (de ahí el permiso `RECEIVE_BOOT_COMPLETED`).
2. `success`: hecho (`SUCCEEDED`). `retry`: más tarde, con un intento más y la espera del backoff. `failure`: no tiene arreglo (`FAILED`), ni lo de detrás en una cadena. La Bitácora: ¿puede salir bien la segunda vez? Sin red, tiempo agotado o error del servidor, sí; una respuesta ilegible, no. `runAttemptCount` empieza en 0: el quinto intento tiene `runAttemptCount = 4`, y `4 + 1 >= 5` es el último.
3. WorkManager guarda el nombre de la clase y la crea por reflexión con `(Context, WorkerParameters)`. Sin fábrica, no encuentra ese constructor y el trabajo acaba en `FAILED`, sin excepción en el test (ejercicio 01). Con el repositorio directo, Hilt lo crearía al inyectar la fábrica en `BitacoraApp`; el repositorio llama a `WorkManager.getInstance`, que pide la configuración a `BitacoraApp`, que lee la fábrica aún sin asignar: `lateinit property workerFactory has not been initialized`. Con `Provider`, se pide al crear el primer worker.
4. Bitácora: `CONNECTED` (pocos kilobytes, mejor pronto). Copia: `UNMETERED` y batería no baja (un archivo entero que puede esperar), y la diaria, además, cargando. Si deja de cumplirse, WorkManager para el trabajo (cancela la corrutina) y lo vuelve a poner en cola. Backoff: 10 s como mínimo; periódico: 15 minutos. Lo que sea menor lo sube sin avisar (medido: 1 s da `10000`, 5 min da `900000`).
5. `KEEP`: se queda el que estaba. `REPLACE`: cancela el de antes y entra el nuevo. `APPEND`: el nuevo detrás, en `BLOCKED`, y si el de antes falla, también. `APPEND_OR_REPLACE`: como `APPEND`, pero empieza de cero si el de antes ya falló. La Bitácora usa `REPLACE`: la nueva sincronización lee la cola entera, con el último cambio; con `KEEP`, un cambio hecho mientras una corre esperaría a la siguiente. Cancelar a mitad no estropea nada porque cada paso es idempotente (`PUT`, `DELETE`, la misma regla al bajar) y la cola solo se vacía de lo que ya subió.
6. En memoria se perdería con el proceso, y en el `Data` del trabajo no cabe ni se puede consultar. En la misma transacción: o el cambio y su fila, o ninguno; si no, un proceso muerto entre las dos escrituras deja un cambio que no se sube nunca. Una fila por código: tres cambios sin red son una subida, con lo último. La lápida es el recuerdo de un borrado (`deleted = true`, con su hora): sin ella no hay nada que subir, y una versión más vieja de otro sitio devolvería la observación.
7. Bajar, decidir (`merge` frente a la cola) y subir lo que queda. Bajando primero, la regla vive en el móvil y se prueba con datos a mano; el servidor solo confirma. Si mientras se sube la A-1 de las 10:00 la persona la cambia a las 10:01, borrar «la A-1» perdería el cambio nuevo; con la hora, se queda. 409: el servidor tiene una más nueva; el cambio sale de la cola y la suya llega en la siguiente. 404 al borrar: ya no está; sale de la cola.
8. De dos versiones, gana la de hora mayor. Con la misma hora hace falta decidir, y si cada sitio decide distinto, cada uno se queda con una y no convergen. En la Bitácora gana lo de aquí al bajar, y el servidor arbitra con 409; en el ejercicio 02, el nombre del móvil. La hora es la de cada móvil: uno adelantado gana aunque escribiera antes. Alternativas: la hora del servidor (pierde el orden de lo hecho sin red), números de versión con 409 (hay que resolver a mano o juntar), juntar los cambios (CRDT).
9. El inicializador arranca WorkManager con la configuración de serie, sin la fábrica, antes de que la app pueda dar la suya. El proveedor de `androidx.startup` lleva también otros inicializadores (maps-compose, OkHttp): quitarlo entero los quitaría. En Robolectric, el inicializador no corre: sin `Configuration.Provider`, `getInstance` lanza `IllegalStateException`; con él, arranca bajo demanda, es estático entre tests y lo que pide red no corre nunca.
10. `TestListenableWorkerBuilder`: la lógica de `doWork()` sola, con el intento, los datos y la fábrica que digas. `WorkManagerTestInitHelper`: un WorkManager de tests para probar la cola (condiciones, trabajo único, reintentos), donde nada corre hasta que el `TestDriver` lo dice. `SynchronousExecutor`: todo en el hilo del test, sin esperar. Tras un `retry`, `setAllConstraintsMet` otra vez (`setInitialDelayMet` no lo vuelve a ejecutar).

**Ejercicio 01, paso 7**: (a) `withoutWifiItWaits` (`expected:<SUCCEEDED> but was:<FAILED>`) y `aBusyServerWaitsAndTriesAgain` (`expected:<ENQUEUED> but was:<FAILED>`). Ninguna excepción: WorkManager no encuentra el constructor de dos parámetros y deja el trabajo en `FAILED`. En una app de verdad, ninguna copia se haría, y nadie se enteraría salvo mirando `WorkInfo`. (b) `askingTwiceMakesOneCopy`: el id ya no es el primero. `REPLACE` sirve si la copia nueva vale más que la que espera (por ejemplo, si cada petición lleva un archivo distinto que sustituye al anterior). (c) `10000`: el mínimo del backoff es 10 s. `900000`: el del periódico, 15 min. (d) El trabajo se queda en `ENQUEUED` con `runAttemptCount = 1` y el servidor recibe una sola llamada: `setInitialDelayMet` es para `setInitialDelay`, no para la espera del backoff. (e) Un periódico vuelve a la cola después de cada vez: si falla esa noche, lo intenta la siguiente. Se le puede poner backoff, pero con un periodo de 24 h no hace falta.

**Ejercicio 02, paso 6**: (a) `aVersionDoesNotBeatItself`. Con `>=`, de dos idénticas gana la segunda del `reduce`: da igual aquí, porque son iguales, pero si guardasen algo más (de qué lista vienen), el resultado dependería del orden. (b) `aNewerDeletionWins`. Ana borra la nota y sincroniza: sin lápida, en el servidor la nota ya no está... pero Luis, que la tiene de antes, sincroniza y la sube otra vez, y vuelve para todos. (c) La hora del servidor: el cambio de Ana, hecho sin red, llegaría tarde y ganaría a uno más nuevo de Luis subido antes. Números de versión: el segundo en subir recibe un 409 y alguien tiene que elegir. (d) Si el servidor tiene otra versión con la misma hora, contesta 409 al `PUT`: el cambio de aquí sale de la cola y la del servidor entra en la siguiente sincronización. Los dos acaban con la del servidor.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La copia del cuaderno (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| `doWork` con `runAttemptCount` bien contado y sin reintentar lo que no tiene arreglo; condiciones y backoff en la petición; trabajo único con la política que no duplica; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado, con los estados de (a), los valores de (c) y lo que pasa en (d) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Gana el último (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| `beats` con desempate estricto, `merge` que conserva las lápidas y no depende del orden, `Clock` sin estado de más (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con el escenario de (b) contado paso a paso | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Lo que se apunta sin red

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 31 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: guardar dos veces lo mismo, un cambio durante la subida, el empate, el 404 al borrar, sin red no toca nada, `FAILED` y otra sincronización | 25 |
| Datos | Versión 2 con su migración probada; la cola en la misma transacción que el cambio, con lápidas; «gana el último» en el DAO; `refresh()` que baja, decide y sube, vacía la cola solo de lo que sigue igual y no se traga la cancelación | 25 |
| App | `SyncWorker` con `success`, `retry` y `failure` bien elegidos; la fábrica inyectada con `Provider`; `Configuration.Provider` y el inicializador fuera del manifiesto; la sincronización encolada como trabajo único con red y backoff; el ViewModel sin cambios | 15 |
| Adaptación al dominio | Qué cambia en tu dominio sin red, y qué puede perder «gana el último»; README con lo que pide el paso 11 | 15 |
| Entrega | Rama `week-32`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `core` o `data` dependen de Android o de WorkManager | -10 |
| La cola en memoria, en el `Data` del trabajo o escrita fuera de la transacción del cambio | -10 |
| Un borrado que no llega al servidor (sin lápida) | -10 |
| El ViewModel o la pantalla llaman a WorkManager | -10 |
| `retry()` para siempre, sin límite de intentos, o `retry()` con una respuesta ilegible | -10 |
| `catch (e: Exception)` sin relanzar lo que no es de la red, o `runCatching` alrededor de la sincronización | -10 |
| Un test que espera de verdad (`Thread.sleep`, un `delay` real) a la red o al backoff | -10 |
| Un test de la semana 31 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-32` → `main` en el repositorio del aprendiz.
