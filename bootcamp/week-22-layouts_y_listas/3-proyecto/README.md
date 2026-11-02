# Proyecto — La lista de observaciones

⏱️ 4 h 30 min · Capa 21 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

En la semana 21, la pantalla principal era una `Column` con todo dentro: con muchas observaciones, "Compartir" acababa fuera de la pantalla y no había forma de llegar a él. Esta semana la pantalla reparte el espacio. Arriba, fijos, el título, el campo, la ubicación con "Ubicar" a su derecha, los tres niveles a partes iguales y el resumen. Abajo, fijo, "Compartir". En medio, la lista de observaciones: un `LazyColumn` que se queda con el alto que sobra, solo compone las filas que se ven y guarda hasta dónde ha bajado el usuario, también tras la muerte del proceso. Cada fila enseña la insignia del nivel, la línea de la observación y, debajo, su ubicación, y se pulsa entera para abrirla.

El estado de la app no cambia: el `ViewModel` y la actividad son los de la semana 21.

## Dónde trabajas

En **tu repositorio**, rama `week-22`, a partir de lo que entregaste en la semana 21. El `starter/` es la Bitácora de la semana 21 resuelta, con comentarios `Paso N` donde va cada cambio. Ya trae tests nuevos:

- `app/src/test/kotlin/dev/bitacora/BitacoraScreenTest.kt`: la pantalla sola, con 100 observaciones hechas a mano. Lo que se compone, lo que se queda fijo al desplazar la lista, el reparto de los niveles, la insignia y la ubicación de cada fila, y que la fila se pulsa entera. Vale para los dos sabores.
- en `MainActivityTest`, `theListKeepsItsPlaceAfterProcessDeath`: con 12 observaciones, la posición de la lista vuelve tras la muerte del proceso.
- en `DemoTest`, la cuarta observación no existe hasta bajar a ella.

Con la pantalla de la semana 21, fallan 6 de 10 en `testDemoDebugUnitTest` y 6 de 29 en `testFieldDebugUnitTest`. Los demás siguen en verde. Copia esos tests a tu repositorio.

## Pasos

1. **Las letras de la insignia**: `level_low_short`, `level_medium_short` y `level_high_short` en los recursos, en el idioma del dominio y en el segundo idioma (en la Bitácora agro, "B", "M", "A" y "L", "M", "H"). En `Levels.kt`, `internal val Level.short: Int`, como `label`.
2. **La fila** (`ObservationRow.kt`, nuevo):
   - `LevelBadge(level, modifier)`: un `Box` de 40 dp con el color del nivel y su letra en el centro (ejercicio 01);
   - `ObservationRow(observation, onClick, modifier)`: una `Row` que ocupa todo el ancho, se pulsa entera y lleva 8 dp de margen arriba y abajo, **dentro** de la zona que se pulsa. Dentro, la insignia, 16 dp de separación y una `Column` con `weight(1f)`: la línea (`resources.line`, la de la semana 21) y, si hay posición, las coordenadas (`R.string.place`).
3. **Lo fijo** (`BitacoraScreen.kt`): una `Column` que ocupa toda la pantalla, con 16 dp de margen. El campo, a todo el ancho. La ubicación y "Ubicar" en una `Row`: el texto con `weight(1f)` y el botón a la derecha. Los tres niveles en otra `Row` con `Arrangement.spacedBy(12.dp)` y `weight(1f)` en cada botón. El resumen. "Compartir", al final.
4. **La lista**: entre el resumen y "Compartir", un `LazyColumn` con `Modifier.weight(1f)` y `items(state.observations, key = { it.code })`, con una `ObservationRow` por observación que llama a `onOpen`. Fuera los `TextButton` de la semana 21.
5. **Los tests**: `./gradlew :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug`, hasta el verde. Mira `theListKeepsItsPlaceAfterProcessDeath`: no has escrito nada para que la posición sobreviva. ¿Quién la guarda? (Teoría 05: el parámetro `state` del `LazyColumn` tiene un valor por defecto.)
6. **La verificación**: `./gradlew build`. Esta semana no entra ninguna dependencia nueva: `LazyColumn`, `Row` y `Box` están en `foundation`, que ya venía con `material3`. Si `verification-metadata.xml` pide algo, es que has añadido una biblioteca que no hacía falta.
7. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y la cuenta en un comentario (en `app/src/test`, con un `BitacoraUiState` hecho a mano):
   - la línea de una fila empieza en 16 + 40 + 16 = 72 dp;
   - una fila sin ubicación mide 56 dp (8 + 40 + 8);
   - "Ubicar" acaba en 304 dp (320 - 16), pegado al margen derecho;
   - con la lista vacía, "Compartir" acaba en 454 dp (470 - 16): la lista se queda con el alto que sobra aunque no tenga nada;
   - los tres niveles empiezan a la misma altura;
   - las filas no son botones: los nodos con `Role.Button`, en orden, siguen siendo "Ubicar", los tres niveles y "Compartir".
8. **Explica en tu README** por qué la lista lleva `weight(1f)` y no `verticalScroll` en la `Column` de fuera (¿qué pasaría con "Compartir"? ¿Y con un `LazyColumn` dentro?), por qué la clave es el código y no la posición, y dónde vive la posición de la lista y por qué no en el `SavedStateHandle`.
9. **Abre el pull request** `week-22` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, la lista perezosa con clave, lo fijo fuera de la lista, la fila pulsable entera, y tests con valores calculados a mano.

## Pistas

- La pantalla de Robolectric mide 320 × 470 dp con densidad 1. Robolectric mide el texto a 1 dp por carácter de ancho: compara dónde empieza un texto, no cuánto mide (teoría 01).
- `aRowShowsItsBadgeAndPlace` busca la letra con `useUnmergedTree = true`: la fila es `clickable` y junta los textos de sus hijos en un solo nodo (semana 21, teoría 04).
- `LanguagesTest` busca los botones por su papel, pero en `field` arranca sin observaciones: no ve las filas. Una fila con `clickable` no tiene `Role.Button`; lo comprueba el último test del paso 7, con una observación en la lista.
- `performScrollToKey("A-12")` necesita la clave: sin ella, `Failed to scroll to the item identified by "A-12", couldn't find the key.`
- Si "Compartir" desaparece al añadir observaciones, la lista no tiene `weight(1f)` y se queda con todo el alto.
- `./gradlew :app:testFieldDebugUnitTest --tests '*BitacoraScreenTest'` para ir test a test.
