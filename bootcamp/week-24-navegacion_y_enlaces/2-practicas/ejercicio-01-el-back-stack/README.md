# Ejercicio 01 — El back stack

⏱️ 2 h · Teoría previa: 01, 02, 03 y 05

## Objetivo

Dos pantallas en una sola actividad: la sesión, con una línea por observación, y una observación, con su nota y un botón por nivel. Abrir apila, "Siguiente" apila otra encima, elegir un nivel guarda y vuelve, atrás vuelve de una en una y en la sesión cierra. La nota de una observación tapada sigue ahí al volver, y todo sobrevive a la muerte del proceso.

## Cómo está montado

`starter/` es una app Android de un solo módulo con Compose, Robolectric y, ya en el build, Navigation 3 1.2.0 y el plugin de serialización. Trae hechos el tema de la semana 23, `Session` (un `ViewModel` con A-1, A-2 y A-3 y `replace`) y las dos pantallas en `Screens.kt`, `SessionScreen` y `ObservationScreen`, que no saben nada de la navegación: avisan con `onOpen`, `onChoose` y `onNext`. `MainActivity` pinta `BitacoraNav(session)`, que está vacío.

Los tests (`app/src/test/kotlin/dev/bitacora/BackStackTest.kt`, no los toques) arrancan la actividad entera y buscan por el texto. Al empezar, fallan los 9: también `backOnTheSessionCloses`, que antes de atrás comprueba que se ve la sesión.

## Pasos

1. **Las claves** (`Navigation.kt`): `SessionKey`, un `data object`, y `ObservationKey(val code: String)`, una `data class`. Las dos `@Serializable` y `NavKey` (teoría 01 y 02).
2. **El `NavDisplay`**: el back stack con `rememberNavBackStack(SessionKey)`, `onBack` que quita la última clave, y un `entryProvider` con una entrada por clave. La de la sesión pinta `SessionScreen` y abre con `backStack += ObservationKey(it.code)`. La de la observación busca la suya por el código, pinta `ObservationScreen` y, con `onChoose`, la guarda en la sesión con el nivel nuevo y quita la clave. `onNext` añade la clave de la siguiente (después de A-3, A-1). Las observaciones, con `session.observations.collectAsStateWithLifecycle()` (teoría 03).
3. **El estado de cada destino**: `entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator())` (teoría 05).
4. **Ejecuta** `./gradlew test` (9 de 9) y `./gradlew build`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia `rememberNavBackStack(SessionKey)` por `remember { mutableStateListOf<NavKey>(SessionKey) }`. ¿Qué test falla, y qué vería el usuario al volver a la app tras la muerte del proceso? Restáuralo.
   - (b) Quita `@Serializable` de `ObservationKey`. ¿Compila? ¿Qué test falla, con qué mensaje, y en qué momento de la vida de la app? Restáuralo.
   - (c) Pon `entryDecorators = emptyList()`. ¿Qué test falla? Quita ahora el parámetro entero: ¿y ahora? ¿Qué te dice eso del valor por defecto? Restáuralo.
   - (d) Pon `onBack = {}`. ¿Cuántos tests fallan? ¿Qué hace atrás para el usuario? Restáuralo.
   - (e) Añade a `ObservationKey` un `val observation: Observation? = null`. ¿Qué dice el compilador? Da dos razones más para no meter la observación en la clave. Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'rememberSaveableStateHolderNavEntryDecorator'` | Está en `androidx.navigation3.runtime`, no en `compose.runtime.saveable` |
| `IllegalStateException: Unknown screen ...` | Falta la `entry` de ese tipo de clave en el `entryProvider` |
| `Serializer for class 'ObservationKey' is not found` | Falta `@Serializable`; el plugin ya está en el build |
| `backGoesToTheSessionAndChangesNothing`: la actividad se cierra | `onBack` no quita la clave, o el back stack es de una sola |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.
