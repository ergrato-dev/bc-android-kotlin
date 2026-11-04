# Proyecto — Que se lea bien y recomponga poco

⏱️ 4 h 30 min · Capa 25 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 25, TalkBack leía cada fila de la Bitácora empezando por la letra de la insignia ("A, A-1 · Alta..."), no había encabezados para saltar, el resumen cambiaba sin avisar y en el detalle nada decía qué nivel tenía la observación. Y, después de borrar o añadir una, cada tecla del borrador recomponía todas las filas que se ven. Esta semana la Bitácora se lee bien y deja de recomponer lo que no ha cambiado, y las dos cosas quedan probadas.

## Dónde trabajas

En **tu repositorio**, rama `week-26`, a partir de lo que entregaste en la semana 25. El `starter/` es la Bitácora de la semana 25 resuelta, con comentarios `Paso N` donde va cada cambio.

Y tests nuevos o cambiados:

- `app/src/test/kotlin/dev/bitacora/AccessibilityTest.kt`: la fila sin la insignia, el título como encabezado, el resumen como región viva, el detalle con su encabezado y su nivel seleccionado, y todo lo que se pulsa con etiqueta y 48 dp;
- `app/src/test/kotlin/dev/bitacora/StabilityTest.kt`: teclear no saca observaciones nuevas;
- `app/src/testDemo/kotlin/dev/bitacora/DeleteTest.kt`: `talkBackReadsTheRowAndCanDeleteIt` espera ahora la fila sin la "A".

Con la Bitácora de la semana 25, fallan 5 de 50 en `testFieldDebugUnitTest` y 6 de 43 en `testDemoDebugUnitTest`. Copia esos tests a tu repositorio.

## Pasos

1. **La insignia** (`LevelBadge`): `clearAndSetSemantics {}` al final de la cadena (teoría 01, apartado 2).
2. **Los encabezados**: el título de `BitacoraScreen` y el código de `ObservationScreen`, con `semantics { heading() }`.
3. **El resumen**: `semantics { liveRegion = LiveRegionMode.Polite }` (teoría 02, apartado 5).
4. **El nivel del detalle**: en cada botón de nivel, `semantics { selected = level == observation.level }`.
5. **El `ViewModel`**: `handle.getStateFlow<String?>("observations", null).map { it?.let(::decode) ?: Samples.observations }` como cuarto flujo de `combine`, y la lambda recibe ya la lista (teoría 05, apartado 2).
6. **La estabilidad** (`app/build.gradle.kts`): `composeCompiler` con `stabilityConfigurationFiles.add(layout.projectDirectory.file("compose-stability.conf"))`, y `app/compose-stability.conf` con `dev.bitacora.core.*`. Los informes, solo si se piden: `if (providers.gradleProperty("composeReports").isPresent) { reportsDestination = ...; metricsDestination = ... }`. Compruébalo con `./gradlew :app:compileFieldDebugKotlin -PcomposeReports --rerun`: `stable observation: Observation` (teoría 04).
7. **Los tests**: `./gradlew :core:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew build`. No entra ninguna dependencia nueva.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - en inglés (`@Config(qualifiers = "en")`), una fila Alta se lee `"A-1 · High · ..."`, sin la `"H"`;
   - con `fontScale = 2f`, todo lo que se pulsa sigue teniendo 48 dp de alto para el dedo;
   - el detalle de una observación Baja: "Baja" seleccionado y "Alta" no;
   - con un borrador de 81 caracteres, el campo tiene `SemanticsProperties.Error` con `"Entrada no válida"`;
   - con un `ObservationDraftViewModel`, tras añadir dos y cambiar el nivel de la primera (`replace`), la segunda es igual a la de antes (`assertEquals`) pero no la misma instancia (`assertNotSame`): por eso hace falta el archivo de estabilidad;
   - en la demo, tras borrar A-2 con la acción de TalkBack, el resumen dice `"2 observaciones · Baja: 1 · Media: 0 · Alta: 1"` y sigue siendo región viva.
9. **Explica en tu README** qué lee TalkBack de una fila de tu dominio antes y después del paso 1; qué decía el informe de `ObservationRow` antes y después del paso 6; y por qué, antes de borrar, teclear no recomponía las filas de la demo y después sí.
10. **Abre el pull request** `week-26` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, la fila sin la insignia, los encabezados, el resumen que avisa, el nivel seleccionado, el `ViewModel` que no saca instancias nuevas con cada tecla, el archivo de estabilidad, y tests con valores esperados escritos a mano.

## Pistas

- `BitacoraScreenTest` busca la "A" con `useUnmergedTree = true` y sigue en verde: el árbol sin unir no cambia con `clearAndSetSemantics` (teoría 01).
- `StabilityTest` necesita a alguien suscrito a `state` (`WhileSubscribed`) y el looper principal al día: `shadowOf(Looper.getMainLooper()).idle()`.
- Si el informe sale vacío, lo ha escrito la última compilación, la de los tests: `--rerun` en la tarea de la variante (teoría 04, apartado 2).
- `everythingClickableHasALabelAndRoomForAFinger` pasa desde el principio: los botones de Material ya tienen etiqueta y 48 dp. Es una guarda para lo que añadas.
- `./gradlew :app:testFieldDebugUnitTest --tests '*AccessibilityTest'` para ir test a test.
