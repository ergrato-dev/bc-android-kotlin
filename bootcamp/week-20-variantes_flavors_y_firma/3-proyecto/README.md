# Proyecto — La Bitácora en dos sabores, y firmada

⏱️ 4 h 30 min · Capa 19 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Cierra la fase 3.

## Qué construyes

La Bitácora tiene ahora cuatro formas de construirse, menos una que sobra. La de debug se instala al lado de la publicada. La demo, con la que enseñas la app sin salir al campo, es otra app: se llama "Bitácora demo" y arranca con tres observaciones de ejemplo. Solo se publica la de campo, firmada con tu clave de subida. La clave y sus contraseñas no están en el repositorio, y sin ellas el build sigue funcionando: el release sale sin firmar.

## Dónde trabajas

En **tu repositorio**, rama `week-20`, a partir de lo que entregaste en la semana 19. El `starter/` es la Bitácora de la semana 19 resuelta, con sus tests y con comentarios `Paso N` donde va cada cambio. En `src/test/kotlin/` vienen ya hechos `Keystore.kt`, que crea una clave de subida de usar y tirar para los tests, y `Apk.signature()`, que ejecuta `apksigner verify`.

En `src/test/kotlin/BuildTest.kt` hay un test de ejemplo que empieza en rojo: construye las dos de debug y mira sus `applicationId`. Los tests de la semana 19 empiezan en verde y, a medida que añadas los flavors, se pondrán en rojo: el paso 7 los arregla. Copia todo eso a tu repositorio.

## Pasos

1. **Los tipos de build** (`build-logic`, plugin `bitacora.android-application`): `debug` con `applicationIdSuffix = ".debug"` y `versionNameSuffix = "-debug"`. Es de toda app de la Bitácora, así que va en el plugin de convención.
2. **La firma** (el mismo plugin): la configuración `release` con las propiedades `bitacoraStoreFile`, `bitacoraStorePassword`, `bitacoraKeyAlias` y `bitacoraKeyPassword`. `release` la usa solo si está `bitacoraStoreFile`.
3. **Los flavors** (`app/build.gradle.kts`): la dimensión `environment`; `demo`, con `.demo` y `-demo`; `field`, con `isDefault = true`. Son de esta app, así que van en su build y no en el plugin.
4. **Sin `demoRelease`**: `androidComponents.beforeVariants`.
5. **Los ejemplos**:
   - `object Samples` con `observations: List<Observation>`, en `src/demo/kotlin/dev/bitacora/`, con tres observaciones (una de cada nivel, y una con ubicación);
   - el mismo objeto, vacío, en `src/field/...`;
   - en `ObservationDraftViewModel`, las observaciones de la sesión empiezan con `Samples.observations` mientras no haya ninguna guardada.
6. **Los textos de la demo**: `app_name` y `title` en `src/demo/res/values` y `src/demo/res/values-en`. Solo esos dos.
7. **Los tests que ya tenías**:
   - Ejecuta `./gradlew :app:testDemoDebugUnitTest`. ¿Cuántos fallan, y por qué?
   - Los de Robolectric dan por hecho una Bitácora vacía: son de la de campo. Muévelos a `app/src/testField/kotlin/`.
   - En `PlaceAndPhotoTest`, la `Uri` de la foto ahora es `content://dev.bitacora.agro.debug.files/...`.
   - En `BuildTest`: el APK de debug es `app/build/outputs/apk/field/debug/app-field-debug.apk` (`:app:assembleFieldDebug`), lint es `:app:lintFieldDebug`, y la autoridad del `FileProvider` y el permiso de `androidx.core` llevan el sufijo.
8. **Los tests de la demo**, en `app/src/testDemo/kotlin/`.
9. **La verificación**: el comando de la semana 19 con las tareas nuevas (`:app:testDemoDebugUnitTest`, `:app:testFieldDebugUnitTest` y `:app:lintFieldDebug`) y `git diff --stat gradle/verification-metadata.xml`. ¿Cambia algo? ¿Por qué?
10. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**:
    - la demo arranca con tres observaciones y su resumen,
    - la siguiente observación de la demo es la `A-4`,
    - el título de la demo, en español y en inglés,
    - el `packageName` de la demo,
    - en `BuildTest`, no hay `demoRelease`,
    - en `BuildTest`, `check` mira `fieldDebug` y no `demoDebug` (`--dry-run`),
    - en `BuildTest`, el release firmado con la clave de `Keystore` (`-P`), con su DN en `apksigner`.
11. **Tu clave de subida**:
    - Créala con `keytool` fuera del repositorio, y añade `*.jks` y `*.keystore` a `.gitignore`.
    - Pon las cuatro propiedades en `~/.gradle/gradle.properties`.
    - Construye `:app:assembleFieldRelease` y comprueba la firma con `apksigner verify --print-certs`.
    - `git status` no puede mostrar ni la clave ni las contraseñas.
12. **Explica en tu README** qué variantes tiene la Bitácora y para qué es cada una, por qué los tests de Robolectric están en `testField`, y qué harías si perdieras la clave de subida.
13. **Abre el pull request** `week-20` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, tres variantes, `field` por defecto, la demo con sus textos en los dos idiomas, ni la clave ni sus contraseñas en el repositorio, el release sin firmar si no están, y tests con valores calculados a mano.

## Pistas

- Los tests de `src/test` se ejecutan en las dos variantes de debug. Uno que solo vale para una va en `src/testDemo` o `src/testField` (teoría 03).
- `./gradlew test --tests ...` no funciona: `test` también llama a `:app:test`, que solo agrupa. Usa `:app:testFieldDebugUnitTest --tests ...` para la app, y `:test --tests 'BuildTest'` para los de TestKit.
- En el build de TestKit, una propiedad se pasa como `-PbitacoraStoreFile=...`. `Keystore.properties()` te da las cuatro.
- `Project` usa `~/.gradle` como carpeta de TestKit, así que el build de los tests lee tu `~/.gradle/gradle.properties`. Con tu clave ahí, sus `assembleFieldRelease` salen firmados con ella, salvo que el test pase sus `-P`, que ganan.
- La caché de configuración vuelve a calcularse cuando cambia una propiedad de la firma: es una entrada de la configuración (teoría 05).
