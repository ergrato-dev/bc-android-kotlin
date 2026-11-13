# Features que no se conocen y reglas del grafo

> Con `:feature:map` en su módulo, la pregunta deja de ser «dónde va el código» y pasa a ser «qué sabe cada pieza de las demás». El mapa no sabe que existe una pantalla de la observación, ni dónde viven las observaciones, ni qué texto lleva cada nivel. Esta teoría cuenta cómo se dibuja la frontera de una feature, qué se queda en la app, y cómo convertir las reglas del grafo de la teoría 01 en un plugin que para el build.

## 🎯 Objetivos

- Dibujar la frontera de una feature: datos y lambdas, no pantallas ni repositorios
- Dejar en la app lo que junta las piezas: navegación, Hilt, textos compartidos
- Comprobar las reglas del grafo al configurar el build
- Saber qué no ve Gradle sin ayuda

## 1. La frontera de una feature

En la semana 27, `MapScreen` vivía en la app y lo veía todo:

```kotlin
fun MapScreen(observations: List<Observation>, onOpen: (Observation) -> Unit, modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    // ...
    Marker(snippet = resources.line(observation), ...)
}
```

`line()` escribe «A-1 · Alta · roya en el lote 2» con los textos de los niveles, que son de la app y los usa también la lista. En `:feature:map`, `line()` no existe: está en la app, y la feature no depende de la app. Hay tres salidas:

| Salida | Coste |
| --- | --- |
| Copiar `line()` y los textos de los niveles en el mapa | Dos sitios que cambiar cuando cambie un nivel |
| Un módulo `:core:ui` con los textos de los niveles, del que dependan la app y el mapa | Un módulo más, para una función |
| El texto llega de fuera: `snippet: (Observation) -> String` | Un parámetro más |

La Bitácora elige la tercera:

```kotlin
@Composable
fun MapScreen(
    observations: List<Observation>,
    snippet: (Observation) -> String,
    onOpen: (Observation) -> Unit,
    modifier: Modifier = Modifier,
)
```

y la app lo cablea:

```kotlin
MapScreen(state.observations, snippet = { resources.line(it) }, onOpen = { open(ObservationKey(it.code)) })
```

Cuando una segunda feature necesite los niveles, `:core:ui` empezará a merecer la pena. Antes, no.

La regla general: **una feature recibe datos y devuelve eventos.** Datos del modelo (`List<Observation>`), funciones para lo que no sabe hacer (`snippet`) y lambdas para lo que pasa (`onOpen`). Así el mapa se prueba con una lista escrita a mano, y la app decide qué significa «abrir».

## 2. Lo que se queda en la app

- **La navegación.** `MapKey`, `ObservationKey` y el `NavDisplay` de la semana 24 siguen en `MainActivity`. El mapa no sabe que `onOpen` abre la observación en el panel de detalle, y si mañana abriera un diálogo, el mapa no cambiaría. Con muchas features, cada una puede exponer su parte de las entradas del `NavDisplay`, pero la lista de destinos la sigue juntando la app.
- **Hilt.** `AppModule` crea la base de datos, el cliente y el repositorio, y está en la app: es lo único que conoce todas las piezas. `:feature:map` no tiene Hilt, porque no tiene ViewModel: recibe la lista de `BitacoraViewModel`.
- **Lo que comparten varias pantallas.** El tema, los colores, los textos de los niveles, `ObservationRow`: mientras solo los use la app, son de la app.

## 3. Lo que Gradle no ve

Gradle comprueba una sola regla del grafo: que no haya ciclos. Y la comprueba tarde.

> 🔬 Medido con Gradle 9.8.2, en el sandbox del ejercicio 02: con `:core:database` → `:core:model` y `:core:model` → `:core:database`, `gradle help` termina bien. `gradle build` falla con `Circular dependency between the following tasks: :core:database:compileJava \--- :core:model:compileJava`.

El resto de reglas (una feature que depende de otra, `core` que depende de una feature, una feature que va directa a la base de datos) compila y funciona. Nada se rompe el día que alguien las salta. Se rompe meses después, cuando ya hay veinte usos que deshacer.

## 4. Las reglas, en un convention plugin

Las dependencias entre módulos se declaran al configurar, y Gradle avisa de cada una al declararla. Un convention plugin que aplican todos los módulos puede mirar cada una:

```kotlin
// bitacora.module.gradle.kts
configurations.configureEach {
    dependencies.withType<ProjectDependency>().configureEach {
        moduleRule(project.path, path)?.let { throw GradleException(it) }
    }
}
```

- `configurations.configureEach` llega a todas las configuraciones, las que ya existen y las que se creen después: `implementation`, `api`, `testImplementation`, `ksp`...
- `dependencies.withType<ProjectDependency>().configureEach` se llama con cada dependencia de proyecto en cuanto se declara. Las de Maven Central no entran.
- `project.path` es el módulo que declara (`:feature:map`); `path`, el módulo al que apunta (`:feature:notes`).

`moduleRule` es una función normal, en Kotlin, con las reglas de la teoría 01:

```kotlin
fun moduleRule(from: String, to: String): String? = when {
    to == ":app" -> "$from no puede depender de :app: la app está arriba del todo, y nadie depende de ella"
    from == ":core:model" -> ":core:model no depende de nada: es la hoja del grafo, y lo usan todos"
    from.startsWith(":core:") && to.startsWith(":feature:") -> "$from no puede depender de $to: core no sabe de pantallas"
    from.startsWith(":feature:") && to.startsWith(":feature:") -> "$from no puede depender de $to: dos features no se conocen, las une la app"
    from.startsWith(":feature:") && to == ":core:database" -> "$from no puede depender de :core:database: una feature lee y escribe a través de :core:data"
    else -> null
}
```

> 🔬 Medido en el ejercicio 02: con `testImplementation(project(":feature:notes"))` en `:feature:map`, `gradle help` falla al configurar con `:feature:map no puede depender de :feature:notes: dos features no se conocen, las une la app`. El error no dice el archivo ni la línea: salta dentro de un callback de Gradle, no en el script del módulo. Sin dependencias prohibidas, `help --configuration-cache` reutiliza la caché la segunda vez (`Configuration cache entry reused.`).

> 🔬 Medido también: en Gradle 9, `ProjectDependency.dependencyProject`, que sale en muchos ejemplos viejos, ya no existe (`Unresolved reference 'dependencyProject'`). La ruta es `path`.

Como las reglas corren al configurar, sin resolver nada, funcionan con la caché de configuración: si no cambia ningún `build.gradle.kts`, ni siquiera se vuelven a ejecutar.

Otros proyectos lo hacen de otras formas: el plugin `com.autonomousapps.dependency-analysis` avisa de `api` que deberían ser `implementation` y de dependencias que no se usan, y hay plugins que dibujan el grafo y lo comparan con uno guardado. La idea es la misma: el grafo es código, y se revisa como código.

## 5. El grafo, en un test

En la Bitácora, `ModulesTest` (en la raíz, con TestKit, como `BuildTest`) mira el grafo desde fuera, con las tareas que Gradle ya trae:

- `projects` lista los módulos: los seis del plan, y ningún `:data`.
- `:app:dependencies --configuration fieldDebugCompileClasspath` dice qué ve la app al compilar. `maps-compose` no está; `project ':feature:map'` sí.
- `:core:model:jar` y los demás `jar`: una biblioteca de Android no tiene esa tarea, así que si un módulo de `core` lo fuera, fallaría.

> 🔬 Medido con Gradle 9.8.2: en la salida de `dependencies`, un módulo sale como `project ':feature:map'`, con comillas. La primera versión de `ModulesTest` buscaba `project :feature:map` y falló: un test que busca texto en la salida de Gradle depende del formato de esa versión.

## Para recordar

- Una feature recibe datos y lambdas y devuelve eventos. No conoce pantallas, repositorios ni Hilt.
- La navegación, Hilt y lo compartido por varias pantallas se quedan en la app. Un `:core:ui` llega cuando lo necesita la segunda feature, no antes.
- Gradle solo ve los ciclos, y al construir. Las demás reglas, en un convention plugin con `configurations.configureEach` y `withType<ProjectDependency>()`, que para el build al configurar.
- El grafo se prueba como el resto: `projects` y `dependencies` desde TestKit.
