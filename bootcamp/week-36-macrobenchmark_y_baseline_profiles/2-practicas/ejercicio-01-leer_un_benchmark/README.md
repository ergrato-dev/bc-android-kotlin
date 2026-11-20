# Ejercicio 01 — Leer un benchmark

⏱️ 1 h 30 min · Teoría previa: 01 y 06

## Objetivo

Macrobenchmark no da un número: deja un JSON con todas las medidas, iteración a iteración. Al terminar, tienes las cuentas que hace con él (mediana, coeficiente de variación, percentiles), comprobadas contra las suyas. Y tienes una regla para decidir si una diferencia es una mejora o ruido, aplicada a medidas de verdad: las de la Bitácora en un emulador.

Es un proyecto de Kotlin para la JVM: no necesita el SDK de Android ni un dispositivo.

## Cómo está montado

- `src/main/kotlin/dev/bitacora/bench/Results.kt`: lee el JSON con kotlinx.serialization. Ya está hecho.
- `src/main/kotlin/dev/bitacora/bench/Stats.kt`: `median`, `coefficientOfVariation` y `percentile`, con `TODO()`.
- `src/main/kotlin/dev/bitacora/bench/Compare.kt`: `compare`, con `TODO()`.
- `src/test/resources/startup.json` y `scroll.json`: lo que dejó Macrobenchmark 1.5.0 al medir la Bitácora en un emulador Pixel 6 con API 36. El de arranque, 3 modos × 15 iteraciones; el de la lista, 2 modos × 5 recorridos.
- `src/test/kotlin/dev/bitacora/bench/BenchTest.kt`: 11 tests. No los toques, son el enunciado.

Compila, y al empezar fallan 10 de 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué pasa ya `itReadsTheEmulatorRun`? Abre `startup.json`: busca `context` y apunta el modelo, los núcleos, la frecuencia y `cpuLocked`. ¿Qué te dicen sobre si estos números sirven para decidir? (Teoría 06, apartado 4.)
2. **La mediana**: ordena y coge el del medio; con un número par, la media de los dos del medio. Pasan los tres tests de la mediana. En `theMedianOfAnOddList` hay un 1 900: ¿cuánto valdría la media?
3. **El coeficiente de variación**: la desviación típica entre la media. ¿Muestral (n − 1) o de la población (n)? Prueba una, ejecuta `theCoefficientOfVariationIsTheOneMacrobenchmarkWrites` y, si falla, mira por cuánto.
4. **Los percentiles**: ordena y busca la posición `p × (n − 1)`; si cae entre dos, interpola. En `scroll.json`, cada iteración tiene su lista de fotogramas: ¿con qué fotogramas calcula Macrobenchmark su P90, los de una iteración o todos juntos?
5. **La comparación**: `change` es `(después − antes) / antes`, con las medianas. Si el CV de alguna de las dos pasa de `maxNoise`, `TOO_NOISY`; si no, si `|change|` es menor que `threshold`, `NOISE`; si no, `FASTER` o `SLOWER`. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`). Contesta y, donde haga falta, cambia algo, ejecuta y restáuralo:
   - (a) Con tu `compare`, ¿qué dice el arranque hasta el primer fotograma (TTID) con `Full()` frente a `None()`? ¿Tiene sentido que todo compilado sea más lento? ¿Qué te dice eso del ruido?
   - (b) Cambia `maxNoise` a 0,12 en `theEmulatorIsTooNoisyForTheFullDisplay` (como argumento de `compare`). ¿Qué dice ahora? ¿Es una mejora de verdad? Restáuralo.
   - (c) Compara los P90 de `frameDurationCpuMs` de `scroll.json` a mano. ¿Cuánto cambian? ¿Por qué esta regla no sirve tal cual para los fotogramas (pista: ¿hay CV)?
   - (d) En `startup.json`, mira `runs` de `timeToFullDisplayMs` en `withoutProfile`. ¿Hay alguno raro? ¿Cuánto cambia la media si lo quitas, y cuánto la mediana?
   - (e) ¿Cuántas iteraciones harían falta para que un 5 % fuera distinguible con un CV del 10 %? No hay que calcularlo exacto: razona qué pasa con el ruido de la mediana al repetir más.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `kotlin.NotImplementedError: An operation is not implemented.` | Es el `TODO()`: ese paso falta |
| El CV no coincide por un 3 % | Has dividido entre n: la muestral divide entre n − 1 |
| La mediana de una lista par sale como uno de los dos | Es la media de los dos del medio |
| `percentile` falla con `IndexOutOfBoundsException` en P99 | El vecino de arriba no puede pasar del último |
| Los P90 no coinciden por poco | Los fotogramas de todas las iteraciones juntos (`runs.flatten()`), y ordenados |
| `theEmulatorIsTooNoisyForTheFullDisplay` da `FASTER` | El ruido se mira antes que el cambio: primero `TOO_NOISY`, después lo demás |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 6.
