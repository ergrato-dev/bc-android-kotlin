# Bibliotecas de Android: el módulo del mapa

> Los módulos `core` de la teoría 02 son Kotlin puro: no tienen recursos, ni manifiesto, ni Compose. Una feature sí: el mapa tiene sus textos, usa Compose y Google Maps, y se prueba en Robolectric. Eso es una biblioteca de Android, `com.android.library`. Esta teoría cuenta lo que cambia respecto a la app de las semanas 15 a 32: el namespace, la clase `R`, los recursos y los manifiestos que se juntan, los tests y el convention plugin que comparten la app y las bibliotecas.

## 🎯 Objetivos

- Crear una biblioteca de Android y saber qué la distingue de la app
- Entender el namespace, la `R` no transitiva y cómo se juntan los recursos y los manifiestos
- Probar una biblioteca en Robolectric
- Compartir la configuración de la app y de las bibliotecas en un convention plugin

## 1. `com.android.library`

Una biblioteca se compila igual que la app, pero no se instala: produce un AAR (clases, recursos y manifiesto) que va dentro de una app. Por eso no tiene `applicationId`, ni `versionCode`, ni firma. `:feature:map` queda así:

```kotlin
plugins {
    id("bitacora.android-library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.bitacora.map"
}

dependencies {
    // api: Observation y Modifier salen en la firma de MapScreen.
    api(project(":core:model"))
    api(platform(libs.compose.bom))
    api(libs.compose.material3)
    // implementation: Maps es cosa de este módulo.
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
}
```

La app tiene dos flavors (`demo` y `field`) y la biblioteca, ninguno. No pasa nada: AGP le da a cada variante de la app la única variante de la biblioteca que encaja (`debug` con `debug`, `release` con `release`).

> 🔬 Medido en el proyecto: con Maps en `implementation`, el classpath de compilación de la app no tiene `maps-compose`, y el de ejecución sí. El APK lo lleva, pero ningún archivo de la app puede importar `GoogleMap`. `ModulesTest.theMapLibrariesStayInsideTheFeature` lo comprueba.

## 2. El namespace

El `namespace` es el paquete de la clase `R` (y de `BuildConfig`) del módulo, y el paquete con el que se completan los nombres relativos de su manifiesto: `android:name=".HelpActivity"`, en una biblioteca con namespace `dev.bitacora.help`, es `dev.bitacora.help.HelpActivity`. Cada módulo tiene el suyo.

> 🔬 Medido con AGP 9.4.1, con `:feature:map` en el namespace de la app: `Namespace 'dev.bitacora' is used in multiple modules and/or libraries: AndroidManifest.xml, :feature:map. Please ensure that all modules and libraries have a unique namespace.` Falla al juntar los manifiestos, antes de compilar.

## 3. La `R` no transitiva

Cada módulo de Android tiene su propia clase `R`, con sus recursos y nada más. Desde AGP 8 es lo que hay por defecto (`android.nonTransitiveRClass=true`): la `R` de la app no tiene los recursos de sus bibliotecas.

> 🔬 Medido: al mover los textos `map` y `on_map` a `:feature:map`, el botón «Mapa» de la app, que hacía `stringResource(R.string.map)`, da `Unresolved reference 'map'`.

El texto ya no es de la app: es del mapa. Se usa con la `R` del mapa, con un alias para no confundirla con la de la app:

```kotlin
import dev.bitacora.map.R as MapR

Button(onClick = onMap) { Text(stringResource(MapR.string.map)) }
```

¿Por qué no transitiva? Con la `R` transitiva, la de la app tenía un campo por cada recurso de cada biblioteca (AndroidX incluido), y un cambio en cualquier recurso de cualquier módulo cambiaba la `R` de todos los que dependen de él, y los recompilaba. Con la no transitiva, cambiar un texto del mapa solo cambia la `R` del mapa.

## 4. Los recursos se juntan

Al construir la app, los recursos de todas sus bibliotecas se juntan con los suyos. Si dos tienen el mismo nombre, gana la app, y entre bibliotecas, la que se declara primero. La app puede así cambiar un texto de una biblioteca sin tocarla: le basta con un recurso del mismo nombre.

> 🔬 Medido en el ejercicio 01: la biblioteca `:feature:help` tiene `help_title` = «Ayuda»; la app define otro `help_title` = «Ayuda de la Bitácora». Dentro de la app, `HelpActivity`, que es código de la biblioteca y lee `dev.bitacora.help.R.string.help_title`, enseña «Ayuda de la Bitácora». En los tests de la biblioteca sola, «Ayuda».

Es útil, y es un peligro: dos bibliotecas con un `title` cada una se pisan sin avisar. Por eso los recursos de una feature llevan nombres que dicen de quién son, o un prefijo. AGP tiene `resourcePrefix = "map_"`, con el que lint avisa de los recursos de la biblioteca que no lo llevan.

Lint mira cada biblioteca por su cuenta: `:feature:map` tiene sus textos en `values` y en `values-en`, y `lintDebug` de la biblioteca pide las dos.

> 🔬 Medido en el ejercicio 01: sin el plural `tips` en `values-en`, `:feature:help:lintDebug` falla con `MissingTranslation`: `"tips" is not translated in "en" (English)`.

## 5. Los manifiestos se juntan

Cada biblioteca tiene su `AndroidManifest.xml`, y AGP junta el de la app con los de todas sus bibliotecas. Una actividad, un permiso o un `<meta-data>` de una biblioteca acaba en la app sin que la app lo declare. Ya lo viste en la semana 32: WorkManager trae `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` y `FOREGROUND_SERVICE`. La app manda en el resultado: con `tools:node="remove"` quita lo que no quiere.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.VIBRATE" tools:node="remove" />
</manifest>
```

> 🔬 Medido en el ejercicio 01: la biblioteca pide `VIBRATE` y declara `HelpActivity`. En el manifiesto final de la app (`app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`) está la actividad, y `VIBRATE` no, porque la app lo quita.

La clave de Google Maps (`com.google.android.geo.API_KEY`) se queda en el manifiesto de la app: es de la app (Google la restringe al `applicationId` y a la firma de la app), no del mapa, y la escribe el `manifestPlaceholders` de la app.

## 6. Los tests de una biblioteca

Una biblioteca se prueba como la app, con Robolectric y `createComposeRule`, con dos diferencias:

- **No tiene `targetSdk`.** Robolectric elige el Android que simula con el `targetSdk`, y sin él usa el `compileSdk`.

  > 🔬 Medido con Robolectric 4.17 y `compileSdk = 37`: `IllegalArgumentException: Path is not a file: .../android-all-instrumented-17-robolectric-15733970-i7.jar`. La Bitácora solo tiene en la caché el `android-all` de Android 16 (semana 16). Con `testOptions.targetSdk = 36`, los tests de la biblioteca usan el mismo que la app.

- **La actividad vacía de `createComposeRule`** (`ui-test-manifest`) va en `debugImplementation` de la biblioteca, como en la app (semana 21): los tests de la biblioteca usan el manifiesto de la biblioteca.

`MapScreenTest`, en `:feature:map`, prueba la pantalla sin la app: le pasa dos observaciones y una lambda, y mira los textos. Sin Hilt, sin la base de datos y sin `MainActivity`.

## 7. Un convention plugin para la app y las bibliotecas

La app y las bibliotecas comparten casi todo: `compileSdk`, `minSdk`, Kotlin con los avisos como errores y Robolectric con su `android-all`. Lo común va en `bitacora.android-base`, que se aplica después del plugin de AGP:

```kotlin
// bitacora.android-library.gradle.kts
plugins {
    id("com.android.library")
    id("bitacora.android-base")
}

android {
    testOptions.targetSdk = 36
}
```

`bitacora.android-base` no sabe si está en la app o en una biblioteca. Configura `CommonExtension`, la parte común de los dos `android { }`:

```kotlin
extensions.configure<CommonExtension>("android") {
    compileSdk = 37
    defaultConfig.minSdk = 26
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
}
```

> 🔬 Medido con AGP 9.4.1: en `CommonExtension`, `testOptions { ... }` no compila (`None of the following candidates is applicable`). `testOptions` es una propiedad, y se configura con `testOptions.unitTests...`. Y `kotlin { }` no tiene accesor en un plugin que no aplica Kotlin en su bloque `plugins`: se configura con `extensions.configure<KotlinAndroidProjectExtension>("kotlin")`.

## 8. Cada biblioteca resuelve su grafo

Lo de la teoría 02, apartado 5, pasa también aquí, y más: `:feature:map` resuelve sus dependencias sin las de la app, y Maps pide versiones viejas de AndroidX.

> 🔬 Medido: al crear `:feature:map`, la verificación de dependencias pidió 95 componentes nuevos (38 con binario, AAR o JAR): versiones viejas de `activity`, `fragment`, `lifecycle` y `savedstate` que Maps declara y que, en la app, Gradle sube a las de Compose. La biblioteca compila contra ellas; el APK lleva las de la app.

Mientras la biblioteca no use una API de una versión más nueva que la que resuelve, no se nota. Si la usara, no compilaría en la biblioteca aunque compilase en la app. El arreglo es declarar en la biblioteca la versión que necesita.

## Para recordar

- Una biblioteca de Android (`com.android.library`) no se instala: va dentro de una app, con sus clases, sus recursos y su manifiesto.
- Un namespace por módulo: es el paquete de su `R` y de su manifiesto. Dos módulos con el mismo no se juntan.
- La `R` de cada módulo solo tiene sus recursos. Desde la app, `import dev.bitacora.map.R as MapR`.
- Los recursos y los manifiestos se juntan, y la app gana: puede cambiar un texto de una biblioteca y quitar lo que trae con `tools:node="remove"`.
- Una biblioteca no tiene `targetSdk`: sus tests de Robolectric necesitan `testOptions.targetSdk`.
- Lo común a la app y a las bibliotecas va en un plugin que configura `CommonExtension`.
