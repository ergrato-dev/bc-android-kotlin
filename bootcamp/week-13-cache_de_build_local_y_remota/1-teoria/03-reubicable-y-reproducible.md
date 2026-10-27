# Reubicable y reproducible

> Que una tarea sea cacheable no basta. Si su clave incluye la carpeta donde está tu proyecto, nadie más acierta en la caché. Y si Gradle no conoce todas sus entradas, la caché devuelve un resultado que ya no vale.

## 🎯 Objetivos

- Explicar qué es una tarea reubicable y comprobarlo
- Reconocer salidas no reproducibles y qué hace la caché con ellas
- Declarar las entradas que Gradle no puede ver por sí solo

## 1. Reubicable

Una tarea es **reubicable** si su clave no depende de la carpeta donde está el proyecto. La misma rama en `~/code/bitacora` y en `/tmp/otra-copia` (o en la máquina del CI, en `/home/runner/work/...`) da las mismas claves.

Con `PathSensitivity.ABSOLUTE`, la ruta completa entra en la clave:

```
Appending input file fingerprints for 'source' to build cache key: 1cedabae... - ABSOLUTE_PATH{/home/tú/a2/data/observations.csv=...}
```

> 🔬 Medido en Gradle 9.8.2 con `-Dorg.gradle.caching.debug=true`. Dos copias del proyecto en carpetas distintas, con la misma caché: con `ABSOLUTE`, la segunda copia ejecuta la tarea (`SUCCESS`); con `RELATIVE`, la recupera (`FROM-CACHE`). El `compileKotlin` de las dos copias sale `FROM-CACHE`: el plugin de Kotlin ya es reubicable.

Rompen la reubicación:

- `ABSOLUTE` en un archivo de entrada.
- Una ruta absoluta guardada como `@Input` de tipo `String` (`source.get().asFile.path`). Mejor declarar el archivo con su anotación.
- Una salida que incluye rutas absolutas: el archivo cacheado lleva la ruta de la máquina que lo generó.

## 2. Reproducible

Una tarea es **reproducible** si las mismas entradas dan las mismas salidas, byte a byte. Una que escribe la hora no lo es:

```kotlin
report.get().asFile.writeText("observations=$n\ngenerated=${System.currentTimeMillis()}\n")
```

```
$ ./gradlew countObservations    → SUCCESS     generated=1793076692084
$ ./gradlew clean
$ ./gradlew countObservations    → FROM-CACHE  generated=1793076692084
```

> 🔬 Medido: después de `clean`, la salida es **la de la primera ejecución**, con la hora de entonces. La caché no vuelve a ejecutar la tarea: copia lo que guardó.

La hora no rompe la caché de esa tarea, porque no es una entrada. Pero miente, y estropea las tareas que vienen detrás: si su salida es la entrada de otra tarea, esa otra cambia de clave en cada ejecución real, y nunca acierta. Por eso `generateBuildInfo` escribe la versión y el dominio, y nunca la fecha.

## 3. Entradas que Gradle no ve

Gradle solo sabe lo que declaras. Si una tarea lee un archivo que no es una de sus entradas, la tarea queda `UP-TO-DATE` (o sale `FROM-CACHE`) aunque ese archivo cambie.

El ejemplo está en tu Bitácora. Los tests del proyecto raíz (`src/test`) copian el proyecto entero y ejecutan Gradle sobre la copia, pero para Gradle las entradas de `:test` son las clases de test y su classpath. Nada más:

```
$ ./gradlew test
$ # cambias core/src/main/kotlin/Observation.kt
$ ./gradlew test
> Task :core:compileKotlin
> Task :core:test
> Task :test UP-TO-DATE          ← los tests del build no se ejecutan
```

> 🔬 Medido en la Bitácora de la semana 12. El problema ya estaba ahí, por `UP-TO-DATE`; con la caché activada empeora, porque `clean` ya no lo arregla: `:test` sale `FROM-CACHE`, con el resultado de otro código.

Se arregla declarando las entradas que faltan con la API de ejecución:

```kotlin
tasks.test {
    inputs.files(fileTree(layout.projectDirectory) { exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**", ".git/**", ".idea/**") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("bitacoraFiles")
}
```

```
Task ':test' is not up-to-date because:
  Input property 'bitacoraFiles' file .../core/src/main/kotlin/Observation.kt has changed.
```

> 🔬 Medido: con esas líneas, cambiar `core/` vuelve a ejecutar `:test`. Después de `clean` sale `FROM-CACHE`, y también en una copia en otra carpeta: con `RELATIVE`, sigue siendo reubicable.

- `withPathSensitivity(RELATIVE)` es el `@PathSensitive` de la API de ejecución.
- `withPropertyName` le pone un nombre a la entrada, que es el que aparece en los mensajes.
- Los `exclude` dejan fuera las salidas (`build/`) y las cachés. Si no, cada ejecución cambiaría sus propias entradas.

## ✅ Comprueba

- [ ] ¿Qué es una tarea reubicable y cómo lo compruebas?
- [ ] Después de `clean`, ¿qué hora tiene el archivo de una tarea que escribe la hora? ¿Por qué?
- [ ] ¿Por qué `:test` del raíz quedaba `UP-TO-DATE` después de cambiar `core/`?
