# Anatomía de una app

> Una app Android es un módulo con el plugin `com.android.application`, un manifiesto que dice qué hay dentro y al menos una actividad que el lanzador sabe abrir. El resultado del build es un APK: un zip que puedes abrir e inspeccionar como cualquier otro artefacto.

## 🎯 Objetivos

- Configurar un módulo de aplicación: `namespace`, `applicationId`, versión
- Escribir el manifiesto mínimo de una app que aparece en el lanzador
- Abrir un APK con `apkanalyzer` y saber qué es cada cosa

## 1. El módulo `:app`

```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

Dos nombres que se parecen y no son lo mismo:

- **`namespace`**: el paquete de Kotlin donde AGP genera sus clases (`R`, `BuildConfig`) y contra el que se resuelven los nombres cortos del manifiesto (`.MainActivity` es `dev.bitacora.MainActivity`). Es cosa del código.
- **`applicationId`**: la identidad de la app en el móvil y en Google Play. Dos apps con el mismo `applicationId` son la misma app; cambiarlo después de publicar es publicar otra. En la Bitácora lleva tu dominio.

Y dos versiones: **`versionCode`** es un entero que solo sube (Android lo usa para saber si una instalación es una actualización); **`versionName`** es el texto que ve el usuario.

AGP no deja seguir sin lo mínimo, y lo dice al configurar:

```text
> Android Gradle Plugin: project ':app' does not specify `compileSdk` in build.gradle.kts (...)
> Namespace not specified. Specify a namespace in the module's build file: (...)
```

> 🔬 Medido en AGP 9.4.1: el primero sale con el `starter` del ejercicio 01 tal cual; el segundo, con `compileSdk` y sin `namespace`.

## 2. El manifiesto

`src/main/AndroidManifest.xml` declara los componentes de la app. El mínimo para que aparezca en el lanzador:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:label="@string/app_name">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- El `intent-filter` con `MAIN` y `LAUNCHER` es lo que hace que el lanzador ponga un icono para esa actividad.
- `android:exported="true"` dice que otras apps (el lanzador es otra app) pueden abrirla. Desde Android 12, quien tiene `intent-filter` lo tiene que declarar, y con `targetSdk ≥ 31` el build no sigue sin él:

```text
> Manifest merger failed : android:exported needs to be explicitly specified for element <activity#dev.bitacora.MainActivity>. Apps targeting Android 12 and higher are required to specify an explicit value for `android:exported` when the corresponding component has an intent filter defined.
```

> 🔬 Medido quitando `android:exported` del manifiesto de la Bitácora.

Tu manifiesto no es el que va en el APK. AGP lo **fusiona** (*manifest merger*) con el de cada biblioteca y añade lo que sale del build: `package` (el `applicationId`), las versiones y los niveles de SDK. Por eso el error dice `Manifest merger failed`.

## 3. Una actividad

```kotlin
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Bitácora" })
    }
}
```

Una **actividad** es una pantalla. Android la crea y llama a `onCreate`; tú no escribes `main`. Esta semana es lo justo para ver algo en el móvil. El ciclo de vida completo es otra semana de la fase 3, y las interfaces de verdad llegan con Compose en la fase 4.

## 4. El APK por dentro

```bash
./gradlew :app:assembleDebug
apkanalyzer files list app/build/outputs/apk/debug/app-debug.apk
```

| Entrada | Qué es | De dónde sale |
| --- | --- | --- |
| `classes.dex`, `classes2.dex`... | Tu código, el de tus módulos y el de las bibliotecas, en formato Dalvik | El compilador de Kotlin y después `d8` |
| `AndroidManifest.xml` | El manifiesto fusionado, en formato binario | El *manifest merger* y `aapt2` |
| `resources.arsc` | La tabla de recursos (`@string/app_name`) | `aapt2` |
| `assets/` | Archivos que la app lee tal cual (`assets.open(...)`) | Tus fuentes y las tareas que generan assets |
| `kotlin/*.kotlin_builtins` | Metadatos de la biblioteca estándar de Kotlin | `kotlin-stdlib` |
| `META-INF/` | Firma y metadatos | La firma del build |

> 🔬 Medido con la Bitácora de esta semana: el APK de debug pesa 1 219 154 bytes y tiene 5 archivos `classes*.dex`, porque en debug cada parte se *dexea* por separado para que el build incremental sea rápido. El de release (`app-release-unsigned.apk`) pesa 881 072.

`apkanalyzer` responde a lo que preguntarías al APK: `manifest application-id`, `manifest version-name`, `manifest min-sdk`, `dex packages` (las clases que hay dentro). Los tests de los ejercicios y del proyecto lo usan así: construyen el APK y le preguntan.

## ✅ Comprueba

- [ ] Cambias `namespace` a `dev.bitacora.app`. ¿Qué tienes que tocar en el manifiesto? ¿Y si cambias `applicationId`?
- [ ] ¿Por qué `versionCode` es un entero y no `"1.0.0"`?
- [ ] ¿Qué entrada del APK cambiarías si quisieras que la app leyera un archivo de texto tal cual?
