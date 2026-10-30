# Probar el ciclo de vida en la JVM

> Todo lo de esta semana se puede ver en un móvil girándolo a mano, pero así no se prueba. Robolectric ejecuta actividades de verdad en la JVM, en segundos y sin emulador. Además entra en el build: tiene una versión que pinar, un Android de 200 MB que verificar y un par de sorpresas con el JDK 21.

## 🎯 Objetivos

- Escribir tests de actividad con Robolectric y `ActivityScenario`
- Simular un giro y la muerte del proceso en un test
- Configurar Robolectric en el build: JUnit 4, `--add-opens` y `android-all` resuelto y verificado por Gradle

## 1. Robolectric

Un test unitario de Android (`src/test`) se ejecuta en la JVM contra un `android.jar` vacío: cualquier llamada a Android lanza una excepción. **Robolectric** sustituye ese jar por un Android entero, `android-all`, preparado para la JVM. Con él, una actividad se crea, pasa por su ciclo de vida y monta sus vistas como en un móvil.

```kotlin
@RunWith(AndroidJUnit4::class)
class LifecycleTest {
    @Test
    fun rotatingCreatesANewActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var before: MainActivity
            scenario.onActivity { before = it }
            RuntimeEnvironment.setQualifiers("+land")
            scenario.onActivity { assertNotSame(before, it) }
        }
    }
}
```

| Herramienta | Para qué |
| --- | --- |
| `ActivityScenario.launch` | Arranca la actividad hasta `RESUMED` (de `androidx.test:core`) |
| `scenario.moveToState(...)` | La lleva a `CREATED` (al fondo), `STARTED` o `RESUMED` |
| `scenario.onActivity { }` | Ejecuta código con la actividad viva |
| `RuntimeEnvironment.setQualifiers("+land")` | Cambia la configuración: un giro |
| `Robolectric.buildActivity(...)` | Controla cada paso a mano; con `saveInstanceState(bundle)` y `setup(bundle)`, simula la muerte del proceso |

> 🔬 Medido: `moveToState(CREATED)` llama a `onPause` y `onStop`, pero no a `onSaveInstanceState`. En un móvil, pulsar Inicio sí lo llama. Para probar el estado guardado, usa el giro o `buildActivity`.

Lee la actividad **mientras está viva**, dentro de `onActivity` o justo después de crearla. Una actividad ya destruida pide un `ViewModel` nuevo y vacío a `by viewModels()`, y el test da un resultado que no es el real.

> 🔬 Medido: el primer borrador de los tests del ejercicio 02 leía la caché después de cerrar el escenario y fallaba con una solución correcta.

Robolectric usa **JUnit 4** (`@RunWith`), no el JUnit 6 de las fases 1 y 2. En los módulos Android, AGP ya ejecuta los tests unitarios con JUnit 4. `kotlin-test-junit` conecta `assertEquals` y los demás de `kotlin.test` con JUnit 4.

## 2. En el build

```kotlin
android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

dependencies {
    testImplementation(libs.bundles.robolectric)
}
```

- `isIncludeAndroidResources`: los tests ven los recursos y el manifiesto de la app.
- `--add-opens`: Robolectric toca partes internas del JDK. Sin esta línea, con el JDK 21 todos los tests fallan antes de empezar:

```text
java.lang.RuntimeException: Failed to interact with raw FileDescriptor internals; perhaps JRE has changed?
```

> 🔬 Medido con Robolectric 4.17 y Temurin 21.

`./gradlew test` en un módulo Android con AGP 9 ejecuta solo los tests de `debug` (`testDebugUnitTest`).

> 🔬 Medido: en los ejercicios no existe `testReleaseUnitTest`.

## 3. `android-all`, dentro de Gradle

Robolectric necesita `android-all` para la versión de Android que simula: por defecto, la del `targetSdk` (36, `android-all-instrumented` `16-robolectric-13921718-i7`). Si no le dices nada, lo descarga **él**, fuera de Gradle, a `~/.m2`:

- no pasa por `verification-metadata.xml`: es el único artefacto del build que nadie verifica, y pesa 204 MB;
- no sale de tus repositorios declarados ni respeta `--offline`.

> 🔬 Medido: sin el jar en `~/.m2`, `./gradlew test --offline` lo vuelve a descargar ahí y los tests pasan. Gradle no se entera.

Se arregla con lo de la fase 2: que lo resuelva Gradle, como cualquier dependencia, y que Robolectric lo coja de ahí sin salir a la red.

```kotlin
val androidAll = configurations.dependencyScope("androidAll")
val androidAllJars = configurations.resolvable("androidAllJars") { extendsFrom(androidAll.get()) }
dependencies { androidAll(libs.android.all) }

tasks.withType<Test>().configureEach {
    val jars = files(androidAllJars)
    inputs.files(jars).withNormalizer(ClasspathNormalizer::class.java).withPropertyName("androidAll")
    systemProperty("robolectric.offline", "true")
    jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Drobolectric.dependency.dir=${jars.singleFile.parentFile}") }
}
```

> 🔬 Medido: con esta configuración, `~/.m2` no se toca, el jar entra en `verification-metadata.xml`, y `tamper` sobre él hace fallar `:app:testDebugUnitTest` con `Dependency verification failed`. Con `val jars = androidAllJars.get()` (la `Configuration` dentro de la lambda), la caché de configuración no puede guardar la tarea: `cannot serialize object of type 'DefaultResolvableConfiguration'`. `files(...)` sí se puede guardar.

Cuando subas `targetSdk`, Robolectric pedirá otro `android-all`, y en modo offline el error dice qué jar busca y dónde:

```text
java.lang.IllegalArgumentException: Path is not a file: <carpeta>/android-all-instrumented-16-robolectric-13921718-i7.jar
```

> 🔬 Medido apuntando `robolectric.dependency.dir` a una carpeta sin el jar.

## 4. Lo que trae Robolectric

Robolectric no llega solo: trae unas 120 dependencias al classpath de los tests. Entre ellas está `guava` 33.6.0-jre, afectada por el aviso GHSA-xxph-c9ww-hj94, de severidad alta, publicado el 27 de octubre de 2026 y corregido en la 33.7.2. Es el caso de la semana 14: una transitiva que se sube con una restricción documentada.

```kotlin
dependencies {
    testImplementation(libs.bundles.robolectric)
    constraints {
        testImplementation(libs.guava) {
            because("GHSA-xxph-c9ww-hj94: Robolectric 4.17 trae guava 33.6.0-jre, afectada; corregida en 33.7.2")
        }
    }
}
```

> 🔬 Medido consultando en OSV las 120 dependencias de `debugUnitTestRuntimeClasspath`: `guava` es la única con un aviso. Con la restricción, los tests de los ejercicios y del proyecto siguen en verde con la 33.7.2-jre.

Que sea solo de tests no la hace inofensiva: el código de los tests se ejecuta en tu máquina y en el CI, como el de los plugins.

## 5. La memoria

Con AndroidX, Robolectric y `android-all`, Gradle carga muchas más clases. Al regenerar la verificación de la Bitácora, el daemon se queda sin `Metaspace` con los valores de serie (384 MiB) y el build se para a medias.

> 🔬 Medido: el fallo dejó `gradle/verification-metadata.xml` vacío. Con `org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8` en `gradle.properties`, el mismo comando termina y apunta 359 componentes.

## ✅ Comprueba

- [ ] ¿Qué hace Robolectric que no hace el `android.jar` de los tests unitarios?
- [ ] ¿Cómo simulas en un test que Android mató la app en segundo plano?
- [ ] ¿Por qué importa que `android-all` lo resuelva Gradle y no Robolectric?
