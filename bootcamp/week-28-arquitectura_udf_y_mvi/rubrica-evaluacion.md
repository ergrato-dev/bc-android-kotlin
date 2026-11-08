# Rúbrica de Evaluación — Semana 28: Arquitectura UDF y MVI

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: un estado, intents y un repositorio |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es el flujo unidireccional de datos? ¿Qué baja, qué sube, y cuántos sitios cambian el estado? | 10 |
| 2 | Con la lista y el borrador en dos `StateFlow` y un `combine`, añades "GPS". ¿Qué valores ve quien escucha con un *dispatcher* inmediato, y cuáles con uno que encola? ¿Qué lo evita? | 10 |
| 3 | ¿Qué añade MVI a MVVM? ¿Tiene algo que ver un intent de MVI con un `Intent` de Android? ¿Cuándo no compensa? | 10 |
| 4 | ¿Qué hace puro a un reducer? Di dos cosas que el reducer de la Bitácora no puede hacer, y quién las hace. | 10 |
| 5 | ¿Por qué el `when` del reducer no lleva `else`? ¿Qué dice el compilador si falta un caso? | 10 |
| 6 | ¿Por qué el reducer devuelve el mismo estado, y no un `copy()` igual, cuando nada cambia? Di dos sitios donde se nota. | 10 |
| 7 | ¿Qué es estado y qué es efecto? Clasifica: la lista, "Ubicando…", el aviso "A-1 borrada", abrir el selector de compartir. | 10 |
| 8 | Un `Channel(BUFFERED)`, un `SharedFlow` sin replay y un `StateFlow`: ¿qué hace cada uno con un efecto que llega sin nadie escuchando, y con dos iguales seguidos? ¿Qué pasa con dos que recogen el mismo `Channel`? | 10 |
| 9 | ¿Qué es la fuente de verdad de las observaciones, y qué sigue en el `SavedStateHandle`? ¿Por qué el archivo se escribe en un `.tmp` y se mueve, y qué pasa con un archivo que no se puede leer? | 10 |
| 10 | ¿Qué es inyectar una dependencia? ¿Dónde se crea el repositorio, cómo le llega al ViewModel, y qué pasa si falta `android:name`? ¿Qué problema de la DI a mano resuelve Hilt? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Los datos van en un solo sentido: el estado baja del ViewModel a la pantalla, de solo lectura; lo que pasa sube como un intent; y un solo sitio (`onIntent`, con el reducer) decide el estado nuevo.
2. Con uno inmediato (`UnconfinedTestDispatcher`, como `Main.immediate`), tres: `[agua]/GPS`, `[agua, GPS]/GPS` y `[agua, GPS]/`; el del medio no debería existir. Con uno que encola, dos: el del medio no llega. Un solo `MutableStateFlow` con un `copy` que cambia los dos campos a la vez.
3. Una sola entrada para todo lo que pasa (los intents), un reducer puro que decide, y los efectos aparte. MVI es una forma de escribir el ViewModel de MVVM. El intent de MVI no tiene que ver con el `Intent` de Android: solo el nombre. No compensa en una pantalla con dos campos y una regla.
4. Con el mismo estado y el mismo intent devuelve siempre lo mismo, y no hace nada más: no lee la hora, no escribe, no lanza corrutinas, no conoce `Context`. No guarda la lista (lo hace el ViewModel con `repository.save`), no enseña el aviso (lo pide con `ShowUndo` y lo enseña la actividad), no pide permisos ni ubica (la actividad, que después manda `Located`).
5. Para que un intent nuevo no compile hasta que alguien decida qué hace. `'when' expression must be exhaustive. Add the 'ClearDone' branch or an 'else' branch.`
6. Porque quien llama compara por instancia. El ViewModel guarda si la lista es otra (`!==`): una nueva igual se guardaría para nada. Y los tests de estabilidad (`assertSame`): Compose se salta lo que no ha cambiado. `StateFlow` no lo nota: compara con `equals`.
7. Estado: lo que tiene que volver a salir al pintar de cero, la lista y "Ubicando…". Efecto: lo que pasa una vez, el aviso y el selector. Si el aviso fuera estado, saldría otra vez al girar.
8. El `Channel` lo guarda hasta que alguien lo recoge, entrega cada uno una vez, y con dos que recogen, cada efecto le llega a uno solo (reparte). El `SharedFlow` sin replay lo tira (`tryEmit` da `true`); dos iguales los entrega a quien esté escuchando. El `StateFlow` lo guarda y lo repite al suscribirse otra vez, y dos iguales seguidos son uno.
9. El repositorio (`JsonFileObservationRepository`, en `observations.json`). En el handle, el borrador, la posición, `locating` y la última borrada: de la sesión de la pantalla, que solo tienen que sobrevivir a la muerte del proceso. El `.tmp` y `ATOMIC_MOVE`: si el proceso muere escribiendo, queda el archivo de antes entero. Uno que no se lee se aparta a `.bad` y se empieza con las de partida: no se pisa.
10. Recibirla, en vez de crearla tú, normalmente por el constructor. El repositorio se crea una vez, `by lazy`, en `BitacoraApp`; llega al ViewModel por `viewModels { viewModelFactory { initializer { ... } } }`. Sin `android:name`, Android crea una `Application` normal y el `as BitacoraApp` lanza `ClassCastException`. Hilt automatiza crear cada cosa en orden, con su ámbito, cuando hay muchas dependencias que dependen unas de otras.

**Ejercicio 01, paso 7**: (a) `'when' expression must be exhaustive. Add the 'ClearAll' branch or an 'else' branch.` Con `else`, compila, y `ClearAll` no hace nada sin que nadie lo haya decidido. (b) Porque se pide la misma instancia: un `Next(state.copy())` pasa `assertEquals` y falla `assertSame`. (c) Al girar, la pantalla nueva leería `allPacked = true` y enseñaría el aviso otra vez, salvo que algo lo ponga a `false`. (d) Milisegundos (unos 70 ms los 9). Las reglas de `add`, `delete` y `undo` del ViewModel de la semana 27, que se probaban con Robolectric o con el ViewModel entero.

**Ejercicio 02, paso 6**: (a) `aMessageWaitsForItsCollector`, `aMessageIsDeliveredOnce`, `aFailedSaveRollsBack` (los tres, por tiempo) y `twoCollectorsSplitTheMessages` (los dos reciben los dos). Los tres primeros mandan el efecto antes de que haya nadie escuchando: como entre que la actividad vieja se destruye al girar y la nueva empieza a recoger. (b) Uno cada uno. Cada sitio vería la mitad de los avisos. En un solo sitio, el que envuelve a los dos (en la Bitácora, la actividad). (c) No lanza: falla con la lista vacía, porque el `collect` de `viewModelScope` no corre en el momento. En un móvil hay `Dispatchers.Main`. (d) Con dos intents seguidos y el disco lleno, "lo de antes del intent" del segundo es la lista del primero, que tampoco se guardó; lo único que se sabe que es bueno es lo del repositorio.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El reducer de la salida (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| Un `when` sin `else`; el mismo estado cuando nada cambia; `AllPacked` solo al marcar la última; `removed` olvidada al deshacer; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado, con el mensaje del apartado (a) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — El aviso que no se repite (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| El estado desde el repositorio en `init`; guardar solo con `!==`; un `Channel(BUFFERED)` privado y `receiveAsFlow`; la vuelta a lo guardado con `SaveFailed` después del aviso (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con los tests del apartado (a) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Un estado, intents y un repositorio

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 27 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: `Replace`, `Located(null)`, deshacer solo la última, compartir sin guardar, el último `save` gana, la de ejemplo borrada sigue borrada | 25 |
| UDF y MVI | Un solo `UiState`; `onIntent` como única entrada; el reducer puro, sin `else` y sin Android, con el mismo estado cuando nada cambia; los efectos por un `Channel`, recogidos en un solo sitio; `ObservationDraftViewModel` borrado | 25 |
| Datos y DI | `ObservationRepository` en `data`; el archivo con la memoria primero, `.tmp` + `ATOMIC_MOVE`, `Mutex` y `.bad`; el repositorio por el constructor desde `BitacoraApp`; el borrador y la borrada en el handle | 15 |
| Adaptación al dominio | Los intents y textos de tu dominio; README con lo que pide el paso 9 | 15 |
| Entrega | Rama `week-28`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| El reducer lee `Context`, la hora, el disco o lanza corrutinas | -15 |
| Un efecto guardado en el `UiState` sin intent de vuelta (se repite al girar) | -10 |
| El ViewModel crea su repositorio (sin inyectarlo) | -10 |
| `core` o `data` dependen de Android | -10 |
| Un test de la semana 27 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-28` → `main` en el repositorio del aprendiz.
