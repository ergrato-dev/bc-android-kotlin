# Tipos de build

> Desde la semana 15 tu app tiene dos versiones sin que hayas escrito nada: `debug` y `release`. Son tipos de build, cada uno con su forma de construir la app. Hasta ahora eran casi iguales. Esta semana se distinguen de verdad: la de debug se instala al lado de la publicada, dice en su versión que es de debug, y cada una se firma con su clave.

## 🎯 Objetivos

- Saber qué cambia de `debug` a `release` sin que lo configures
- Instalar la de debug al lado de la de release, con `applicationIdSuffix`
- Distinguir `namespace` y `applicationId`

## 1. Los dos de siempre

AGP crea siempre `debug` y `release`. Ya los usabas: `assembleDebug` construye uno, `assembleRelease` el otro. Sin configurar nada, se diferencian así:

| | `debug` | `release` |
| --- | --- | --- |
| Se puede depurar (`debuggable`) | sí | no |
| Firma | la clave de debug de tu máquina | ninguna, si no se la das |
| `lintVital` (los errores graves de lint paran el build) | no | sí |
| Nombre del APK, sin flavors | `app-debug.apk` | `app-release-unsigned.apk` |

> 🔬 Medido en la sonda: `apkanalyzer manifest debuggable` da `true` en debug y `false` en release. Sin firma configurada, el release sale `-unsigned` (`app-field-release-unsigned.apk`, con los flavors de la teoría 02). La firma es la [teoría 05](05-firma.md).

`release` también es donde se activa R8, que reduce y ofusca el código (`isMinifyEnabled`). Lo verás en la fase 6, con los tests que comprueban que no rompe nada.

## 2. Al lado de la publicada

Android identifica una app por su `applicationId`. Dos APK con el mismo son la misma app: instalar uno sustituye al otro, si están firmados con la misma clave, o falla, si no. Para tener la de debug y la publicada en el mismo móvil, la de debug lleva otro:

```kotlin
buildTypes {
    debug {
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
    }
}
```

> 🔬 Medido en la sonda: `apkanalyzer manifest application-id` da `dev.bitacora.agro.debug` en debug y `dev.bitacora.agro` en release, y `version-name` da `1.0.0-debug` y `1.0.0`.

Los dos sufijos se añaden al `applicationId` y al `versionName` de `defaultConfig`. El código no cambia.

## 3. `namespace` no es `applicationId`

Son dos cosas distintas desde AGP 7, y con los sufijos se nota:

- `namespace` (`dev.bitacora`) es el paquete de Kotlin de `R` y `BuildConfig`. No cambia con la variante: el código siempre importa `dev.bitacora.R`.
- `applicationId` (`dev.bitacora.agro.debug`) es la identidad de la app en el móvil y en Google Play. Es lo que devuelve `packageName` en ejecución.

> 🔬 Medido en la sonda: en `fieldDebug`, `packageName` y `BuildConfig.APPLICATION_ID` son `dev.bitacora.agro.debug`, y las clases siguen en `dev.bitacora`.

Todo lo que en la app depende de su identidad tiene que salir del `applicationId`, no escribirse a mano. En el manifiesto, `${applicationId}`; en el código, `packageName`. La autoridad del `FileProvider` de la semana 19 es el caso típico:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.files"
    ... />
```

> 🔬 Medido en la sonda: con `android:authorities="dev.bitacora.agro.files"` escrito a mano y el código usando `"$packageName.files"`, los tests de la foto fallan con `IllegalArgumentException: Couldn't find meta-data for provider with authority dev.bitacora.agro.debug.files`.

AGP también lo usa donde hace falta. El permiso de firma que `androidx.core` añade a la app (semana 19) lleva el `applicationId` de la variante:

> 🔬 Medido en el proyecto: en `fieldDebug`, `apkanalyzer manifest permissions` da `dev.bitacora.agro.debug.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, no `dev.bitacora.agro.DYNAMIC_...`.

Si la app de debug y la publicada declarasen la misma autoridad, la segunda no se podría instalar: dos apps no pueden tener el mismo proveedor (documentación oficial, `INSTALL_FAILED_CONFLICTING_PROVIDER`).

## ✅ Comprueba

- [ ] ¿Qué dos cosas hace `applicationIdSuffix = ".debug"`, y cuál no hace?
- [ ] ¿Por qué `R` sigue en `dev.bitacora` si el `applicationId` es `dev.bitacora.agro.debug`?
- [ ] ¿Qué pasa con una autoridad de `FileProvider` escrita a mano cuando añades un sufijo?
