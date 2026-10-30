# Rúbrica de Evaluación — Semana 15: AGP y la primera app

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora, app Android |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué pone el SDK de Android y qué pone AGP en un build? ¿Cuál se descarga con el build y cuál no? | 10 |
| 2 | ¿Qué significan `compileSdk`, `minSdk` y `targetSdk`, y por qué subir `targetSdk` es más delicado que subir `compileSdk`? | 10 |
| 3 | ¿Qué diferencia hay entre `namespace` y `applicationId`? ¿Cuál no deberías cambiar después de publicar? | 10 |
| 4 | ¿Qué hace falta en el manifiesto para que una actividad aparezca en el lanzador, y por qué el build exige `android:exported`? | 10 |
| 5 | ¿Qué hay en un APK y de qué paso del build sale cada parte? | 10 |
| 6 | En AGP 9, ¿qué versión de Kotlin compila tu app si no haces nada, cómo se sube y cómo lo compruebas? | 10 |
| 7 | ¿Por qué `core` y `data` siguen siendo Kotlin puro? Da dos ventajas medibles. | 10 |
| 8 | ¿Por qué la Bitácora declara AGP con `apply false` en el raíz si ningún proyecto del raíz es una app? | 10 |
| 9 | ¿Por qué `aapt2` puede faltar en `verification-metadata.xml` aunque el build funcione, y cómo se arregla bien? | 10 |
| 10 | ¿Qué hace `addGeneratedSourceDirectory` que no harías bien escribiendo la ruta a mano? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. El SDK es una carpeta en tu máquina: las plataformas (`android.jar` de cada versión), `build-tools` (`d8`), `cmdline-tools` (`sdkmanager`, `apkanalyzer`), `platform-tools` (`adb`). AGP es un plugin de Gradle que añade las tareas y usa el SDK. AGP se descarga con el build desde `google()`, con versión en el catálogo y verificación; el SDK no: AGP lo busca con `ANDROID_HOME` (o `sdk.dir`).
2. `compileSdk`: contra qué API compilas. `minSdk`: la versión más vieja que puede instalarla. `targetSdk`: con qué versión declaras que la has probado; Android activa los comportamientos nuevos según ella. Subir `compileSdk` solo te da APIs nuevas; subir `targetSdk` cambia cómo se comporta la app en el móvil y hay que probarla otra vez.
3. `namespace` es del código: el paquete de `R` y de los nombres cortos del manifiesto. `applicationId` es la identidad de la app en el móvil y en Play. Cambiar `applicationId` después de publicar es publicar otra app: los usuarios no reciben la actualización.
4. Una `<activity>` con un `intent-filter` con la acción `MAIN` y la categoría `LAUNCHER`. Desde Android 12, un componente con `intent-filter` tiene que declarar `android:exported`; el lanzador es otra app, así que va a `true`. Con `targetSdk ≥ 31`, sin él falla el *manifest merger*.
5. `classes*.dex` (Kotlin compilado y pasado por `d8`), `AndroidManifest.xml` (el manifiesto fusionado, en binario), `resources.arsc` (la tabla de recursos, de `aapt2`), `assets/` (copiados tal cual), `kotlin/*.kotlin_builtins` (de `kotlin-stdlib`) y `META-INF/` (la firma).
6. La que trae AGP: con 9.4.1, `kotlin-gradle-plugin` 2.2.10. Se sube poniendo el plugin de Kotlin en el classpath del build, en el raíz con `apply false` (2.4.21): gana la más alta. Se comprueba con `./gradlew buildEnvironment`: `kotlin-gradle-plugin:2.2.10 -> 2.4.21`.
7. No necesitan nada de Android. Sus tests se ejecutan en la JVM, en segundos, sin SDK ni dispositivo; `:core:test` no ejecuta ninguna de las tareas de Android. Y no dependen de cómo cambie AGP: se pueden reutilizar fuera de la app.
8. Para que AGP y el plugin de Kotlin se carguen en el mismo classpath, el del raíz, que ven todos los módulos. El raíz ya aplica Kotlin para los tests; si AGP solo se carga en `app`, falla con `Could not generate a decorated class for type KotlinAndroidTarget`. Es la regla de la semana 10: cada plugin se declara una vez, en el raíz.
9. Porque AGP solo lo resuelve cuando compila recursos: sin `res/`, o con esas tareas `UP-TO-DATE` o desde la caché, el modo de escritura no lo ve. Con la caché de configuración, Gradle lo resuelve al guardarla y la verificación falla. Se arregla haciendo que Gradle lo vea: con recursos y `--write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease`. No a mano ni con `lenient`.
10. Dice a AGP que la tarea genera assets, cuál es su salida y que la variante los incluya. AGP elige la carpeta y hace que las tareas que juntan los assets dependan de la tuya. Escribiendo la ruta a mano dependes de una carpeta interna de AGP que cambia entre versiones, te falta (o sobra) un `dependsOn` y la caché y `UP-TO-DATE` dejan de ser fiables.

**Ejercicio 01, paso 5**: (a) `Manifest merger failed : android:exported needs to be explicitly specified for element <activity#dev.bitacora.MainActivity>`. Android 12 lo exige para no exportar componentes sin querer; antes, tener `intent-filter` lo exportaba en silencio. (b) `The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.` (c) AGP trae 2.2.10 y se usa 2.4.21, por `alias(libs.plugins.kotlin.jvm) apply false` en el raíz. (d) Varios `classes*.dex`; `MainActivity` sale en `dex packages` como `C d dev.bitacora.MainActivity` (`d`: definida en el APK).

**Ejercicio 02, paso 5**: (a) `app/build/generated/assets/generateBuildInfo/`, elegida por AGP. No hace falta una tarea por variante porque la salida no depende de la variante; si dependiera (el `applicationId`, por ejemplo), se registraría una tarea dentro de `onVariants`. (b) Con `--rerun-tasks`, `:core:test` son 4 tareas y `:app:testDebugUnitTest`, 23: pasa por las de Android (manifiesto, recursos, variante) aunque `app` no tenga tests. Los tiempos los mide cada uno. (c) Porque `release` no tiene firma configurada; un APK sin firmar no se instala. (d) Fallan 3: `theApkHasTheBuildInfo`, `aNewVersionChangesTheAssetAndTheManifest` y `theTaskRunsOnceAndIsUpToDateAfter`. La tarea solo está conectada a `release`, así que `assembleDebug` ni la ejecuta ni mete el asset. Los de release y `theOutputDirIsChosenByAgp` siguen pasando.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La primera app con AGP (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Ningún valor de SDK o versión fuera de `android { }`; manifiesto sin permisos ni atributos que no se piden (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Kotlin puro y la Variant API (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Sin rutas de `build/` escritas a mano ni `dependsOn`; `core` sin Android (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora, app Android

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: `applicationId`, clases en el dex, asset en release, versión nueva, AGP alterado, caché de configuración reutilizada, `core` sin Android | 25 |
| Build | AGP en el catálogo; la configuración de Android en `bitacora.android-application`; `bitacora.build-info` con la Variant API y sin romper la rama de `java`; `core` y `data` sin Android | 20 |
| Verificación | `gradle/verification-metadata.xml` regenerado por Gradle con `sha256`, con AGP y `aapt2`, sin `lenient` ni entradas a mano | 15 |
| Adaptación al dominio | `applicationId` y texto de `MainActivity` con tu dominio; README con mediciones y lo que pide el paso 11 | 20 |
| Entrega | Rama `week-15`, pull request con `test`, mediciones y componentes de la verificación antes y después | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `local.properties` en el repositorio | -10 |
| `org.gradle.dependency.verification=lenient` u `off` en el repositorio | -20 |
| Un sha256 añadido o cambiado a mano | -20 |
| `org.jetbrains.kotlin.android` aplicado o Android en `core`/`data` | -10 |
| Mediciones inventadas o sin el comando que las produjo | -10 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-15` → `main` en el repositorio del aprendiz.
