# Ejercicio 02 — Estado que sobrevive

⏱️ 2 h · Teoría previa: 02, 03 y 05

## Objetivo

Decidir dónde vive cada dato de una pantalla según lo que tiene que aguantar: el giro del móvil, la muerte del proceso o ninguno de los dos. Tienes tres sitios: un campo de la actividad, una propiedad de un `ViewModel` y el `SavedStateHandle`.

## Cómo está montado

`starter/` es una pantalla de notas de campo. Tiene cuántas observaciones llevas (`count`), el borrador de la siguiente (`draft`) y una caché que se puede volver a calcular (`cache`). Ahora los tres son campos de `MainActivity`, y se pierden al girar el móvil. `FieldNotesViewModel` está vacío. Los tests (`app/src/test/kotlin/dev/bitacora/StateTest.kt`, no los toques) giran el móvil con Robolectric y simulan la muerte del proceso: guardan el estado de una actividad y crean otra, desde cero, con ese `Bundle`.

Dos tests pasan desde el principio y tienen que seguir pasando: `startsEmpty` y `theCacheIsRecalculatedAfterProcessDeath`. La caché **no** tiene que sobrevivir a la muerte del proceso.

## Pasos

1. **`count` y `draft` en el `SavedStateHandle`** (`FieldNotesViewModel`): propiedades que leen y escriben `state["count"]` y `state["draft"]` (`0` y `""` si no hay nada). `addObservation()` suma uno a `count` y vacía `draft`.
2. **`cache` en el `ViewModel`**: una propiedad normal, `var cache: String? = null`.
3. **La actividad no guarda nada** (`MainActivity`): `private val notes: FieldNotesViewModel by viewModels()`. `count`, `draft`, `cache` y `addObservation()` delegan en `notes`.
4. **Ejecuta** `./gradlew test` (7 de 7).
5. **`NOTAS.md`** (en `starter/`):
   - (a) Haz una tabla: campo de la actividad, propiedad del `ViewModel`, `SavedStateHandle`; columnas giro y muerte del proceso. Rellénala con lo que dicen los tests.
   - (b) Pasa `count` a una propiedad normal del `ViewModel`. ¿Qué test falla? ¿Por qué el giro sí lo aguanta? Restáuralo.
   - (c) Guarda `draft` a mano en `onSaveInstanceState` y léelo en `onCreate`, sin `SavedStateHandle`. ¿Pasan los tests? ¿Qué ventaja tiene el `SavedStateHandle`?
   - (d) Un dato de 2 MB (una foto, una lista enorme), ¿iría al `SavedStateHandle`? Busca en la documentación qué pasa cuando el `Bundle` es demasiado grande.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Cannot create an instance of class dev.bitacora.FieldNotesViewModel` | El constructor tiene que ser exactamente `(state: SavedStateHandle)`: `by viewModels()` sabe crearlo así |
| `Unresolved reference 'viewModels'` | Falta `import androidx.activity.viewModels` |
| Pasan los de giro y fallan los de muerte del proceso | `count` o `draft` son propiedades normales del `ViewModel`, no del `SavedStateHandle` (paso 1) |
| `theCacheIsRecalculatedAfterProcessDeath` falla | Guardaste `cache` en el `SavedStateHandle`: ahí no va |
| `theCacheSurvivesARotation` falla | `cache` sigue siendo un campo de la actividad (paso 3) |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.
