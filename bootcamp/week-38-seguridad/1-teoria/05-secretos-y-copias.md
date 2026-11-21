# Secretos y copias de seguridad

> Hay dos sitios por donde se escapa lo que no debería: el repositorio y las copias de seguridad. Una clave escrita en `strings.xml` la tiene cualquiera que clone el repositorio, y un token en `files/` viaja a la copia de Google y al móvil nuevo. Esta teoría explica qué secretos puede llevar una app (pocos), de dónde llegan al build sin pasar por git, y cómo se decide qué va en una copia.

## 🎯 Objetivos

- Distinguir un secreto del repositorio de un secreto de la app
- Pasar una clave al build con una propiedad de Gradle, sin escribirla en el repositorio
- Saber que todo lo que va en el APK es público
- Excluir de las copias lo que no se puede restaurar en otro móvil

## 1. Todo lo del APK es público

Un APK es un zip. Quien lo descarga ve los recursos, el manifiesto y el código, aunque R8 haya cambiado los nombres:

🔬 En el ejercicio 02, con la clave pasada por propiedad (`./gradlew assembleRelease -PalertsKey=ak_live_PRUEBA`):

```
$ apkanalyzer resources value --type string --config default --name alerts_key app-release-unsigned.apk
ak_live_PRUEBA
```

No hay forma de esconder un secreto dentro de una app: ni ofuscándolo, ni partiéndolo, ni en código nativo. Así que la pregunta no es dónde se esconde, sino **qué clave se puede publicar**:

| Clave | ¿En la app? |
| --- | --- |
| La de Maps, restringida en la consola de Google a `dev.bitacora.agro` y a la huella SHA-1 de su firma | Sí: es pública por diseño, y solo vale para tu app |
| Una que identifica a la app ante tu servidor (como `alerts_key`) | Sí, sabiendo que es un identificador, no una contraseña |
| La de un servicio de pago, de la base de datos, de la nube | No. La app habla con tu servidor, y tu servidor con el servicio |
| El token de una persona (el de la Bitácora) | No va en el APK: llega al vincular y se guarda cifrado (teoría 01) |

## 2. Fuera del repositorio

Aunque la clave acabe en el APK, no va en git: el repositorio lo ven más personas que la consola donde se restringe, y una clave en el historial no se borra con un commit. Las de la Bitácora llegan al build por **propiedades de Gradle**:

```kotlin
// app/build.gradle.kts
defaultConfig {
    manifestPlaceholders["mapsApiKey"] = providers.gradleProperty("mapsApiKey").getOrElse("")
}
```

| Dónde se define | Para quién |
| --- | --- |
| `~/.gradle/gradle.properties` | Tu máquina, para todos los proyectos. Fuera del repositorio |
| `-PmapsApiKey=…` | Una ejecución |
| `ORG_GRADLE_PROJECT_mapsApiKey` | Una variable de entorno: los secretos de CI |

Sin la propiedad, vacía: la app compila y el mapa no carga. Nadie necesita la clave para compilar ni para pasar los tests.

En el ejercicio 02 la clave va a un recurso, con `resValue`:

```kotlin
buildFeatures { resValues = true }
defaultConfig {
    resValue("string", "alerts_key", providers.gradleProperty("alertsKey").getOrElse(""))
}
```

🔬 Sin `resValues = true`, AGP 9 no configura el proyecto: `defaultConfig contains custom resource values, but the feature is disabled.` (Como `BuildConfig`, está apagado por defecto.)

`local.properties` también sirve (y ya está en el `.gitignore`), pero Gradle no lo lee solo: hay que abrirlo a mano en el build. Las propiedades de Gradle no necesitan código.

Si una clave llega a git: se **revoca** y se crea otra. Reescribir el historial no sirve: alguien ya pudo clonarlo. `git log -S ak_live_` encuentra el commit donde entró.

## 3. Las copias de seguridad

Android hace copias de la app sin que la app haga nada (`android:allowBackup` es `true` por defecto):

| Copia | Cuándo |
| --- | --- |
| La de Google (*cloud backup*) | Periódicamente, cifrada con el bloqueo de pantalla de la persona |
| El paso a otro móvil (*device transfer*) | Al configurar un móvil nuevo desde el viejo |

Se llevan `files/`, `databases/`, las `shared_prefs/` y DataStore. Para las observaciones es justo lo que se quiere: la persona cambia de móvil y su Bitácora sigue ahí.

Para el token, no. Lo cifra una clave del Keystore, y el Keystore no va en las copias: en el móvil nuevo, `token.bin` llega pero no se puede abrir. `TokenStore.read()` devuelve `null` y la app pide vincular otra vez; no falla, pero llevar el archivo no sirve de nada. Se excluye:

```xml
<!-- res/xml/data_extraction_rules.xml: Android 12 o más -->
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="file" path="token.bin" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="file" path="token.bin" />
    </device-transfer>
</data-extraction-rules>
```

```xml
<!-- res/xml/backup_rules.xml: Android 11 o menos (minSdk 26) -->
<full-backup-content>
    <exclude domain="file" path="token.bin" />
</full-backup-content>
```

```xml
<application
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules">
```

Con `targetSdk` 31 o más, Android 12 lee el primero y no el segundo; los dos hacen falta mientras `minSdk` sea menor de 31. `SecurityTest.theTokenStaysOutOfBackups` comprueba que el token está excluido en los tres sitios.

`android:allowBackup="false"` también lo resuelve, pero se lleva por delante las observaciones. Se excluye lo que no debe viajar, no todo.

Esto es lo que rompía tantas apps con `EncryptedSharedPreferences`: el archivo cifrado viajaba en la copia, la clave del Keystore no, y al abrirlo en el móvil nuevo, la app fallaba al arrancar.

## ⚠️ Errores comunes

| Error | Por qué |
| --- | --- |
| Una clave en `strings.xml`, `BuildConfig` o un `const val`, en el repositorio | La ve todo el que lo clone, y queda en el historial |
| «Ofuscar» una clave para que no se vea en el APK | Se ve igual: en el APK no hay secretos |
| La clave de un servicio de pago en la app | Cualquiera la usa a tu cargo |
| Borrar del repositorio una clave filtrada sin revocarla | Sigue en el historial y en los clones |
| Solo `fullBackupContent` con `targetSdk` 31 o más | Android 12 o más no lo lee |
| `allowBackup="false"` para quitar un archivo | La persona pierde todo lo demás al cambiar de móvil |

## 🧪 Comprueba que lo entendiste

1. ¿Por qué no se puede esconder un secreto en un APK? ¿Qué claves pueden ir en él?
2. ¿De dónde llega la clave de Maps al manifiesto? ¿Qué pasa si no está?
3. Una clave llegó a `main`. ¿Qué haces, y en qué orden?
4. ¿Qué pasaría con el token si viajara en la copia? ¿Por qué no se excluyen también las observaciones?

---

← [04 · Componentes exportados y PendingIntent](04-componentes-exportados-y-pendingintent.md) · Siguiente: [06 · Las dependencias y Play Integrity](06-dependencias-y-play-integrity.md)
