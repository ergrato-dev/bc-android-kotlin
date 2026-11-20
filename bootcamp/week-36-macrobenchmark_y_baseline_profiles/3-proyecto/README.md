# Proyecto — La Bitácora medida

⏱️ 5 h · Capa 35 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora tiene más de 300 tests que dicen si hace lo que debe. Ninguno dice cuánto tarda. Esta semana:

- **Un módulo `:baselineprofile`** (`com.android.test`) con dos benchmarks: el arranque en frío, con tres compilaciones, y el recorrido de una lista de 40.
- **Un generador** que graba el Baseline Profile y el Startup Profile en un emulador que Gradle gestiona. El perfil va al repositorio.
- **`ReportDrawnWhen`**: la app avisa de que está lista cuando llega la lista, y Macrobenchmark mide `timeToFullDisplayMs`.
- **Números de verdad**, con su `context`, y una decisión: ¿mejora, o es ruido?

```
:app (field)                                        :baselineprofile (com.android.test)
  plugin androidx.baselineprofile                     plugin androidx.baselineprofile · targetProjectPath = ":app"
  profileinstaller · ReportDrawnWhen                  Bitacora.kt (fill, scrollTheList) · StartupBenchmark · ScrollBenchmark
  src/fieldRelease/generated/baselineProfiles  ◄────  BaselineProfileGenerator ── generateBaselineProfile (GMD pixel6Api36)
  benchmarkRelease ◄── connectedBenchmarkReleaseAndroidTest
```

La app cambia en cuatro sitios: `ReportDrawnWhen`, el plugin y `profileinstaller`, las variantes de demo, y el perfil generado.

## Dónde trabajas

En **tu repositorio**, rama `week-36`, a partir de lo que entregaste en la semana 35. El `starter/` es la Bitácora de la semana 35 resuelta. Trae ya:

- en `gradle/libs.versions.toml`, Macrobenchmark y el plugin de Baseline Profiles 1.5.0 (`libs.benchmark.macro.junit4`, `libs.plugins.baselineprofile`), `libs.plugins.android.test`, UiAutomator 2.4.1 (`libs.uiautomator`), profileinstaller 1.4.1 (`libs.profileinstaller`) y wire-runtime 6.4.7 (`libs.wire.runtime`);
- en `gradle/verification-metadata.xml`, lo que traen, también lo que hace falta para ejecutar en un GMD: 66 componentes nuevos, revisados en OSV. Dos tienen aviso: wire-runtime 6.4.0, que no está entero a propósito (la restricción del paso 4 la sube a 6.4.7), y okio 2.10.0, del que solo entra el POM porque se resuelve a 3.17.0.

Y un test nuevo:

- `src/test/kotlin/PerformanceTest.kt`, en la raíz, con TestKit: el módulo y a qué apunta, el perfil en el repositorio, el de arranque, que llega al APK, que `build` ensambla los benchmarks y no los de demo, que se genera en el GMD y que Macrobenchmark no llega a la app.

Con la Bitácora de la semana 35 y estos cambios, fallan 6 de los 7 de `PerformanceTest`. Los demás de la raíz (`BuildTest`, `ModulesTest`, `TestingTest`, `UiTest`) y los de los módulos siguen en verde. Copia esos archivos a tu repositorio.

**Para generar el perfil hace falta un dispositivo**: KVM para el emulador gestionado (en WSL2, con la virtualización anidada encendida y tu usuario en el grupo `kvm`), o un móvil con la depuración USB y API 33 o más. Sin ninguno de los dos, pide a tu instructor el perfil generado con la solución, y dilo en tu README.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'PerformanceTest'`. ¿Por qué pasa ya `theBenchmarkLibrariesAreNotInTheApk`, si nadie ha declarado `profileinstaller`?
2. **Mira antes de tocar**: `./gradlew :app:assembleFieldRelease` y `unzip -l app/build/outputs/apk/field/release/app-field-release-unsigned.apk | grep dexopt`. ¿Ya hay un perfil? ¿De quién es? Apunta su tamaño.
3. **El módulo**: `include(":baselineprofile")` en `settings.gradle.kts`, los dos plugins con `apply false` en el raíz, y `baselineprofile/build.gradle.kts` (teoría 02, apartado 1) con `missingDimensionStrategy("environment", "field")`.
4. **El emulador para generar**: en el módulo, el GMD `pixel6Api36` con `systemImageSource = "google"` y `testedAbi = "x86_64"`, y `baselineProfile { managedDevices += "pixel6Api36"; useConnectedDevices = false }` (teoría 04, apartado 3). Y la restricción de `wire-runtime` en sus dependencias: ¿qué la trae?
5. **Los recorridos**: `Bitacora.kt` con `PACKAGE`, `fill(count)` y `scrollTheList()`; `StartupBenchmark` con `None`, `Partial(Require)` y `Full`, 15 arranques en frío; `ScrollBenchmark` con `FrameTimingMetric`, 5 recorridos (teoría 02, apartados 3 y 4). ¿Por qué el de la lista no lleva `startupMode`?
6. **La app**: el plugin, `implementation(libs.profileinstaller)` y `baselineProfile(project(":baselineprofile"))`. Las variantes de demo, solo debug, y `baselineProfile { warnings { disabledVariants = false } }`. `testedAbi = "x86_64"` también en el GMD de la semana 35.
7. **«Ya estoy lista»**: `ReportDrawnWhen { state.loaded }` en `MainActivity`, antes del `return` (teoría 05, apartado 2). En `DeviceTest`, `itReportsFullyDrawn`. Y saca los textos de `addingShowsTheRow` de los recursos: en un dispositivo en inglés, «Descripción» no existe.
8. **El generador**: `BaselineProfileGenerator` con dos `collect`, el arranque con `includeInStartupProfile = true` y añadir y recorrer sin él. `./gradlew :app:generateBaselineProfile`. ¿Cuánto tarda? ¿Cuántas líneas tienen `baseline-prof.txt` y `startup-prof.txt`, y cuántas son de `dev/bitacora`? Haz commit de los dos.
9. **Que llegue y que compile**: el tamaño de `assets/dexopt/baseline.prof` ahora (compáralo con el del paso 2), y `./gradlew build -m | grep -E "baselineprofile:assemble|app:assemble.*Benchmark"`. ¿Sale alguna de demo?
10. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:assembleFieldBenchmarkRelease :baselineprofile:assembleBenchmarkRelease :baselineprofile:assembleNonMinifiedRelease :app:assembleFieldDebugAndroidTest :app:compileFieldDebugUnitTestKotlin :app:compileDemoDebugUnitTestKotlin` y `git diff --stat gradle/verification-metadata.xml`. ¿Entra algo que no traiga el starter? Después, `./gradlew build`.
11. **Mide** (con un dispositivo): `./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`, en un emulador con `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`. Copia el JSON a tu repositorio (`docs/benchmarks/`) y pásale la comparación del ejercicio 01: ¿qué dice de TTID, de TTFD y de los fotogramas? Copia también su `context`.
12. **Escribe al menos 5 tests más**, con lo esperado escrito **a mano** y el porqué en un comentario. Por ejemplo:
    - en `PerformanceTest`, que el perfil generado tiene reglas para `BitacoraScreenKt` y para `ObservationRow`, y que `startup-prof.txt` no tiene ninguna de `ObservationScreen` (el detalle no se abre al arrancar);
    - que el manifiesto de `app-field-benchmarkRelease.apk` tiene `profileable` con `shell="true"` y no es depurable, y que el de release no tiene `profileable`;
    - que `:app:assembleDemoBenchmarkRelease` no existe (`fail` y el mensaje);
    - un benchmark de tibio (`StartupMode.WARM`) y uno de abrir el detalle de una observación con `TraceSectionMetric` o `StartupTimingMetric`: ¿qué esperas, y cómo lo mides?;
    - en `DeviceTest`, que `fullyDrawnReporter` **no** ha avisado antes de que llegue la lista (con un repositorio que tarde).
13. **Explica en tu README** qué es el perfil que ya había antes del paso 8, qué aporta el tuyo y cómo se regenera; qué midió tu dispositivo (con su `context`) y qué decides con esos números; qué no sabes todavía (si mediste en un emulador, qué haría falta para saberlo). Si no tienes dispositivo, dilo.
14. **Abre el pull request** `week-36` → `main` con la salida de `./gradlew test`, el diff del perfil generado (resumido) y tus números.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md):

- el módulo `:baselineprofile`, con `targetProjectPath`, el GMD y `useConnectedDevices = false`;
- los benchmarks de arranque (tres compilaciones) y de la lista, con UiAutomator por `resource-id`;
- `ReportDrawnWhen` antes del `return`, comprobado en `DeviceTest`;
- el perfil y el de arranque generados y en el repositorio, en el APK de release;
- las variantes de demo solo debug, sin avisos del plugin;
- los tests de la semana 35 en verde, sin warnings de Kotlin, `lintFieldDebug` en verde y tests con los valores esperados escritos a mano;
- los números, con su `context`, y una conclusión que no diga más de lo que dicen.

## Pistas

- `ERRORS (not suppressed): EMULATOR`: estás en un emulador; el argumento `suppressErrors=EMULATOR` en la línea de comandos, no en el build.
- `NullPointerException ... UiObject2.setGestureMargin(int) on a null object reference` en el recorrido: la lista no está. O el benchmark lleva `StartupMode.COLD` (mata la app tras `setupBlock`), o la lista está vacía (no aparece en el árbol de UiAutomator).
- `fill` no añade nada: el botón «Alta» apagado es la `View` que se pulsa, no su texto; espera a `By.clickable(true).enabled(true).hasChild(...)`.
- `generate` sale como saltado (`AssumptionViolatedException`) en los benchmarks: es lo esperado, solo corre con `generateBaselineProfile`.
- ``Variant `demoNonMinifiedRelease` is disabled``: es a propósito; `warnings { disabledVariants = false }`.
- `The device "pixel6Api36" does not specify a "testedAbi"`: `testedAbi = "x86_64"`, en los dos GMD.
- `D8: Startup method not found: ...$$ExternalSyntheticLambda...`: un aviso del perfil de arranque; el build sigue (teoría 05, apartado 1).
- `itReportsFullyDrawn` en Robolectric no pasa nunca: va en `src/androidTest` (teoría 05, apartado 3).
- `Dependency verification failed for configuration ':baselineprofile:benchmarkReleaseCompileClasspath'` con `wire-runtime-jvm-6.4.0.module`: falta la restricción del paso 4. La verificación del starter no trae la 6.4.0 a propósito.
- `generateBaselineProfile` tarda unos 10 minutos: crea el emulador, recorre la app varias veces y lo apaga. Es normal.
