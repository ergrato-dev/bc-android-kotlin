# Glosario — Semana 34

| Término | Definición |
| --- | --- |
| **`advanceTimeBy`** | Adelanta el reloj virtual de un test los milisegundos que se le pasan y ejecuta lo que toca antes de ese momento, sin incluir el instante final |
| **`advanceUntilIdle`** | Ejecuta todo lo que hay en la cola del planificador, adelantando el reloj virtual lo que haga falta, hasta que no queda nada |
| **`backgroundScope`** | Ámbito de `runTest` que se cancela al terminar el bloque del test. Para las corrutinas que no acaban solas, como un `collect` de un `StateFlow` |
| **Cobertura** | La parte del código que ejecutan los tests: líneas, ramas o instrucciones. Dice qué se ejecuta, no si se comprueba |
| **Contrato** (test de) | Los tests que cualquier implementación de una interfaz tiene que pasar, escritos una vez en una clase abstracta y ejecutados contra cada una (el fake y la de verdad) |
| **`currentTime`** | La hora del reloj virtual del test, en milisegundos desde que empezó `runTest` |
| **Doble de prueba** | Lo que sustituye en un test a una dependencia de verdad: dummy, stub, fake, spy o mock |
| **Dummy** | Un doble que solo rellena un parámetro que el test no usa |
| **Fake** | Un doble que funciona de verdad con un atajo: en memoria, sin red, sin archivo |
| **Kover** | La herramienta de cobertura de JetBrains para Kotlin: un plugin de Gradle que pone un agente en las tareas `test`, junta informes de varios módulos y verifica reglas |
| **`koverVerify`** | La tarea de Kover que falla si la cobertura no cumple las reglas (`minBound`). Entra en `check` si el proyecto tiene esa tarea |
| **Mock** | Un doble que lleva escritas las llamadas que espera y falla si no llegan como se dijo. En Kotlin, con MockK (`every`/`coEvery`, `verify`/`coVerify`) |
| **Pirámide de tests** | Muchos tests pequeños y rápidos abajo (unidad), menos en medio (integración) y pocos arriba (extremo a extremo), que son los lentos y frágiles |
| **Planificador** (`TestCoroutineScheduler`) | El reloj virtual y la cola de tareas de un test de corrutinas. Todos los `TestDispatcher` de un test tienen que compartir el mismo |
| **Rama** (cobertura de) | Cada camino de un `if`, `when`, `?:` o `&&`. En Kotlin cuenta también las que genera el compilador (valores por defecto, serializadores) |
| **`runCurrent`** | Ejecuta lo que toca en el instante actual del reloj virtual, sin moverlo |
| **`runTest`** | El constructor de tests de corrutinas: crea un `TestScope` con un reloj virtual, espera a los hijos y falla si alguno lanza una excepción. Su límite (1 min por defecto) es de tiempo real |
| **`setMain`** | `Dispatchers.setMain(...)`: cambia `Dispatchers.Main` por un `TestDispatcher` en la JVM, donde no hay hilo principal. Se deshace con `resetMain()` |
| **Spy** | Un doble (o el de verdad) que apunta lo que le piden, para que el test lo mire después |
| **`StandardTestDispatcher`** | El dispatcher de test por defecto: cada `launch` va a la cola y se ejecuta cuando el test deja correr el planificador |
| **Stub** | Un doble que contesta lo que el test le dice |
| **Test fixtures** (`testFixtures`) | Un conjunto de fuentes con código para tests (fakes, datos) que otros módulos pueden usar con `testImplementation(testFixtures(project(...)))`. Lo crea el plugin `java-test-fixtures` |
| **Tiempo virtual** | El reloj de `runTest`: un `delay` no espera, sino que adelanta la hora del test hasta la siguiente tarea |
| **Turbine** | Biblioteca de Cash App para probar un `Flow` leyendo sus eventos en orden (`awaitItem`, `awaitComplete`, `expectNoEvents`); falla si sobra un evento |
| **`UnconfinedTestDispatcher`** | Dispatcher de test que empieza cada corrutina al momento, hasta su primera suspensión. Experimental |
