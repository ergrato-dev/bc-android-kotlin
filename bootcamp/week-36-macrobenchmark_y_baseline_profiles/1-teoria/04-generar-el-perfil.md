# Generar el Baseline Profile

> El perfil de la Bitácora no se escribe: se graba. Un test recorre la app en un dispositivo como lo haría una persona, ART apunta qué clases y métodos han hecho falta, y el plugin copia la lista al código de la app. Esta teoría monta el generador, lo ejecuta en un emulador que Gradle gestiona solo, y explica dónde queda el perfil y qué hacer con él.

## 🎯 Objetivos

- Escribir un generador con `BaselineProfileRule.collect`
- Conectar el módulo productor y la app consumidora con el plugin
- Generar el perfil en un Gradle Managed Device con `generateBaselineProfile`
- Saber dónde queda, por qué va al repositorio y cuándo regenerarlo

## 1. El generador

```kotlin
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun addAndScroll() = rule.collect(packageName = PACKAGE) {
        pressHome()
        startActivityAndWait()
        fill(40)
        scrollTheList()
    }
}
```

Va en el mismo módulo que los benchmarks, y usa las mismas funciones (`fill`, `scrollTheList`): el recorrido que se mide es el que se optimiza. `collect` repite el bloque varias veces y junta lo usado. `includeInStartupProfile` es para la teoría 05.

> 🔬 Medido: en `connectedBenchmarkReleaseAndroidTest`, `generate` no se ejecuta. Sale como saltado, con `org.junit.AssumptionViolatedException: got: <false>, expected: is <true>` en `BaselineProfileRule.kt:54`. Solo corre en la variante `nonMinifiedRelease`, la que lanza el plugin. Y al revés: los benchmarks se saltan al generar.

## 2. El plugin, a los dos lados

| Dónde | Qué pone | Para qué |
| --- | --- | --- |
| `:baselineprofile` (productor) | `alias(libs.plugins.baselineprofile)` | Ejecuta el generador y recoge el resultado |
| `:app` (consumidor) | `alias(libs.plugins.baselineprofile)` y `baselineProfile(project(":baselineprofile"))` | Recibe el perfil y lo guarda en su código |

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.profileinstaller)
    baselineProfile(project(":baselineprofile"))
}
```

## 3. En un emulador que Gradle gestiona

El generador necesita un dispositivo. Con un Gradle Managed Device (semana 35), Gradle lo crea, lo arranca y lo apaga, y el perfil sale igual en cualquier máquina:

```kotlin
// baselineprofile/build.gradle.kts
android {
    testOptions.managedDevices.localDevices.create("pixel6Api36") {
        device = "Pixel 6"
        apiLevel = 36
        systemImageSource = "google"
        testedAbi = "x86_64"
    }
}

baselineProfile {
    managedDevices += "pixel6Api36"
    useConnectedDevices = false
}
```

- `systemImageSource = "google"` es una imagen con los servicios de Google, la que ya tenía el SDK (`system-images;android-36;google_apis;x86_64`).
- `useConnectedDevices = false`: el emulador gestionado, no el móvil que haya enchufado.
- API 33 o más: `BaselineProfileRule` no necesita root. Con 28 a 32, sí.

> 🔬 Medido con AGP 9.4.1: sin `testedAbi`, `pixel6Api36Setup` avisa: `The device "pixel6Api36" does not specify a "testedAbi". This currently defaults to "x86_64", but will change to "arm64-v8a" in AGP 10.0.` Lo mismo en el GMD de la app (semana 35), y se arregla igual.

```bash
./gradlew :app:generateBaselineProfile
```

> 🔬 Medido en esta máquina (WSL2 con KVM): 9 min 7 s la primera vez, con la creación del emulador incluida, y 9 min 56 s la segunda, con los dos recorridos. Por eso no se genera en cada `build`: `automaticGenerationDuringBuild` está apagado por defecto.

## 4. Dónde queda

```
app/src/fieldRelease/generated/baselineProfiles/
├── baseline-prof.txt     25 879 líneas, 777 de dev/bitacora
└── startup-prof.txt      20 737 líneas, 561 de dev/bitacora
```

- `fieldRelease`: es un perfil por variante, y la única release es la de field.
- Va en `src/`, con el código (`saveInSrc`, encendido por defecto), y **va al repositorio**, como las capturas de la semana 35. El build no necesita un dispositivo para usarlo, y un cambio en el perfil se ve en el diff.
- Tiene reglas de todas las bibliotecas que usó el recorrido: Compose (14 416 líneas), coroutines (1 622), Ktor (1 317), WorkManager (1 172), Room...

> 🔬 Medido: con un solo `collect` con `includeInStartupProfile = true`, los dos archivos salieron idénticos (20 659 líneas). Con el recorrido de la lista en un `collect` aparte, el de arranque se queda en lo del arranque.

## 5. Cuándo regenerarlo

El perfil nombra clases y métodos. Si cambian, sus reglas ya no encuentran nada:

| Cambio | ¿Regenerar? |
| --- | --- |
| Una pantalla nueva en el recorrido, un método que se renombra | Sí |
| Subir Compose o Room | Sí: sus clases cambian |
| Cambiar un texto o un color | No |
| Cada versión que se publica | Lo normal: antes de publicar, en CI con un GMD |

Un perfil viejo no para el build: lo que ya no existe deja de ayudar. Con el perfil de arranque, D8 lo avisa (teoría 05).

## ⚠️ Errores comunes

| Error | Qué pasa | Arreglo |
| --- | --- | --- |
| Generar en el móvil conectado sin querer | El perfil depende de qué haya enchufado | `useConnectedDevices = false` y un GMD |
| `build/` en lugar de `src/` | El perfil se pierde con `clean`, y nadie lo revisa | `saveInSrc`, y al repositorio |
| Un recorrido distinto del que se mide | Se optimiza lo que nadie mide | Las mismas funciones en el generador y los benchmarks |
| Esperar que `build` lo genere | Tarda nueve minutos y necesita KVM | `./gradlew :app:generateBaselineProfile` a propósito |

## 📝 Resumen

- `BaselineProfileRule.collect` recorre la app y apunta lo usado.
- El plugin va en el productor y en la app; `baselineProfile(project(...))` los une.
- `generateBaselineProfile` lo hace en un GMD, igual en cualquier máquina.
- Queda en `src/fieldRelease/generated/baselineProfiles` y va al repositorio.
- Se regenera cuando cambia el código del recorrido o sus bibliotecas.

## 🔗 Recursos

- [Crear Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile)
- [El plugin de Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/configure-baselineprofiles)
- [Gradle Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices)
