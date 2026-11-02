# Proyecto — Borrar, deshacer y moverse

⏱️ 4 h 30 min · Capa 24 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 24, una observación equivocada se quedaba en la sesión. Esta semana se borra deslizando su fila hacia la izquierda, o con la acción "Borrar" de TalkBack, y un aviso ofrece deshacerlo durante 10 s. Las filas de debajo suben poco a poco, y la insignia de una observación pasa al color de su nivel nuevo en 300 ms, sin recomponer en cada fotograma. Y se arregla un fallo que destapa borrar: el código de la siguiente observación se repetía.

## Dónde trabajas

En **tu repositorio**, rama `week-25`, a partir de lo que entregaste en la semana 24. El `starter/` es la Bitácora de la semana 24 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya `nextCode()` en `core`, con el cálculo de antes (`"A-${size + 1}"`), y el `ViewModel` usándolo.

Y tests nuevos:

- `app/src/testDemo/kotlin/dev/bitacora/DeleteTest.kt`: deslizar borra y solo hacia la izquierda, deshacer la devuelve a su sitio, el aviso dura 10 s, TalkBack lee la fila sin el fondo y la puede borrar, las de debajo suben poco a poco, el código no se repite y lo borrado sigue borrado tras la muerte del proceso;
- `app/src/test/kotlin/dev/bitacora/BadgeAnimationTest.kt`: la insignia cambia de color en 300 ms, para los dos sabores;
- `core/src/test/kotlin/dev/bitacora/core/CodeTest.kt`: el código de la siguiente.

Con la Bitácora de la semana 24, fallan 1 de 44 en `testFieldDebugUnitTest`, 9 de 37 en `testDemoDebugUnitTest` y 1 de 5 en `:core:test`. Copia esos tests a tu repositorio.

## Pasos

1. **El código** (`core`): `nextCode()` devuelve uno más que el mayor número de los códigos, no que el tamaño (teoría 04, apartado 5).
2. **El `ViewModel`**: `delete(code)` quita la observación y guarda en el handle cuál era y dónde estaba; `undo()` la devuelve a ese sitio. Las dos como el resto: el JSON de `data` en el handle.
3. **La fila que se borra** (`ObservationRow.kt`): `SwipeToDelete(onDelete, modifier, content: @Composable (Modifier) -> Unit)`, con un `SwipeToDismissBox` que solo se borra hacia la izquierda, el estado en un `remember`, "Borrar" detrás sin semántica (`clearAndSetSemantics`) y en `errorContainer`, y el contenido en un `Surface`. A `content` le pasa un `Modifier.semantics { customActions = ... }` con la acción "Borrar", que va en el `modifier` de `ObservationRow`: es el nodo que enfoca TalkBack (teoría 04). Los textos `delete`, `deleted` (`%1$s borrada`) y `undo`, en español y en inglés.
4. **La pantalla** (`BitacoraScreen.kt` y `MainActivity`): `onDelete` y `onUndo`. Cada fila, en un `SwipeToDelete` con `Modifier.animateItem()`. Al borrar, `onDelete`, `snackbar.currentSnackbarData?.dismiss()` y, en una corrutina, el aviso con "Deshacer" y `SnackbarDuration.Long`; si se pulsa, `onUndo`. El `dismiss` es porque `undo()` devuelve la última borrada: sin él, el aviso de la anterior seguiría en pantalla, y su "Deshacer" devolvería otra. El `SnackbarHost`, en un `Box` alrededor de la `Column`, abajo.
5. **La insignia** (`LevelBadge`): el color con `animateColorAsState(..., tween(300))`, sin `by`, y `drawBehind { drawRect(color.value) }` después del `clip` (teoría 05).
6. **Los tests**: `./gradlew :core:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew build`. No entra ninguna dependencia nueva: las animaciones y `SwipeToDismissBox` ya venían con `material3`.
7. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - `nextCode()` con `A-2` y `A-10`: `A-11` (el número, no el texto, que ordenaría `A-2` después de `A-10`);
   - en inglés (`@Config(qualifiers = "en")`), el aviso dice `"A-2 deleted"` y `"Undo"`;
   - borrar A-1 y A-2 seguidas: el aviso dice `"A-2 borrada"`, y "Deshacer" devuelve A-2 y no A-1;
   - con un `ObservationDraftViewModel(handle)`, `delete("A-1")`, y otro `ObservationDraftViewModel` con el mismo handle (la muerte del proceso): `undo()` la devuelve, la primera;
   - con el reloj parado, a los 150 ms de borrar A-1 la fila de A-2 está más arriba que a los 50;
   - en `field`, que empieza vacía, añadir dos y borrar la primera: la siguiente es `A-3`.
8. **Explica en tu README** qué observación de tu dominio se borra por error y qué pasaría sin deshacer; por qué la fila devuelta se borraba otra vez con `rememberSwipeToDismissBoxState`; y qué lee TalkBack de una fila tuya, antes y después de `clearAndSetSemantics`.
9. **Abre el pull request** `week-25` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, borrar en un solo sentido y con TalkBack, deshacer desde el `ViewModel`, el aviso que se va, las filas que se mueven, la insignia que no recompone, los códigos que no se repiten, y tests con valores esperados escritos a mano.

## Pistas

- En los 470 dp de Robolectric, en la demo solo caben A-1 y A-2: A-3 no está compuesta hasta que se baja a ella (`performScrollToKey`).
- Si el estado cambia desde el test (`level = Level.LOW`), `rule.waitForIdle()` antes de avanzar el reloj: si no, la animación empieza al final del avance (teoría 01).
- `DeleteTest` busca la acción "Borrar" en el nodo con el texto de la fila: si la pones en el `SwipeToDismissBox`, queda en otro nodo, sin texto.
- `@Composable invocations can only happen from the context of a @Composable function` en el `remember`: lee `SwipeToDismissBoxDefaults.positionalThreshold` fuera.
- `./gradlew :app:testDemoDebugUnitTest --tests '*DeleteTest'` para ir test a test.
