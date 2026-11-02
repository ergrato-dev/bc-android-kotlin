# Ejercicio 01 — La tarjeta que se mueve

⏱️ 2 h · Teoría previa: 01, 02, 03 y 05

## Objetivo

Una tarjeta por observación. Un toque la abre y enseña la descripción, que crece poco a poco; otro toque la cierra, y la descripción se va poco a poco. Una pulsación larga cambia el nivel, también con TalkBack, y la insignia pasa al color nuevo en 300 ms sin recomponer en cada fotograma. Todo comprobado en la JVM, con el reloj parado.

## Cómo está montado

`starter/` es una app Android de un solo módulo con Compose y Robolectric. Trae hecho el tema de la semana 23, `Level` (con su color en `res/values/colors.xml` y `next`, el nivel siguiente), `Session` (un `ViewModel` con A-1 Alta, A-2 Media y A-3 Baja, y `nextLevel`) y `MainActivity`, que pinta una `ObservationCard` por observación y deja abierta como mucho una. En `ObservationCard.kt`, la tarjeta y la insignia, sin gestos, sin animaciones y sin color.

Los tests (`app/src/test/kotlin/dev/bitacora/ObservationCardTest.kt`, no los toques) llevan `@GraphicsMode(NATIVE)` y paran el reloj con `rule.mainClock.autoAdvance = false` para mirar a medias (teoría 01). Al empezar, fallan los 8.

## Pasos

1. **Los gestos** (`ObservationCard`): la `Column` con `combinedClickable`: `onClick = onToggle`, `onLongClick = onChangeLevel` y `onLongClickLabel` con `R.string.change_level` (teoría 03).
2. **La descripción**: `AnimatedVisibility(expanded)` en vez de enseñarla siempre, con las transiciones por defecto (teoría 02).
3. **La insignia** (`LevelBadge`): `val color = animateColorAsState(colorResource(level.color), tween(300), label = "badge")`, sin `by`, y `Modifier.drawBehind { drawRect(color.value) }` (teoría 01 y 05).
4. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew build`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia `drawBehind { drawRect(color.value) }` por `background(color.value)`. ¿Falla algún test? Añade un contador (`var count = 0` fuera, `count++` en `LevelBadge`) y cuenta cuántas veces se recompone la insignia de A-1 en un cambio de nivel, con una y otra versión. ¿Qué te dice eso de lo que comprueban los tests? Restáuralo.
   - (b) Quita `onLongClickLabel`. ¿Qué test falla, con qué mensaje? ¿Qué oye quien usa TalkBack? Restáuralo.
   - (c) Quita el `tween(300)` (deja el `spring` por defecto). ¿Qué test falla? ¿Qué color tiene la insignia a los 150 ms? Restáuralo.
   - (d) Cambia `AnimatedVisibility(expanded)` por `if (expanded)`. ¿Qué dos tests fallan, y qué dice el de la altura? Restáuralo.
   - (e) En `theBadgeChangesColorIn300Milliseconds`, quita la línea `rule.mainClock.autoAdvance = false`. ¿Qué pasa, y por qué? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `theBadgeHasTheLevelColor` da negro o transparente | `drawBehind` no pinta: falta `drawRect(color.value)`, o va en otro `Box` |
| `theDescriptionGrowsIn`: `64.0 < 88.0 < 88.0` | La descripción salta: falta `AnimatedVisibility` |
| `theLongPressHasALabel`: `but was:<null>` | Falta `onLongClickLabel` |
| `aLongPressChangesTheLevel` no cambia nada | `onLongClick` no llama a `onChangeLevel`, o `combinedClickable` no está en la `Column` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.
