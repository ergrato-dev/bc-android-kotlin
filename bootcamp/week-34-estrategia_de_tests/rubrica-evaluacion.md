# Rúbrica de Evaluación — Semana 34: Estrategia de tests

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora con estrategia de tests |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué prueba cada nivel de la pirámide en la Bitácora, y cuánto cuesta un test en cada uno (medido)? ¿Por qué `ReducerTest` es tan rápido aunque esté en la app? ¿Dónde pondrías un test de «A-10 va después de A-2», y por qué ahí? | 10 |
| 2 | ¿Qué diferencia hay entre un stub, un fake, un spy y un mock? ¿Qué papeles hace `FakeRepository`? | 10 |
| 3 | ¿Por qué un test con mocks se rompe al refactorizar y uno con fakes no? ¿Cuándo usarías MockK de todos modos? ¿Qué hace `relaxed = true`? | 10 |
| 4 | ¿Qué es un test de contrato? ¿Qué encontró en la Bitácora, y por qué no lo había visto ningún test de la app? ¿Qué va en el contrato y qué se queda en `RoomTest`? | 10 |
| 5 | ¿Qué añade `java-test-fixtures` a un módulo? ¿Qué ve el código de `testFixtures` (¿y lo `internal`?), y cómo lo usa otro módulo? ¿Qué pasa si se declara con `implementation` en lugar de `testImplementation`? | 10 |
| 6 | ¿Qué hace `runTest` con un `delay(10_000)`? ¿En qué se diferencian `StandardTestDispatcher` y `UnconfinedTestDispatcher`? ¿Qué pasa si un `TestDispatcher` tiene su propio planificador? | 10 |
| 7 | Con `launch { delay(1000); done = true }`, ¿qué vale `done` después de `advanceTimeBy(1000)`? ¿Y después de `runCurrent()`? ¿Qué lleva tiempo real dentro de `runTest`, dónde pasa eso en la Bitácora y cómo se arregla? | 10 |
| 8 | ¿Para qué son `Dispatchers.setMain` y `backgroundScope`? ¿Qué error da cada uno si falta? ¿El límite de tiempo de `runTest` es virtual o real, y de cuánto es? | 10 |
| 9 | ¿Qué comprueba Turbine que no comprueba `first()`? ¿Cómo falla si sobra un valor, si falta uno y si el `Flow` falla? ¿Por qué un test sobre un `StateFlow` puede perderse un valor intermedio? | 10 |
| 10 | ¿Qué cuenta Kover en la Bitácora y qué se quita, y por qué? ¿Por qué el informe junto daba `:core:network` cubierta sin tests propios? ¿Por qué la regla es del 90 % de líneas y no de ramas? ¿Qué no dice la cobertura? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Unidad (la JVM, una función o una clase): `:core:model`, `ReducerTest`, `ViewModelTest`, de 2 a 80 ms por test. Integración en la JVM (Room con SQLite empaquetado, Ktor con `MockEngine`, DataStore): `:core:data`, 0,1 s. Robolectric (pantallas): 0,44 s por test y unos 7 s de arranque por módulo. TestKit (el build): 26 s por test. Extremo a extremo, en un dispositivo: ninguno todavía. `ReducerTest` no usa Robolectric: lo lento es Robolectric, no el módulo de Android. El orden por número va en `:core:data`, contra Room: es una regla de la consulta, y es el nivel más bajo en el que puede fallar.
2. Stub: contesta lo que el test le dice. Fake: funciona de verdad con un atajo (en memoria). Spy: apunta lo que le piden. Mock: lleva escritas las llamadas que espera y falla si no llegan (o si llega una sin configurar). `FakeRepository` es un fake (`MutableStateFlow`), un stub (`answer`) y un spy (`saves`, `refreshes`).
3. El mock comprueba qué llamadas se hacen, y un refactor cambia las llamadas sin cambiar el comportamiento (`save` pasa a `saveAll`). El fake comprueba lo que queda. MockK, cuando no hay forma razonable de escribir un fake (una clase de un SDK sin interfaz) o cuando lo que interesa es la llamada porque no deja estado. `relaxed = true` devuelve valores vacíos en lugar de fallar con `MockKException: no answer found for ...`.
4. Los mismos tests, escritos una vez en una clase abstracta, ejecutados contra el fake y contra la implementación de verdad (JUnit 5 ejecuta los `@Test` de la clase abstracta en cada subclase). Encontró que el fake no ordenaba por número: `expected: <[A-1, A-2, A-10]> but was: <[A-10, A-2, A-1]>`. Los tests de la app usaban el fake, así que probaban la mentira. En el contrato, lo que la app da por hecho (lo de partida, lo guardado, lo que sale, el orden y el `Flow`). En `RoomTest`, lo que solo es de Room: cerrar y abrir, el JSON de la semana 29, las de partida solo una vez.
5. Un tercer conjunto de fuentes, `src/testFixtures`, con su JAR (`testFixturesJar`) y sus configuraciones. Ve lo principal del módulo con sus `api`, y lo `internal` (medido: compila), pero no `src/test`. Otro módulo lo pide con `testImplementation(testFixtures(project(":core:data")))`, y los tests del propio módulo lo ven sin declarar nada. Con `implementation`, los fakes entran en el APK.
6. Termina al momento, en 0 ms reales, con `currentTime == 10000`: el planificador adelanta el reloj virtual. `Standard` pone cada `launch` en la cola (lo ejecuta `runCurrent`, `advanceTimeBy` o una suspensión del test) y es estable; `Unconfined` lo empieza al momento y es experimental. Con su propio planificador, `advanceUntilIdle()` del test no lo mueve, y lo que lanza no se ejecuta (medido: `ran == false`).
7. `false`, porque `advanceTimeBy` no incluye el instante final; después de `runCurrent()`, `true`. En los dos casos, `currentTime == 1000`. Lo que corre en `Dispatchers.Default` o `IO` lleva tiempo real (medido: `delay(500)` tarda 504 ms y `currentTime` sigue en 0). En la Bitácora, las consultas de Room (`setQueryCoroutineContext(Dispatchers.IO)`) y el `scope` por defecto de DataStore. Se arregla inyectando el dispatcher (o el `scope`), como el `dispatcher` del `MockEngine` en `NetworkTest`.
8. `setMain`: `viewModelScope` usa `Dispatchers.Main`, que no existe en la JVM. Sin él: `IllegalStateException: Dispatchers.Main was accessed when the platform dispatcher was absent and the test dispatcher was unset`. `backgroundScope`: un ámbito que `runTest` cancela al terminar, para lo que no acaba (un `collect` de un `StateFlow`, un bucle de sondeo). Lanzado con `launch`: `UncompletedCoroutinesError: After waiting for ..., there were active child jobs ... Use TestScope.backgroundScope ...`. El límite es de tiempo real, de 1 minuto por defecto.
9. Que no hay valores de más: si queda uno sin leer al salir del bloque, falla (`Unconsumed events found: - Item(2) - Complete`). Si falta: `No value produced in 3s`, en tiempo real. Si el `Flow` falla cuando se esperaba un valor: `Expected item but found Error(...)`, con la excepción como causa. Un `StateFlow` guarda solo el último valor: si quien asigna no deja paso a quien recoge (otro hilo, un `StandardTestDispatcher`), los intermedios se pierden.
10. Las clases de los cuatro `core`. Se quita lo que genera Room (`*_Impl`), que es de Room, y los `testFixtures`, que son código de test: si contaran, un fake subiría la cobertura sin probar nada. `:core:network` salía cubierta porque los tests de `:core:data` la ejecutaban; en su informe propio, 0 de 32 líneas. Las ramas cuentan las que genera Kotlin (valores por defecto, el serializador): 20 de las 24 de `RemoteObservation` sin cubrir, ninguna de un `if` nuestro. La cobertura no dice si un test comprueba algo, ni si se prueban los casos que importan (`ConnectTimeoutException` estaba cubierta y sin probar), ni lo que falta por escribir.

**Ejercicio 01, paso 6**: (a) `UncompletedCoroutinesError: After waiting for 1m, there were active child jobs`: el bucle es un hijo del `TestScope`, `runTest` espera a que acabe, y el bucle no acaba (el reloj virtual avanza sin fin hasta el minuto real). (b) Falla: `start()` solo pone el bucle en la cola de un `StandardTestDispatcher`. Hasta que algo deja correr la cola (`runCurrent`, `advanceTimeBy`), la primera petición no se ha hecho. (c) `currentTime` 0 y el test tarda los segundos reales que espera: `Default` está fuera del reloj. Se arregla recibiendo el dispatcher (o haciendo el `delay` en el contexto de quien llama, sin `withContext`). (d) `expectNoEvents()` pasa: el siguiente valor llega a los 1000 ms virtuales, y `expectNoEvents()` mira la cola en ese instante sin mover el reloj. Falla la línea siguiente: `expected: <1000> but was: <0>`, porque nadie ha esperado al segundo valor.

**Ejercicio 02, paso 8**: (a) Con el fake del principio, 4: lo cuenta todo, la repetida y la vacía incluidas. Con el arreglado, 2. Los cuatro tests que ya pasaban no usaban nada en lo que el fake mintiera, así que no lo veían. (b) Falla `theReportJoinsBothModules` (`el fake cuenta en la cobertura`). El porcentaje no cambia, porque aquí todo está al 100 %: el fake se ejecuta entero, igual que lo demás. En un proyecto con código sin probar, sumar un fake cubierto lo subiría. (c) `:store` solo baja al 88,9 % (`remove` y las vacías de `FileNoteStore` ya no se ejecutan); el raíz, al 93,3 %, porque junta esas líneas con las de `:report`, que están cubiertas. El informe junto diluye lo que falta en un módulo. (d) El contrato es un test del módulo, no una herramienta para otros, y necesita JUnit. Para ponerlo en `testFixtures` haría falta JUnit y kotlin-test en `testFixturesImplementation`, y lo usaría otro módulo con otra implementación de `NoteStore` (por ejemplo, una con Room).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El tiempo virtual (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 12 de 12 en verde | 13 |
| `retrying` atrapa solo `IOException` y no espera tras el último intento; `searches` con los operadores en el orden correcto; `Poller` con su `Job` y sin un segundo bucle; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado, con el error de (a) copiado y el test de (c) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Fakes compartidos y cobertura (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| El fake en `testFixtures` y arreglado (no los tests); el contrato con al menos 4 tests de lo que dice `NoteStore`; Kover sin los fixtures y con `check` (revisión) | 3 |
| `NOTAS.md`: paso 8 explicado, con el test de (a) y los números de (b) y (c) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora con estrategia de tests

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada y `koverVerify` en verde | 10 |
| Tests | Los de la semana 33 y `TestingTest` en verde, y mínimo 5 nuevos con el valor esperado escrito a mano (paso 11) | 25 |
| Fakes y contrato | Un solo `FakeRepository` y un solo `FakeSettings`, en los `testFixtures` de `:core:data`, fuera del APK; el contrato con al menos 5 tests, contra los dos; el fake arreglado, no el test; `RoomTest` solo con lo de Room | 25 |
| Corrutinas, Turbine y cobertura | Turbine en los `Flow` del contrato y del ViewModel; `:core:network` con sus tests; Kover con los cuatro `core`, sin `*_Impl` ni fixtures, la regla en `check` y sin ejecutar TestKit | 15 |
| Adaptación al dominio | Tu pirámide con tus tiempos; README con lo que pide el paso 12, con la salida de `koverLog` | 15 |
| Entrega | Rama `week-34`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Un fake copiado en otro módulo, o en `implementation` (llega al APK) | -10 |
| El contrato arreglado cambiando el test en lugar del fake | -10 |
| Un `Channel` o un `collect` a mano donde basta Turbine | -5 |
| Un `Thread.sleep` o un `delay` de verdad en un test | -10 |
| Lógica de la app cambiada para que pase un test (más allá del orden del fake) | -10 |
| Tests que solo ejecutan código, sin comprobar nada, para subir la cobertura | -15 |
| La regla de Kover bajada, o una clase nuestra excluida, para que pase `koverVerify` | -15 |
| Un test de la semana 33 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-34` → `main` en el repositorio del aprendiz.
