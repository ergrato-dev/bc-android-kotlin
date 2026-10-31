# Compose en el build

> Hasta ahora la pantalla de la Bitácora era un árbol de vistas: creabas un `TextView`, lo guardabas en una variable y le cambiabas el texto cuando cambiaba algo. Con Compose no hay vistas que cambiar. Escribes funciones que describen la pantalla a partir de unos datos, y cuando los datos cambian, Compose vuelve a llamarlas. Para eso hace falta un plugin del compilador de Kotlin y unas bibliotecas. Esta teoría es lo que entra en el build y la primera pantalla.

## 🎯 Objetivos

- Añadir Compose a una app: el plugin del compilador, el BOM y las bibliotecas
- Saber qué falla, y cómo, si falta una pieza
- Escribir un composable y ponerlo en una actividad con `setContent`

## 1. Un plugin del compilador

Compose no es solo una biblioteca. Las funciones marcadas con `@Composable` las transforma el compilador: les añade parámetros ocultos con los que Compose sabe dónde está cada una y qué ha cambiado. Ese trabajo lo hace un plugin del compilador de Kotlin, que desde Kotlin 2.0 viene con Kotlin y lleva su misma versión:

```toml
# gradle/libs.versions.toml
[plugins]
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}
```

Sin él, el código compila contra las bibliotecas, pero el compilador no sabe qué hacer con las funciones de Compose:

> 🔬 Medido en la sonda: sin el plugin, `compileDebugKotlin` falla con `Back-end (JVM) Internal error: Couldn't inline method call: CALL 'public final fun remember ...`. No dice "falta el plugin de Compose": si ves un error interno del compilador con `remember` o `@Composable`, mira primero los plugins.

En el raíz se carga con `apply false`, como los otros plugins de Kotlin ([semana 15](../../week-15-agp_y_la_primera_app/1-teoria/03-kotlin-en-agp-9.md)).

> 🔬 Medido en la sonda: sin esa línea también compila, porque el plugin usa el de Kotlin que ya está cargado en el raíz. Se pone por lo mismo que los demás: cada plugin se carga una vez, en un solo sitio.

Con AGP 9 no hace falta nada más en el bloque `android`. En AGP 8 se activaba con `buildFeatures { compose = true }`.

> 🔬 Medido en la sonda, con AGP 9.4.1: sin `buildFeatures.compose` todo compila y los tests pasan. Con `compose = true` no cambia nada, ni avisa.

## 2. El BOM y las bibliotecas

Compose son muchas bibliotecas (`runtime`, `ui`, `foundation`, `material3`...) que se publican juntas. El BOM fija la versión de todas a la vez, como el de JUnit o el de kotlinx.coroutines ([semana 10](../../week-10-multimodulo_y_version_catalogs/1-teoria/03-version-catalogs.md)):

```kotlin
dependencies {
    implementation(platform(libs.compose.bom))           // 2026.09.00: Compose 1.12.1, Material 3 1.4.0
    implementation(libs.activity.compose)                // setContent
    implementation(libs.compose.material3)               // Text, Button, TextField...
    implementation(libs.lifecycle.viewmodel.compose)     // viewModel() dentro de un composable (teoría 05)
    implementation(libs.lifecycle.runtime.compose)       // collectAsStateWithLifecycle (teoría 05)
}
```

Las de Compose van sin versión en el catálogo: la pone el BOM. `activity-compose` y las de `lifecycle` no están en el BOM y llevan la suya. `material3` trae `ui`, `foundation` y `runtime`: no hace falta declararlas.

Un trocito de Compose ya estaba en la Bitácora sin que lo pidieras:

> 🔬 Medido con `apkanalyzer dex packages` en el starter del proyecto, todavía con vistas: el APK tiene el paquete `androidx.compose.runtime`, con `Stable` e `Immutable`. Son las anotaciones de `runtime-annotation`, que llega por `androidx.activity` 1.13 (`navigationevent`). El runtime de verdad, con el `Recomposer`, solo entra con Compose. Por eso el test de ejemplo del proyecto busca la clase `Recomposer` y no el paquete.

Compose pesa:

> 🔬 Medido con `apkanalyzer`: la app del ejercicio 01 de la semana 20 (vistas) pesa 2,6 MB en debug y 1,9 MB en release. La misma pantalla con Compose y Material 3 pesa 11,6 MB y 8,3 MB. Sin R8: lo que no se usa se quita en la fase 6.

Y Gradle necesita más memoria para convertir todo eso a DEX:

> 🔬 Medido en la sonda, sin `gradle.properties`: `assembleDebug` falla en D8 con `java.lang.OutOfMemoryError: Java heap space`. Con `org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=1g`, la de la Bitácora desde la semana 16, pasa.

## 3. La primera pantalla

Una función `@Composable` describe un trozo de pantalla. No devuelve nada: lo que pinta son las llamadas a otros composables que hace dentro. Por convención, su nombre empieza por mayúscula, como una clase:

```kotlin
@Composable
fun Greeting(name: String) {
    Column {
        Text(stringResource(R.string.hello, name))
        Button(onClick = { /* ... */ }) {
            Text(stringResource(R.string.save))
        }
    }
}
```

- `Column` pone sus hijos uno debajo de otro, como un `LinearLayout` vertical. Los layouts son la semana 22.
- `Text` pinta un texto; `Button` es un botón cuyo contenido es otro composable.
- `stringResource` es el `getString` de Compose: lee el recurso en el idioma del móvil, como en la [semana 17](../../week-17-recursos_y_configuraciones/1-teoria/03-idiomas.md).

La actividad ya no hace `setContentView` con vistas. Hace `setContent`, de `activity-compose`, con el composable de la pantalla:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DraftScreen() }
    }
}
```

La actividad sigue siendo la de siempre: tiene su ciclo de vida, sus permisos y sus resultados ([semanas 16 a 19](../../week-16-actividades_y_ciclo_de_vida/README.md)). Lo que cambia es quién pinta.

Android Studio puede enseñar un composable sin ejecutar la app, con `@Preview` y la biblioteca `ui-tooling-preview`. Es una ayuda del editor, no del build: en este bootcamp, lo que se ve se comprueba con tests ([teoría 04](04-el-estado-baja-los-eventos-suben.md)).

## ✅ Comprueba

- [ ] ¿Qué hace el plugin del compilador de Compose que no puede hacer una biblioteca? ¿Qué versión lleva?
- [ ] ¿Por qué las bibliotecas de Compose van sin versión en el catálogo, y `activity-compose` no?
- [ ] Ves `Couldn't inline method call: CALL 'public final fun remember'`. ¿Qué miras primero?
