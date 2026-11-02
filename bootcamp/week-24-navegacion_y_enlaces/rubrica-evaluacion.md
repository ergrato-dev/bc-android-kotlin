# Rúbrica de Evaluación — Semana 24: Navegación y enlaces

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: una actividad y sus destinos |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | Con una actividad por pantalla, ¿quién lleva la pila y qué pasa de una pantalla a otra? ¿Qué cambia con Navigation 3? | 10 |
| 2 | Escribe las dos claves de la Bitácora y un `NavDisplay` con su `entryProvider`. ¿Qué pasa con una clave sin su `entry`? | 10 |
| 3 | `remember { mutableStateListOf(SessionKey) }` o `rememberNavBackStack(SessionKey)`: ¿qué ve el usuario tras la muerte del proceso con cada uno? | 10 |
| 4 | Quitas `@Serializable` de `ObservationKey`. ¿Compila? ¿Cuándo falla, y con qué? | 10 |
| 5 | ¿Por qué `ObservationKey` lleva el código y no la observación? ¿Qué tiene que hacer la entrada si no la encuentra? | 10 |
| 6 | Atrás con la sesión sola en el back stack, y con una observación encima: ¿quién responde y qué pasa? ¿Y con `onBack = {}`? | 10 |
| 7 | En la semana 18 el nivel volvía con `setResult`. ¿Cómo vuelve ahora, y por qué no hace falta que viaje? | 10 |
| 8 | Escribe el `intent-filter` de `bitacora://observation/{código}`. ¿Para qué es cada categoría? | 10 |
| 9 | Escribe `startOf`. ¿Por qué la sesión va debajo? ¿Qué pasa con un `Intent` sin `Uri`, y con `A-99`? | 10 |
| 10 | Una nota en A-1, abres A-2 y vuelves; después cierras A-1 y la abres otra vez. ¿Qué ves en cada momento, y quién lo decide? ¿Cuándo usarías un `ViewModel` por entrada? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Android: `startActivity` apila, atrás desapila, y lo que pasa tiene que caber en un `Intent` (extras) y volver con `setResult`. Con Navigation 3, una actividad: el back stack es una lista de claves que es tuya, se navega cambiándola, y `NavDisplay` pinta la última.
2. `@Serializable data object SessionKey : NavKey` y `@Serializable data class ObservationKey(val code: String) : NavKey`. `NavDisplay(backStack, onBack = { backStack.removeLastOrNull() }, entryProvider = entryProvider { entry<SessionKey> { ... }; entry<ObservationKey> { key -> ... } })`. Una clave sin `entry` da `IllegalStateException: Unknown screen ...` al pintarla.
3. Con `mutableStateListOf`, vuelve a la sesión, y atrás cierra la app. Con `rememberNavBackStack`, vuelve a la observación abierta, y atrás va a la sesión: la lista se guarda en el estado de la actividad.
4. Compila y navega. Falla al guardar el estado (al irse a segundo plano): `SerializationException: Serializer for class 'ObservationKey' is not found.`
5. Con `Observation` dentro no compila (`Serializer was not found for type 'Observation?'`), sería una copia que se queda vieja, y el estado guardado viaja por Binder, que tiene un límite. La verdad está en el `ViewModel`, una vez. Tras la muerte del proceso, el primer fotograma puede no tenerla: `find ... ?: return@entry`, no `first`, que da `NoSuchElementException`.
6. Con la sesión sola, `NavDisplay` no recoge atrás: Android cierra la actividad (`isFinishing`). Con una observación encima, `NavDisplay` llama a `onBack`, que la quita. Con `onBack = {}`, atrás no hace nada: ni vuelve ni cierra.
7. La entrada llama a `notes.replace(...)` y quita su clave. Los dos destinos están en la misma actividad, con el mismo `ViewModel`: el resultado se guarda donde está la verdad, y la sesión lo ve porque lo observa. Atrás sin elegir no cambia nada.
8. `<intent-filter>` con `action VIEW`, `category DEFAULT`, `category BROWSABLE` y `<data android:scheme="bitacora" android:host="observation" />`. `DEFAULT`: sin ella, un `Intent` implícito no la encuentra (`queryIntentActivities` da `[]`). `BROWSABLE`: el navegador la añade al abrir un enlace; sin ella, no se abre desde una página.
9. `if (intent.data == null) return listOf(SessionKey)`, `match(DeepLinkRequest(intent))` con un `UriDeepLinkMatcher` y `withBackStack { listOf(SessionKey, it.key) }`, y `takeIf { exists(code) } ?: listOf(SessionKey)`. La sesión debajo para que atrás vuelva a la lista y no salga de la app. Un `Intent` sin `Uri` ni acción hace que `DeepLinkRequest` lance `IllegalArgumentException: DeepLinkRequest must have either a uri or extras.` (el del lanzador pasa, por la acción `MAIN`). `A-99`, sin la comprobación, cierra la app (`ArrayIndexOutOfBoundsException` en el ejercicio 02); con ella, abre la sesión.
10. Al volver de A-2, la nota sigue: el decorador saveable guardó el estado de A-1 mientras estaba tapada (sin él, vacía). Al cerrar A-1 y abrirla, vacía: lo que sale del back stack se borra. Lo deciden los decoradores de `NavDisplay`. Un `ViewModel` por entrada (`rememberViewModelStoreNavEntryDecorator`) es para lo que es de esa pantalla y hay que limpiar al cerrarla, como una carga; su `onCleared` se llama al quitarla.

**Ejercicio 01, paso 5**: (a) `theBackStackSurvivesProcessDeath`: no encuentra `A-1`. El usuario volvería a la lista, y atrás cerraría la app. (b) Compila. `theBackStackSurvivesProcessDeath`, con `SerializationException: Serializer for class 'ObservationKey' is not found.`: al guardar el estado, no al navegar. (c) Con `emptyList()`, `aCoveredObservationKeepsItsNote`. Sin el parámetro, los 9 en verde: el valor por defecto ya trae el decorador saveable. (d) 5 de 9. Atrás no hace nada en una observación. (e) `Serializer was not found for type 'Observation?'`. Sería una copia que se queda vieja, y engorda el estado guardado.

**Ejercicio 02, paso 5**: (a) `anIntentWithoutALinkStartsOnTheSession`, con `IllegalArgumentException: DeepLinkRequest must have either a uri or extras.` El del lanzador lleva la acción `MAIN`, que le basta a `DeepLinkRequest`; el explícito no lleva ni acción ni `Uri`. (b) `aBadLinkStartsOnTheSession` (`expected:<[SessionKey]> but was:<[SessionKey, ObservationKey(code=A-99)]>`) y `anUnknownCodeOpensTheSession` (`ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 3`): cualquier app podría cerrar la Bitácora con un enlace. (c) `aLinkStartsWithTheSessionUnderneath` (`but was:<[ObservationKey(code=A-2)]>`) y `backFromALinkGoesToTheSession`: atrás cierra la app. (d) `androidSendsTheLinksToTheBitacora`: `queryIntentActivities` da `[]`. (e) Ninguno: el `Intent` de los tests no lleva `BROWSABLE`. Con `addCategory(Intent.CATEGORY_BROWSABLE)`, sin la categoría, `[]`: el enlace no se abriría desde el navegador.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El back stack (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| Claves `@Serializable` con un código; `rememberNavBackStack`; las pantallas sin el back stack, con lambdas; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Los enlaces (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `intent-filter` con las dos categorías; `UriDeepLinkMatcher` con `withBackStack`; el código comprobado (revisión) | 3 |
| `NOTAS.md`: paso 5 explicado, con el test propio de `BROWSABLE` | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Una actividad y sus destinos

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 23 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: atrás en la sesión, dos observaciones seguidas, la foto olvidada, el `Intent` explícito, el enlace que no encaja, el nivel desde un enlace | 25 |
| Navegación | Una sola actividad; claves serializables con un código; `rememberNavBackStack`; atrás que vuelve y en la sesión cierra; el resultado en el `ViewModel` | 25 |
| Enlaces | El `intent-filter`; `startOf` con la sesión debajo, la guarda de `intent.data` y el código comprobado | 10 |
| Adaptación al dominio | La pantalla de la observación de tu dominio en Compose; README con lo que pide el paso 9 | 20 |
| Entrega | Rama `week-24`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `ObservationActivity` o `EditObservation` siguen en el código | -10 |
| La observación entera, u otro objeto del dominio, dentro de una clave | -10 |
| Un enlace que abre un código sin comprobar que existe | -15 |
| Las pantallas reciben el back stack en vez de lambdas | -5 |
| `onBack` que no quita la clave, o un atrás que no responde | -10 |
| Un test de la semana 23 o del starter borrado o debilitado para que pase (salvo `EditTest.kt`) | -20 |
| Navigation Compose, u otra dependencia que no hacía falta, o un sha256 añadido a mano | -20 |
| `core` o `data` dependen de Android o de Compose | -10 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-24` → `main` en el repositorio del aprendiz.
