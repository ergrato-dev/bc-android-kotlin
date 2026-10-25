# Cachés de Gradle

> Gradle tiene dos cachés distintas y se confunden. Una guarda **resultados de tareas** (no vuelvo a compilar lo mismo); la otra, **la configuración** (no vuelvo a evaluar el build). Y a partir de Gradle 9, los archivos que produce son **reproducibles** sin tocar nada.

## 🎯 Objetivos

- Distinguir caché de compilación (de resultados) y caché de configuración
- Hacer una tarea propia cacheable y comprobarlo
- Escribir tareas compatibles con la caché de configuración
- Saber qué es un archivo reproducible y por qué importa

## 1. Caché de resultados (*build cache*)

`UP-TO-DATE` solo sirve en **tu** carpeta `build/`. Si haces `clean`, o cambias de rama y vuelves, se pierde. La caché de resultados guarda la **salida** por la huella de las entradas, y la restaura (`FROM-CACHE`) aunque borres `build/`.

Se activa con `org.gradle.caching=true` en `gradle.properties`, o con `--build-cache`. Para que una tarea propia sea cacheable:

```kotlin
@CacheableTask
abstract class CountLines : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)      // la ruta no importa, solo el contenido
    abstract val source: RegularFileProperty
    /* ... */
}
```

- `@CacheableTask` dice que el resultado depende **solo** de las entradas declaradas.
- `@PathSensitive` dice si la ruta del archivo forma parte de la huella. `NONE` permite reutilizar el resultado en otra carpeta o máquina.

La caché local vive en `~/.gradle/caches/build-cache-1`. Se puede mover en `settings.gradle.kts`:

```kotlin
buildCache {
    local { directory = settingsDir.resolve("local-cache") }
}
```

> 🔬 Medido: `countObservations --build-cache` → `SUCCESS`; `clean`; otra vez → `FROM_CACHE`. Sin `@CacheableTask`, la segunda vuelta es `SUCCESS`. (Más adelante en la fase 2 verás la caché **remota**, compartida entre máquinas.)

**Cuándo NO** es cacheable una tarea: si lee algo que no declaras (la hora, una variable de entorno, un archivo fuera de las entradas), o si tiene efectos fuera de sus salidas (borrar, publicar, imprimir lo que importa). `Delete` o `announce` no se cachean.

## 2. Caché de configuración (*configuration cache*)

La fase de configuración (lección 1) cuesta, sobre todo en builds grandes. Con la caché de configuración, Gradle **guarda el grafo de tareas ya armado** y, si nada de lo que influye cambió, salta la fase 2.

```properties
# gradle.properties
org.gradle.configuration-cache=true
```

```
Configuration cache entry stored.       # primera vez
Reusing configuration cache.            # siguientes: la fase 2 no se ejecutó
Configuration cache entry reused.
```

Si cambias `gradle.properties` o `build.gradle.kts`, Gradle lo detecta y vuelve a configurar:

```
Calculating task graph as configuration cache cannot be reused because Gradle property 'bitacora.version' has changed.
```

### La regla para que funcione

Las acciones (`doLast`, `@TaskAction`) corren **después** de guardar el grafo, y no pueden depender del `Project`. Por eso:

```kotlin
tasks.register("bad")  { doLast { println(project.name) } }          // ❌ falla
tasks.register("good") { val n = project.name; doLast { println(n) } }   // ✅ captura el valor antes
```

```
Invocation of 'Task.project' by task ':bad' at execution time is unsupported with the configuration cache.
```

Es la regla de oro: **lo que necesites, léelo al configurar, en una variable o un `Provider`; la acción solo usa lo capturado**. Es por esto que las tareas de la lección 3 usan `Property` y no `project`.

> 🔬 Medido: `bad` falla con `--configuration-cache` y funciona sin él; `good` guarda y después reutiliza la entrada.

## 3. Archivos reproducibles

Un archivo (`jar`, `zip`) es **reproducible** si construirlo dos veces con las mismas entradas da **los mismos bytes**. Importa para la caché (las huellas coinciden), para auditar (el SHA-256 de lo publicado es verificable) y para ahorrar subidas.

Antes, un zip incluía la fecha de cada archivo y su orden en disco, así que casi nunca salía igual. **Desde Gradle 9.0, `Jar`, `Zip` y demás son reproducibles por defecto**: fechas fijas, orden determinista, permisos fijos (0755 y 0644). No tienes que configurar nada; basta con **no deshacerlo**:

```kotlin
tasks.withType<AbstractArchiveTask>().configureEach { isPreserveFileTimestamps = true }   // ❌ rompe la reproducibilidad
```

> 🔬 Medido: `clean`, cambiar la fecha del `README.md` y reconstruir da el **mismo SHA-256**; con `isPreserveFileTimestamps = true`, uno distinto.

## 4. Resumen

| Caché | Guarda | Se activa con | Se ve como |
| --- | --- | --- | --- |
| De resultados | La salida de cada tarea | `org.gradle.caching=true` | `FROM-CACHE` |
| De configuración | El grafo de tareas ya armado | `org.gradle.configuration-cache=true` | `Reusing configuration cache` |
| `UP-TO-DATE` | *(nada: compara con `build/`)* | siempre | `UP-TO-DATE` |

## ✅ Comprueba

- [ ] ¿Qué diferencia hay entre `UP-TO-DATE` y `FROM-CACHE`?
- [ ] ¿Por qué falla `project.name` dentro de `doLast` con la caché de configuración?
- [ ] ¿Qué hace falta para que un zip sea reproducible en Gradle 9?
