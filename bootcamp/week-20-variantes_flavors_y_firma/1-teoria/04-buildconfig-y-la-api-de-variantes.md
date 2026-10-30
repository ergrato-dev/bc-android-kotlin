# BuildConfig y la API de variantes

> A veces la diferencia entre variantes es un dato, no una clase: la URL del servidor, si se envían informes de errores. Para eso AGP genera `BuildConfig`, una clase con constantes de la variante. Y cuando la diferencia es una decisión del build (qué variantes existen, qué genera cada una), está la API de variantes, `androidComponents`.

## 🎯 Objetivos

- Activar `BuildConfig` y añadirle campos por flavor
- Saber qué campos trae siempre y cuáles solo con flavors
- Elegir entre `beforeVariants` y `onVariants`

## 1. `BuildConfig`

En AGP 9, `BuildConfig` está apagada. Si declaras un campo sin activarla, el build lo dice:

> 🔬 Medido en la sonda: `Product Flavor 'field' contains custom BuildConfig fields, but the feature is disabled. To enable the feature, add the following to your module-level build.gradle: android.buildFeatures.buildConfig = true`.

```kotlin
android {
    buildFeatures {
        buildConfig = true
    }
    productFlavors {
        create("demo") {
            dimension = "environment"
            buildConfigField("String", "SERVER_URL", "\"https://demo.bitacora.example\"")
        }
        create("field") {
            dimension = "environment"
            buildConfigField("String", "SERVER_URL", "\"https://api.bitacora.example\"")
        }
    }
}
```

El tercer argumento es **código Java**, tal cual: para una cadena hacen falta las comillas dentro de las comillas. AGP escribe con eso la clase `dev.bitacora.BuildConfig` (el `namespace`), y el código la usa como cualquier constante: `BuildConfig.SERVER_URL`.

> 🔬 Medido en el ejercicio 01: sin las comillas de dentro, `BuildConfig.java` tiene `SERVER_URL = https://api.bitacora.example;`, y el build falla en `compileFieldDebugJavaWithJavac` con `error: ';' expected`. La compilación de Kotlin pasa: no lee el cuerpo de las clases de Java.

Un campo tiene que existir en todos los flavors, o no compila en los que falta:

> 🔬 Medido en la sonda: con `SAMPLES` solo en `field`, `compileDemoDebugKotlin` falla con `Unresolved reference 'SAMPLES'`.

## 2. Lo que trae siempre

Sin campos tuyos, `BuildConfig` tiene `DEBUG`, `APPLICATION_ID`, `BUILD_TYPE`, `VERSION_CODE` y `VERSION_NAME`, ya con los sufijos:

> 🔬 Medido en la sonda: en `demoDebug`, `BuildConfig.DEBUG` es `true`, `APPLICATION_ID` es `dev.bitacora.agro.demo.debug` y `VERSION_NAME` es `1.0.0-demo-debug`. La clase está en `app/build/generated/source/buildConfig/demo/debug/dev/bitacora/BuildConfig.java`, y la de `fieldDebug` tiene también `FLAVOR = "field"` y `BUILD_TYPE = "debug"`.

`FLAVOR` solo existe si hay flavors:

> 🔬 Medido en el ejercicio 01: sin flavors, `BuildConfig.FLAVOR` da `Unresolved reference 'FLAVOR'`. Por eso los tests del ejercicio lo leen por reflexión.

## 3. ¿Campo, recurso o clase?

| Diferencia | Dónde |
| --- | --- |
| Un dato que lee el código (URL, interruptor) | `buildConfigField` |
| Un texto, un color, un icono | Recurso en la carpeta del flavor ([teoría 03](03-source-sets-por-variante.md)) |
| Un comportamiento (datos de ejemplo, un servidor falso) | Clase en la carpeta de cada flavor |

Un `if (BuildConfig.FLAVOR == "demo")` en `main` funciona, pero mete el código de la demo en el APK de campo. Una clase en `src/demo` no llega a la app publicada.

Un secreto (una clave de API) en `buildConfigField` no está a salvo: queda en el código del APK, y cualquiera puede leerlo. Eso no es una forma de esconderlo (documentación oficial).

## 4. `beforeVariants` y `onVariants`

`androidComponents` tiene dos momentos:

- `beforeVariants` actúa sobre la descripción de la variante, antes de crearla: si existe (`enable`), su `minSdk`, si tiene tests de la JVM.
- `onVariants` actúa sobre la variante ya creada: sus fuentes, sus assets, sus campos de `BuildConfig`, su manifiesto. Lo que se añade aquí puede salir de una tarea (`Provider`), y Gradle la ejecuta cuando hace falta.

```kotlin
androidComponents {
    beforeVariants(selector().withFlavor("environment" to "demo").withBuildType("release")) { it.enable = false }
    onVariants(selector().withBuildType("debug")) { variant ->
        // variant.name: "demoDebug", "fieldDebug"
    }
}
```

El plugin de `build-info` de la semana 12 usa `onVariants` para añadir a cada variante el archivo que genera su tarea. Ahora lo hace dos veces, una por flavor.

`selector()` elige variantes por flavor, por tipo de build o por nombre (`withName`). Sin selector, el bloque se aplica a todas.

## ✅ Comprueba

- [ ] ¿Por qué `buildConfigField("String", "SERVER_URL", "https://api.bitacora.example")` no compila?
- [ ] ¿Por qué es mejor una clase en `src/demo` que un `if` con `BuildConfig.FLAVOR`?
- [ ] ¿Con cuál de los dos, `beforeVariants` u `onVariants`, quitas una variante? ¿Por qué no con el otro?
