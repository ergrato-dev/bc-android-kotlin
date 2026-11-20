# Ejercicio 02 — Un perfil y su benchmark

⏱️ 2 h · Teoría previa: 02, 03 y 04

## Objetivo

Una app de Compose con 200 parcelas que llegan de otro hilo. Al terminar tiene tres cosas:

- un Baseline Profile escrito a mano, que llega a su APK junto al de Compose;
- un módulo `:benchmark` con un benchmark de arranque y un generador;
- la variante que se mide, construida por el plugin.

Los tests no necesitan un dispositivo: miran lo que deja el build. Si tienes uno (o KVM para un emulador), el paso 7 lo mide de verdad.

## Cómo está montado

`starter/` es una app (`dev.bitacora.parcelas`) con Compose, ya hecha:

- `app/src/main/kotlin/dev/bitacora/Plots.kt`: las parcelas, con `testTag("plots")` en la lista;
- `MainActivity`, con `testTagsAsResourceId` para que UiAutomator encuentre la lista;
- `gradle/libs.versions.toml`, con Macrobenchmark y el plugin 1.5.0, UiAutomator 2.4.1, profileinstaller 1.4.1 y wire-runtime 6.4.7 (la corregida de la que trae Macrobenchmark);
- el raíz, con los plugins cargados (`apply false`) y `src/test/kotlin/ProfileTest.kt`: 7 tests que miran el perfil, los APK y sus manifiestos. `./gradlew test` construye antes los APK que hacen falta. No los toques.

Necesita `ANDROID_HOME`. Compila, y al empezar fallan 4 de 7.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. Pasan `theProfileIsInTheApk`, `theAppHasTheProfileInstaller` y `theBenchmarkIsNotInTheApp`. ¿De dónde sale el perfil, si la app no tiene ninguno? ¿Y `profileinstaller`, si no está en `app/build.gradle.kts`? Compruébalo con `./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep profileinstaller`.
2. **Las reglas a mano**: `app/src/main/baseline-prof.txt`, con reglas para `MainActivity`, `PlotsKt` y `Plot`, y las clases de `dev/bitacora` (teoría 03, apartado 3). Usa `**`. Un comentario arriba que diga qué son H, S y P. Pasa `everyRuleIsWellFormed`.
3. **Del texto al APK**: `./gradlew :app:assembleRelease` y abre `app/build/intermediates/merged_art_profile/release/mergeReleaseArtProfile/baseline-prof.txt`. ¿Cuántas líneas tiene? ¿Dónde están las tuyas? ¿Tus comodines siguen ahí, o se han convertido en métodos? Pasa `theRulesReachTheMergedProfile`.
4. **El módulo `:benchmark`**: `include(":benchmark")` y `benchmark/build.gradle.kts` con `com.android.test`, el plugin `androidx.baselineprofile`, `targetProjectPath = ":app"`, `minSdk = 28` y las tres dependencias (teoría 02, apartado 1), más la restricción de `wire-runtime`. En la app: el plugin, `implementation(libs.profileinstaller)` y `baselineProfile(project(":benchmark"))`.
5. **Los tests del módulo**, en `benchmark/src/main/kotlin/dev/bitacora/benchmark/`:
   - `StartupBenchmark`, con `withoutProfile` (`CompilationMode.None()`) y `withBaselineProfile` (`Partial(BaselineProfileMode.Require)`), 10 arranques en frío, que espera a la lista: `device.wait(Until.hasObject(By.res("plots")), 5_000)`;
   - `BaselineProfileGenerator`, que arranca, espera a la lista y la recorre con `fling`.
   
   `./gradlew test`: 7 de 7. Después, `./gradlew build`.
6. **Las variantes**: `./gradlew :app:tasks --all | grep -i benchmark`. ¿Qué tipos de build ha añadido el plugin? Con `apkanalyzer manifest print`, compara el manifiesto de `app-benchmarkRelease.apk` con el de `app-release-unsigned.apk`. ¿Qué cambia, y por qué hace falta para medir?
7. **Si tienes un dispositivo o KVM** (opcional): `./gradlew :app:generateBaselineProfile` con el móvil conectado. ¿Dónde queda el perfil generado? ¿Cuántas reglas tiene frente a las tuyas? Luego `./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest` (en un emulador, con `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`) y apunta las medianas. Si no tienes, dilo en `NOTAS.md`.
8. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) Quita la `L` de una regla de método (`HSPdev/...`). ¿Qué tarea falla y con qué mensaje? ¿La línea del error es la de tu archivo? ¿Por qué no?
   - (b) Borra `app/src/main/baseline-prof.txt` y compara el tamaño de `assets/dexopt/baseline.prof` en el APK con y sin tus reglas (`unzip -l`). ¿Cuánto aportan? ¿Por qué tan poco?
   - (c) En `StartupBenchmark`, cambia `Require` por `UseIfAvailable`. ¿Compila? ¿Qué mediría de distinto si la app no trajera perfil?
   - (d) El módulo de la Bitácora lleva `missingDimensionStrategy("environment", "field")` y el tuyo no. ¿Por qué aquí no hace falta? (Teoría 02, apartado 1.)
   - (e) Mira el manifiesto del APK de `:benchmark`. ¿A qué paquete apunta su `instrumentation`? ¿Cómo maneja entonces la app?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Error parsing baseline-prof.txt : baseline-prof.txt:NNNN:N error: Illegal token` | Una regla mal escrita; la línea es la del perfil fusionado. Busca el texto que enseña debajo |
| `Project with path ':benchmark' could not be found.` | Falta `include(":benchmark")` en `settings.gradle.kts` |
| `Task with name 'assembleBenchmarkRelease' not found in project ':benchmark'.` | El módulo no tiene el plugin `androidx.baselineprofile`: sin él no hay `benchmarkRelease` |
| `Dependency verification failed` | Este ejercicio no tiene `verification-metadata.xml`: será otra cosa. Lee el mensaje entero |
| `ERRORS (not suppressed): EMULATOR` | Es un emulador: el argumento `suppressErrors=EMULATOR` en la línea de comandos |

## Entrega

Los 7 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 8 (y el paso 7, si lo hiciste).
