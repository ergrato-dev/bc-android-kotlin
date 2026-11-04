# Ejercicio 01 — La ficha que lee TalkBack

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

Una tarjeta de observación que se ve bien y se lee mal. Al terminar, el código es un encabezado, la letra de la insignia no se lee, los dos botones dibujados (seguir y compartir) dicen qué son, si están marcados, y tienen sitio para el dedo sin pisarse, "Urgente" es un interruptor de una sola parada, y con la letra al doble la línea se lee entera.

## Cómo está montado

`starter/` trae hechos `Observation` y `Session` (con `follow` y `urgent`), `MainActivity` con una tarjeta por observación, el tema y los textos. `ObservationCard.kt` pinta la tarjeta: `LevelBadge`, `FollowButton` (un círculo, relleno si se sigue) y `ShareButton` (tres puntos unidos), con comentarios `Paso N`.

Los tests (`app/src/test/kotlin/dev/bitacora/ObservationCardTest.kt`, no los toques) miran el árbol de semántica unido, el que llega a TalkBack. Al empezar, fallan los 8.

## Pasos

1. **El encabezado y la insignia**: en el código, `Modifier.semantics { heading() }`. En `LevelBadge`, `clearAndSetSemantics {}` al final de la cadena (teoría 01).
2. **Los botones dibujados**: `FollowButton` con `toggleable(followed, role = Role.Checkbox, onValueChange = { onFollow() })`, y `ShareButton` con `clickable(role = Role.Button, ...)`. Los dos, con `semantics { contentDescription = ... }` (`R.string.follow` y `R.string.share`, leídos con `stringResource` fuera de la lambda) y `minimumInteractiveComponentSize()` **antes** de `size(24.dp)` (teorías 02 y 03).
3. **Urgente**: la `Row` con `toggleable(observation.urgent, role = Role.Switch, onValueChange = { onUrgent() })`, y el `Switch` con `onCheckedChange = null` (teoría 02, apartado 2).
4. **La letra grande**: quita `maxLines` y `overflow` de la línea.
5. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita el `clearAndSetSemantics {}` de la insignia. ¿Qué test falla? ¿Qué leería TalkBack, y en cuántas paradas? Restáuralo.
   - (b) En `FollowButton`, pon `minimumInteractiveComponentSize()` después de `size(24.dp)`. ¿Qué test falla, con qué números? ¿Por qué `touchBoundsInRoot` mide 48 de ancho igual? Restáuralo.
   - (c) Vuelve a poner `onCheckedChange = { onUrgent() }` en el `Switch`. ¿Qué test falla? ¿Cuántas paradas tiene ahora la fila, y qué diría TalkBack en cada una? Restáuralo.
   - (d) Quita `@GraphicsMode(GraphicsMode.Mode.NATIVE)` del último test. ¿Pasa? ¿Por qué? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `@Composable invocations can only happen from the context of a @Composable function` dentro de `semantics { }` | `stringResource` es composable: léelo en una variable antes |
| `followAndShareDoNotOverlap`: `... se pisa con ...` | `minimumInteractiveComponentSize()` va antes de `size`: si va después, no reserva sitio |
| `urgentIsOneStop`: `Expected '2' nodes but found '3'` | El `Switch` sigue pulsándose por su cuenta: `onCheckedChange = null` |
| `theBadgeIsNotRead` encuentra la "A" | `hideFromAccessibility()` en la caja no calla su `Text`: `clearAndSetSemantics {}` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.
