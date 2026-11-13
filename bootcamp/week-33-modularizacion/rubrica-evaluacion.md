# Rúbrica de Evaluación — Semana 33: Modularización

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora en módulos |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué se gana al modularizar una app, además de tiempo de build? ¿Qué cuesta? ¿Cuándo no merece la pena? | 10 |
| 2 | ¿Qué distingue un módulo app, uno feature y uno core? Dibuja el grafo de la Bitácora y di sus cinco reglas. ¿Cuál de ellas comprueba Gradle solo? | 10 |
| 3 | ¿Por qué `RemoteObservation.toEntity()` y el builder `bitacora()` están en `:core:data` y no en `:core:database`? ¿Qué pasaría si `bitacora()` estuviera en `:core:database`? | 10 |
| 4 | ¿Cuándo es `api` una dependencia y cuándo `implementation`? ¿Por qué `:core:data` tiene casi todo en `api`, y qué no tiene? ¿Para qué sirve `internal` con varios módulos, y quién ve lo `internal` de un módulo? | 10 |
| 5 | ¿Por qué deja de compilar `if (latitude != null) Position(latitude, ...)` al mover la entidad a otro módulo? ¿Cómo se arregla? | 10 |
| 6 | ¿Qué es el namespace de una biblioteca de Android? ¿Qué pasa si dos módulos tienen el mismo? ¿Qué es una `R` no transitiva y cómo usa la app un texto de `:feature:map`? | 10 |
| 7 | ¿Cómo se juntan los recursos y los manifiestos de las bibliotecas en la app? ¿Quién gana si se repite un nombre? ¿Cómo quita la app un permiso que trae una biblioteca? | 10 |
| 8 | ¿Por qué los tests de Robolectric de una biblioteca necesitan `testOptions.targetSdk`? ¿Qué pasa sin él en la Bitácora? ¿Qué va en `bitacora.android-base` y por qué configura `CommonExtension`? | 10 |
| 9 | ¿Por qué `MapScreen` recibe `snippet` en vez de llamar a `line()`? ¿Qué se queda en la app y por qué? ¿Cuándo crearías `:core:ui`? | 10 |
| 10 | ¿Qué tareas se ejecutan al cambiar el cuerpo de `MapScreen` en la Bitácora modular, y cuáles en la de la semana 32? ¿Y al añadir una función pública a `:core:model`? ¿Por qué? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Límites que vigila el compilador (un `import` prohibido no compila), `internal` con sentido, builds que solo compilan lo que cambia y en paralelo lo que no se conoce, tests más cerca de lo que prueban y equipos dueños de sus módulos. Cuesta archivos de build (para eso están los convention plugins), configuración, decisiones de frontera y sorpresas (smart cast, `R` por módulo, versiones por módulo). No merece la pena en una app pequeña con un build de segundos, ni partiendo por clase, ni antes de saber dónde están las fronteras.
2. App: junta las piezas (actividad, navegación, Hilt); nadie depende de ella. Feature: una pantalla, con sus textos y dependencias; solo la usa la app. Core: lo compartido (dominio, base de datos, red, repositorio). `:app` → `:feature:map` → `:core:model`; `:app` → `:core:data` → `:core:database` → `:core:model`, y `:core:data` → `:core:network`. Reglas: nadie depende de `:app`; una feature no depende de otra; `core` no depende de features; `:core:model` no depende de nada; una feature no va a la base de datos (ni a la red) sin `:core:data`. Gradle solo comprueba los ciclos, y al construir, no al configurar.
3. `:core:database` no sabe de la red, y `:core:network` no sabe de la base de datos: la traducción entre los dos va en el módulo que conoce a los dos. `bitacora()` lee el JSON de la semana 29 con `decode`, que está en `:core:data`. En `:core:database`, la base de datos dependería de los datos y los datos de la base de datos: un ciclo.
4. `api` si el tipo sale en una firma pública del módulo (parámetro, retorno, supertipo); `implementation` si solo se usa dentro. `:core:data` expone `Observation` y `Flow` en `ObservationRepository`, `BitacoraDatabase` y `ObservationApi` en el constructor de `RoomObservationRepository`, y `NetworkError` en `RefreshResult`. No expone DataStore ni el JSON. `internal` es visible solo en el módulo: `ObservationsResponse` no existe fuera de `:core:network`. Los tests del mismo módulo lo ven (`src/test` es parte del módulo).
5. Kotlin no puede saber que una propiedad pública de otro módulo devuelve lo mismo las dos veces: ese módulo puede cambiarla a un `get()` calculado sin que este se recompile. `Smart cast to 'Double' is impossible, because 'latitude' is a public API property declared in different module.` Se copia a un `val` local y se compara ese.
6. El paquete de la `R` (y de `BuildConfig`) del módulo, y el que completa los nombres relativos de su manifiesto. Dos iguales: `Namespace 'dev.bitacora' is used in multiple modules and/or libraries`, al juntar los manifiestos. `R` no transitiva: la `R` de cada módulo solo tiene sus recursos, así que un cambio en los textos de una biblioteca no cambia la `R` de los de arriba. La app usa `dev.bitacora.map.R`: `import dev.bitacora.map.R as MapR` y `stringResource(MapR.string.map)`.
7. AGP junta los recursos y los manifiestos de todas las bibliotecas con los de la app al construirla. Con el mismo nombre, gana la app, y entre bibliotecas, la que se declara primero; la app puede cambiar un texto de una biblioteca definiendo uno con el mismo nombre, y el código de la biblioteca lee el de la app. En el manifiesto de la app, `<uses-permission android:name="..." tools:node="remove" />`, con `xmlns:tools`.
8. Una biblioteca no tiene `targetSdk`, y Robolectric elige el Android que simula con él; sin él, usa `compileSdk` (37), y en la Bitácora solo está en la caché el `android-all` de Android 16: `Path is not a file: .../android-all-instrumented-17-...jar`. `bitacora.android-base` lleva `compileSdk`, `minSdk`, las opciones de los tests, Kotlin y Robolectric con su `android-all`. `CommonExtension` es lo común al `android { }` de la app y al de las bibliotecas, así que el mismo plugin sirve para las dos.
9. `line()` usa los textos de los niveles, que son de la app y de la lista; la feature no puede depender de la app. Copiarla duplica, y `:core:ui` sería un módulo para una función: el texto llega de fuera. Se quedan en la app la navegación (lo que significa «abrir»), Hilt (es lo único que conoce todas las piezas) y lo que comparten varias pantallas. `:core:ui`, cuando una segunda feature necesite lo mismo.
10. Modular: `:feature:map:compileDebugKotlin`, los `bundleLib...` del mapa, `:app:mergeLibDexFieldDebug` y `:app:packageFieldDebug`; `:app:compileFieldDebugKotlin`, `UP-TO-DATE`. Semana 32: KSP de la app, `compileFieldDebugKotlin`, `hiltCollectClasses`, la transformación con ASM, `dexBuilder`, `mergeProjectDex` y `package`. El cuerpo no cambia la ABI del mapa, y la app se compila contra la ABI. Una función pública en `:core:model` cambia su ABI: se recompila todo lo que está encima (`:core:database` con KSP, `:core:data`, `:feature:map` y la app con KSP y Hilt).

**Ejercicio 01, pasos 2 y 8**: (2) `dev.bitacora.help.R`, la del namespace de la biblioteca. (8a) `processDebugMainManifest` falla con `Namespace 'dev.bitacora' is used in multiple modules and/or libraries: AndroidManifest.xml, :feature:help`, y `:feature:help:compileDebugKotlin` también falla: dos módulos generarían la misma clase `dev.bitacora.R`, y el manifiesto no sabría con qué paquete completar `.HelpActivity`. (8b) `help_title`, sí: la app define su propio `help_title`, y está en su `R`. `help_body`, no (`Unresolved reference 'help_body'`): con la `R` no transitiva, `dev.bitacora.R` solo tiene los recursos de la app, no los de sus bibliotecas. (8c) De la biblioteca, `HelpActivity` (con `dev.bitacora.help.HelpActivity`); de la app, la `<application>` con su etiqueta. `VIBRATE` no está. (8d) «Ayuda», el de la biblioteca, y pediría vibrar: la otra app no tiene el `help_title` ni el `tools:node="remove"` de esta.

**Ejercicio 02, paso 6**: (a) `help` termina bien: no resuelve nada, y el ciclo no se ve. `build` falla con `Circular dependency between the following tasks: :core:database:compileJava \--- :core:model:compileJava`, al calcular el grafo de tareas. (b) `configurations.configureEach` llega a todas las configuraciones, también a `testImplementation`. Con `named("implementation")`, una dependencia en `testImplementation`, `api` o `ksp` pasaría sin mirar. (c) El error salta dentro de un callback de Gradle al declarar la dependencia, no en el script del módulo. Se puede añadir `project.buildFile` y la configuración (`name`) al mensaje. (d) Al configurar. Si no cambia ningún archivo de build, la caché de configuración se reutiliza y las reglas ni se ejecutan; si cambia uno, Gradle vuelve a configurar y las reglas corren otra vez.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Una biblioteca de Android (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| `targetSdk` de los tests en la biblioteca, el plural en los dos idiomas, la actividad en el manifiesto de la biblioteca, el título cambiado y el permiso quitado desde la app sin tocar la biblioteca; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: pasos 2 y 8 explicados, con el error de (a) copiado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Las reglas del grafo (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 11 de 11 en verde | 13 |
| `moduleRule` como función pura con las cinco reglas en un orden que da el mensaje más útil; el plugin mira todas las configuraciones (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con los dos resultados de (a) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora en módulos

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 32 y `ModulesTest` en verde, cada uno en su módulo, y mínimo 5 nuevos con el valor esperado escrito a mano: el mapa en los dos idiomas, el mapa sin lugares, `toNetworkError()`, lo que ve `:feature:map` y lo que recompila un cambio en el mapa | 25 |
| Módulos core | Los cuatro, de Kotlin puro, con su paquete; `api` solo para lo que sale en las firmas; `ObservationsResponse` `internal`; la traducción y `bitacora()` en `:core:data`; los esquemas con el nombre nuevo | 25 |
| Feature | `:feature:map` como biblioteca con su namespace, sus textos, Maps en `implementation` y sus tests; `snippet` en vez de `line()`; la app con `MapR`; convention plugins con lo común en `bitacora.android-base` | 15 |
| Adaptación al dominio | Qué módulo partirías después en tu dominio y por qué; README con lo que pide el paso 11, con la salida de las tareas | 15 |
| Entrega | Rama `week-33`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Un módulo `core` con el plugin de Android o con dependencias de Android | -10 |
| Una feature que depende de otra, de la app, o directamente de `:core:database` | -10 |
| `api` para algo que no sale en ninguna firma pública | -5 |
| Maps, el JSON o DataStore visibles en el classpath de compilación de la app | -10 |
| Dos módulos con el mismo paquete o el mismo namespace | -10 |
| Lógica de la app cambiada para que encaje en los módulos (más allá de `import`, `snippet` y `MapR`) | -10 |
| Un test de la semana 32 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-33` → `main` en el repositorio del aprendiz.
