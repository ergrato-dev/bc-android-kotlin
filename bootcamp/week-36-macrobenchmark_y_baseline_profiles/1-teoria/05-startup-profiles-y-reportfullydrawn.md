# Startup Profiles y `reportFullyDrawn`

> Dos piezas del arranque que no son el Baseline Profile. Un **Startup Profile** no le dice a ART qué compilar, sino a R8 qué clases poner juntas en el primer archivo dex. Y **`reportFullyDrawn`** es la app diciendo «ya estoy lista»: sin ella, Macrobenchmark solo sabe cuándo salió el primer fotograma, que en la Bitácora es una pantalla vacía.

## 🎯 Objetivos

- Generar un Startup Profile y saber en qué se diferencia del Baseline Profile
- Saber qué hace `dexLayoutOptimization` y por qué esta semana no cambia nada
- Avisar de que la app está lista con `ReportDrawnWhen`
- Comprobarlo donde se puede comprobar: en un dispositivo

## 1. El Startup Profile

`includeInStartupProfile = true` en un `collect` manda sus reglas también a `startup-prof.txt`. Es el mismo formato, otro uso:

| | Baseline Profile | Startup Profile |
| --- | --- | --- |
| Lo lee | ART, al instalar | R8, al construir |
| Para | Compilar por adelantado lo usado | Poner las clases del arranque juntas en `classes.dex` |
| Recorrido | Todo lo importante | Solo el arranque |

Cargar una clase de otro dex al arrancar cuesta leer otro archivo. Con las del arranque juntas en el primero, se lee menos.

```kotlin
// app/build.gradle.kts
baselineProfile {
    dexLayoutOptimization = true
}
```

> 🔬 Medido en la Bitácora, sin R8 (la semana 37 lo enciende): con `dexLayoutOptimization = true`, el APK de release tiene los mismos cuatro dex, con las mismas clases. `classes.dex` tiene 4 991 clases, 152 de `dev.bitacora`, con la opción y sin ella. La reordenación la hace R8: sin minificar, no hay quien la haga. Se mide en la semana 37.

> 🔬 Medido en `./gradlew build`: D8 avisa 41 veces de `Startup method not found`, como `void dev.bitacora.BitacoraScreenKt$$ExternalSyntheticLambda10.<init>(dev.bitacora.model.Level)`. Todos son lambdas sintéticas (`$$ExternalSyntheticLambda`), de `MainActivity` (21), `BitacoraScreenKt` (15) y `NetworkKt` (5): reglas del perfil de arranque que D8 no encuentra en la release. Es un aviso, el build sigue verde.

## 2. «Ya estoy lista»

```kotlin
setContent {
    val state by notes.state.collectAsStateWithLifecycle()
    ReportDrawnWhen { state.loaded }
    if (!state.loaded) return@setContent
    // ...
}
```

`ReportDrawnWhen` (de `activity-compose`) llama a `reportFullyDrawn()` cuando su condición pasa a ser cierta, y Macrobenchmark mide `timeToFullDisplayMs` hasta ahí. Va **antes** del `return`: si fuera después, no se compondría hasta que la lista estuviera, y no tendría nada que esperar.

| | Cuándo avisa |
| --- | --- |
| `ReportDrawn()` | En cuanto se compone |
| `ReportDrawnWhen { condición }` | Cuando la condición es cierta |
| `ReportDrawnAfter { suspend }` | Cuando termina el bloque |

Si varias partes de la pantalla esperan cosas distintas, cada una pone el suyo: la actividad avisa cuando todas han terminado.

## 3. Dónde se comprueba

> 🔬 Medido con Robolectric 4.17: `ReportDrawnWhen` no llega nunca a `reportFullyDrawn`. Ni en la Bitácora ni en un caso mínimo (`ReportDrawnWhen { ready }` con `ready = true` en un `LaunchedEffect`): tras `waitForIdle` y un segundo de reloj, `fullyDrawnReporter.isFullyDrawnReported` sigue en `false`, con un reporter pendiente. Llamando a `reportFullyDrawn()` a mano, sí cambia. No está medido por qué; en un dispositivo funciona.

Así que esta comprobación va en `src/androidTest`:

```kotlin
@Test
fun itReportsFullyDrawn() {
    rule.waitUntil(5_000) { rule.activity.fullyDrawnReporter.isFullyDrawnReported }
}
```

> 🔬 Medido en el emulador: pasa en 2,8 s, con `connectedFieldDebugAndroidTest`. Y la prueba de verdad es el benchmark: con `ReportDrawnWhen`, el JSON trae `timeToFullDisplayMs`, y sin él no.

Al ejecutar por primera vez los tests instrumentados de la semana 35 en un dispositivo, `addingShowsTheRow` falló: `could not find any node that satisfies: (Text + InputText + EditableText contains 'Descripción')`. El emulador estaba en inglés. En el dispositivo, los textos se sacan de los recursos (`rule.activity.getString(R.string.description_hint)`), como avisaba la teoría 05 de esa semana.

## ⚠️ Errores comunes

| Error | Qué pasa | Arreglo |
| --- | --- | --- |
| `ReportDrawnWhen` después del `return` | No se compone hasta que ya está todo | Antes |
| Probarlo con Robolectric | No llega nunca, aunque la app esté bien | Un test instrumentado, o el benchmark |
| Esperar que `dexLayoutOptimization` cambie algo sin R8 | Los dex salen iguales | Semana 37 |
| Asustarse con `Startup method not found` | Es un aviso de lambdas sintéticas | Revisarlo, el build sigue |

## 📝 Resumen

- El Startup Profile le dice a R8 qué clases van juntas en el primer dex. Sin R8, no cambia nada.
- `reportFullyDrawn` es la app diciendo que está lista. `ReportDrawnWhen` lo llama cuando toca.
- En la Bitácora, la condición es que Room haya leído la lista.
- Robolectric no llega a avisar: se comprueba en un dispositivo.

## 🔗 Recursos

- [Startup Profiles y dex layout](https://developer.android.com/topic/performance/baselineprofiles/dex-layout-optimizations)
- [Tiempo hasta la pantalla completa](https://developer.android.com/topic/performance/vitals/launch-time#time-full)
- [`ReportDrawn` en Compose](https://developer.android.com/reference/kotlin/androidx/activity/compose/package-summary#ReportDrawnWhen(kotlin.Function0))
