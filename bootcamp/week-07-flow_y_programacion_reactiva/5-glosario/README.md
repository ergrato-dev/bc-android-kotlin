# Glosario — Semana 07

| Término | Definición |
| --- | --- |
| **backgroundScope** | Scope de `runTest` para coroutines que no terminan; se cancela solo al acabar la prueba |
| **buffer** | Operador que guarda los valores en una cola para que productor y colector no se esperen |
| **catch** | Operador que captura los errores del flujo de **arriba**; no los del colector |
| **channelFlow** | Constructor de flujos que permite emitir desde varias coroutines con `send` |
| **collect** | Terminal que suspende y ejecuta un bloque por cada valor |
| **collectLatest** | Como `collect`, pero cancela el bloque anterior cuando llega un valor nuevo |
| **combine** | Recalcula con el último valor de cada entrada cuando cualquiera cambia |
| **conflate** | Operador que descarta los valores intermedios y deja el último |
| **debounce** | Deja pasar solo el último valor tras un tiempo sin cambios |
| **Flow** | Secuencia de valores asíncrona; por defecto, fría |
| **flowOn** | Cambia el dispatcher de lo que está por encima en la cadena |
| **flatMapLatest** | Transforma cada valor en un flujo y cancela el anterior al llegar uno nuevo |
| **Flujo caliente** | Existe aunque nadie lo recoja y reparte sus valores a todos los colectores |
| **Flujo frío** | No hace nada hasta que lo recogen y se ejecuta de nuevo por cada colector |
| **launchIn** | Recoge un flujo en otra coroutine y devuelve su `Job` |
| **onCompletion** | Se ejecuta al terminar el flujo (bien, con error o cancelado); no captura el error |
| **onStart** | Se ejecuta antes del primer valor y puede emitir |
| **Preservación del contexto** | Un flujo emite en el contexto de su colector; `flowOn` es la vía para cambiarlo |
| **replay** | Cuántos valores recientes recibe un suscriptor tardío de un `SharedFlow` |
| **retry** | Vuelve a recoger el flujo desde el principio tras un fallo |
| **scan** | Operador de acumulado: emite el valor inicial y cada paso |
| **SharedFlow** | Flujo caliente para eventos, con `replay` y buffer configurables |
| **SharingStarted** | Política de `stateIn`/`shareIn`: `Eagerly`, `Lazily` o `WhileSubscribed` |
| **StateFlow** | Flujo caliente con un valor actual, que ignora valores iguales y nunca termina |
| **stateIn** | Convierte un flujo frío en un `StateFlow` dentro de un scope |
| **Transparencia de excepciones** | Un flujo no esconde los errores del colector; no se pone `try/catch` alrededor de `emit` |
| **update** | Modifica un `MutableStateFlow` de forma atómica; su bloque puede ejecutarse más de una vez |
| **WhileSubscribed** | El origen corre mientras hay suscriptores y un tiempo de cortesía después |
