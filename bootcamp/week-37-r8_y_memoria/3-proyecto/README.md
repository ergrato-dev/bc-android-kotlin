# Proyecto — La Bitácora reducida

⏱️ 5 h · Capa 36 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La semana 36 midió la Bitácora en release, pero esa release lleva todo lo que traen sus bibliotecas: 13,2 MB de APK y 4 dex. Esta semana:

- **R8 en la release**, con código y recursos, para toda app de la Bitácora (el plugin de convención). Sin una regla propia, y sabiendo por qué no hace falta.
- **Lo que deja R8**, leído: de dónde llega cada regla, qué conserva, qué quita, y una traza de la release devuelta a sus nombres con `retrace`.
- **LeakCanary** en la debug y en `DeviceTest`, que falla si un test deja una fuga.
- **StrictMode** en la debug, y lo que encuentra.
- **Números**: el APK, los dex y la memoria, antes y después.

```
bitacora.android-application (build-logic)        app
  release: isMinifyEnabled · isShrinkResources      proguard-rules.pro (vacío, a propósito)
  proguard-android-optimize.txt                     BitacoraApp: StrictMode en debug
                                                    debugImplementation(leakcanary-android)
build/outputs/mapping/fieldRelease                  DeviceTest: DetectLeaksAfterTestSuccess
  mapping.txt · seeds.txt · usage.txt · configuration.txt
```

## Dónde trabajas

En **tu repositorio**, rama `week-37`, a partir de lo que entregaste en la semana 36. El `starter/` es la Bitácora de la semana 36 resuelta. Trae ya:

- en `gradle/libs.versions.toml`, LeakCanary 2.14 (`libs.leakcanary.android`, `libs.leakcanary.android.instrumentation`);
- en `gradle/verification-metadata.xml`, lo que traen: 17 componentes nuevos, todos de Square (`leakcanary-*`, `shark-*`, `plumber-*`, `curtains`), revisados en OSV, sin avisos.

Y un test nuevo:

- `src/test/kotlin/ShrinkTest.kt`, en la raíz, con TestKit: que la release pasa por R8 y queda por debajo de 4 MB, que la debug no, que los benchmarks miden la reducida, que `SyncWorker` conserva su nombre y lo demás no, que una línea del mapping se recupera con `retrace`, y que LeakCanary solo está en debug.

Con la Bitácora de la semana 36 y estos cambios, fallan 5 de los 6 de `ShrinkTest`. Los demás de la raíz y los de los módulos siguen en verde. Copia esos archivos a tu repositorio.

**Para el paso 9 hace falta un dispositivo** (o KVM para el emulador). Sin él, todo lo demás se hace igual.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'ShrinkTest'`. ¿Por qué ya pasa `theDebugIsNotShrunk`?
2. **Mira antes de tocar**: `./gradlew :app:assembleFieldRelease` y, con `apkanalyzer` (teoría 05), `apk file-size`, `apk download-size` y `dex references` del APK de field. Guarda una copia del APK fuera de `build/`: la usarás en el paso 7.
3. **R8**: en `bitacora.android-application`, `isMinifyEnabled`, `isShrinkResources` y `proguardFiles` en `release` (teoría 01, apartado 2). `./gradlew :app:assembleFieldRelease`. Lee el error y crea lo que falta, con un comentario que diga por qué está vacío (cuando hayas hecho el paso 5).
4. **¿Funciona?** Que compile no basta (teoría 02). Con un dispositivo, `./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`: `benchmarkRelease` pasa ahora por R8, y los benchmarks añaden 40 observaciones y recorren la lista. Sin dispositivo, `ShrinkTest.theBenchmarksMeasureTheShrunkApp` comprueba al menos que lo que se mide es lo reducido.
5. **Lo que ha hecho R8**, en `app/build/outputs/mapping/fieldRelease/`:
   - `configuration.txt`: ¿cuántos bloques tiene? ¿Cuántos vienen de AAR y cuántos de JAR? Busca los de WorkManager, Room, Hilt, Ktor y kotlinx.serialization: ¿qué conserva cada uno? (Teoría 03, apartado 3.)
   - `mapping.txt`: ¿con qué nombre ha quedado `SyncWorker`? ¿Y `BitacoraViewModel`? ¿Qué regla conserva el primero, y por qué la necesita?
   - `usage.txt` y `seeds.txt`: busca `AppModule`. ¿Dónde está, y por qué?
   - `configanalyzer.html`: ábrelo en el navegador. ¿Qué regla conserva más?
6. **retrace** (teoría 04): en una rama aparte que **no** subes, mete un `error("…")` en una acción de la app, instala `benchmarkRelease`, provoca el fallo y copia la traza de `adb logcat -b crash`. Pásala por `retrace` con el mapping de **esa** build. ¿Coincide el `pg_map_id`? ¿Sale la línea exacta? ¿Cuántas líneas tenía la traza y cuántas tiene ahora? Sin dispositivo: haz lo mismo que `aTraceCanBeRetraced`, con una línea de `onIntent` sacada del mapping.
7. **Medir** (teoría 05): repite el paso 2 con R8, y `apkanalyzer apk compare --different-only` entre los dos APK. Y `dex packages --defined-only --proguard-mappings`: ¿cuánto ocupa `dev.bitacora` frente a Compose?
8. **LeakCanary y StrictMode** (teoría 06): LeakCanary con `debugImplementation`, y `leakcanary-android-instrumentation` en los tests del dispositivo. En `DeviceTest`, `DetectLeaksAfterTestSuccess` entre la regla de Hilt y la de la actividad (`order`). StrictMode en `BitacoraApp.onCreate`, solo con `BuildConfig.DEBUG`, con `penaltyLog`. `./gradlew :test`: ¿qué test de la semana 15 falla ahora, y por qué? Arréglalo sin debilitarlo: la lista esperada tiene que decir exactamente lo que lleva la debug, con un comentario que diga de dónde sale cada permiso nuevo.
9. **Con un dispositivo**:
   - `./gradlew :app:connectedFieldDebugAndroidTest`: los dos de `DeviceTest` en verde. Mete una fuga a propósito (una lista de nivel superior con la actividad dentro), comprueba que fallan los dos, lee el informe y quítala.
   - Abre la debug, añade una observación y busca `StrictMode policy violation` en el logcat. ¿Cuántos avisos hay, de dónde salen y qué harías con cada uno?
   - `adb shell dumpsys meminfo` de la debug, de `benchmarkRelease` sin R8 (desactivándolo un momento) y con R8, 3 arranques cada una. ¿Cuánto baja R8 la memoria?
10. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:assembleFieldBenchmarkRelease :baselineprofile:assembleBenchmarkRelease :baselineprofile:assembleNonMinifiedRelease :app:assembleFieldDebugAndroidTest :app:compileFieldDebugUnitTestKotlin :app:compileDemoDebugUnitTestKotlin` y `git diff --stat gradle/verification-metadata.xml`. ¿Entra algo que no traiga el starter? Después, `./gradlew build`.
11. **Escribe al menos 5 tests más**, con lo esperado escrito **a mano** y el porqué en un comentario. Por ejemplo:
    - en `ShrinkTest`, que `BitacoraDatabase_Impl` está en `seeds.txt` y que `AppModule` está en `usage.txt`;
    - que el AAB (`bundleFieldRelease`) lleva el mapping dentro, y dónde;
    - que la release tiene 2 dex y menos de 65 536 referencias en cada uno;
    - que `configuration.txt` no tiene ninguna regla con `dev.bitacora.** { *; }`, ni de la app ni de una biblioteca;
    - en `DeviceTest`, que girar la pantalla (`ActivityScenario.recreate`) no deja fugas.
12. **Explica en tu README** qué conservan las reglas que llegan solas y por qué tu `proguard-rules.pro` está vacío; qué quita R8 en tu Bitácora (tus números de los pasos 2 y 7); qué encontraron StrictMode y LeakCanary, y qué harías con lo de StrictMode. Si no tienes dispositivo, dilo.
13. **Abre el pull request** `week-37` → `main` con la salida de `./gradlew test` y tus números.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md):

- R8 y la reducción de recursos en la release, desde el plugin de convención, y no en debug;
- ninguna regla propia sin un test que la pida, y ninguna `{ *; }`;
- el mapping leído y una traza recuperada con `retrace`;
- LeakCanary solo en debug, `DetectLeaksAfterTestSuccess` en `DeviceTest`, StrictMode en debug;
- los tests de la semana 36 en verde (el de los permisos, arreglado sin debilitarlo), sin warnings de Kotlin, `lintFieldDebug` en verde y tests con los valores esperados escritos a mano;
- los números, con su `context`.

## Pistas

- `Supplied proguard configuration does not exist: .../app/proguard-rules.pro`: lo nombra `proguardFiles` y no existe. Va en cada app, no en `build-logic`.
- `onlyTheExpectedPermissionsShip` falla tras añadir LeakCanary: es lo que tiene que pasar. Mira con `apkanalyzer manifest permissions` la debug y la release.
- Los 109 tests de Robolectric pasan igual con LeakCanary y StrictMode (medido): si falla uno, es otra cosa.
- `leakcanary.NoLeakAssertionFailedError: Application memory leaks were detected` en `DeviceTest` sin haber metido una fuga: lee el informe, es una de verdad.
- `DetectLeaksAfterTestSuccess` no encuentra la fuga que metiste: va por dentro de la regla de la actividad. Su `order` tiene que ser menor que el de `createAndroidComposeRule`.
- `retrace` devuelve nombres que no cuadran: el mapping es de otra build. Compara el `pg_map_id`.
- `minifyFieldReleaseWithR8` tarda: R8 recorre toda la app y sus bibliotecas en cada release. En debug no corre.
- La traza de `adb logcat` sale con `r8-map-id-…` donde iba el archivo: es lo normal, y es lo que dice qué mapping usar.
