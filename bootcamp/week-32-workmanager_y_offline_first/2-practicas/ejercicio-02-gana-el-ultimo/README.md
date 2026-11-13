# Ejercicio 02 — Gana el último

⏱️ 1 h 30 min · Teoría previa: 03

## Objetivo

Ana y Luis llevan las notas de la finca en sus móviles, sin cobertura casi todo el día. Cuando vuelven a tener red, cada móvil junta lo suyo con lo del servidor, y los dos tienen que acabar viendo lo mismo. Al terminar, `merge` junta dos listas de versiones con la regla «gana el último», con un desempate que no depende del orden, con las notas borradas como lápidas que también compiten, y un `Clock` que nunca repite ni retrocede. Sin Android, sin red y sin base de datos: la regla es lógica pura, y se prueba con datos escritos a mano.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM, sin dependencias. En `src/main/kotlin/dev/bitacora/Conflictos.kt` están ya hechos `Version` (una versión de una nota: `text = null` es una lápida) y `visible()` (lo que se ve, sin lápidas). Con comentarios `Paso N`: `beats`, `merge` y `Clock`. Compila.

Los tests (`src/test/kotlin/dev/bitacora/ConflictosTest.kt`, no los toques) parten de dos versiones de la nota `n1`: la de Ana, de las 1000, y la de Luis, de las 2000. Al empezar, fallan 8 de 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué `aNewerDeletionWins` y `mergingTwiceChangesNothing` pasan ya con un `merge` que devuelve la primera lista tal cual? ¿Qué te dice eso de esos dos tests por separado?
2. **Quién gana** (`beats`): la de hora mayor; con la misma hora, la del móvil de nombre mayor. Una versión nunca se gana a sí misma (teoría 03, apartado 5).
3. **Juntar** (`merge`): todas las versiones de las dos listas, agrupadas por `id`; de cada grupo, la que gana a las demás; ordenadas por `id`. Las lápidas se quedan (`groupBy`, `reduce` y `sortedBy` bastan).
4. **El reloj** (`Clock.now`): la hora del sistema, salvo que sea igual o menor que la última que dio; entonces, la última más uno (teoría 03, apartado 3).
5. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia el desempate a `device >= other.device`. ¿Qué test falla? Con esa regla, ¿qué devuelve `merge` con dos versiones idénticas de dos listas distintas, y por qué daría igual aquí pero no si las versiones guardasen, por ejemplo, de qué lista vienen? Restáuralo.
   - (b) Haz que `merge` tire las lápidas (`.filter { it.text != null }` antes de ordenar). ¿Qué test falla? Cuenta, con Ana, Luis y el servidor, cómo vuelve una nota borrada. Restáuralo.
   - (c) `aClockAheadAlwaysWins` pasa, y describe un fallo. Propón dos arreglos de la teoría 03 y di qué pierde cada uno en el caso de Ana y Luis.
   - (d) En la Bitácora no hay nombre de móvil: con la misma hora, al bajar gana lo de aquí. ¿Por qué el móvil y el servidor acaban igual de todas formas? (Pista: ¿qué contesta el servidor al `PUT`?)

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `aVersionDoesNotBeatItself` falla | El desempate es `>=`: con la misma hora y el mismo móvil, tiene que dar `false` |
| `theOrderDoesNotMatter` falla solo con el empate | El desempate depende de cuál va primero en el `reduce`: tiene que mirar el móvil |
| `mergeKeepsTheWinnerOfEachNote` tiene las notas en otro orden | Falta `sortedBy { it.id }` |
| `aNewerDeletionWins` da una lista vacía | `merge` tira las lápidas: tienen que quedarse; solo `visible()` las quita |
| `theClockNeverGoesBack` da `[2000, 1500]` | `now()` devuelve la hora del sistema sin compararla con la última |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.
