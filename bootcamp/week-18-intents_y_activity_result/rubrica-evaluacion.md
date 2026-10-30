# Rúbrica de Evaluación — Semana 18: Intents y Activity Result

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora abre y comparte |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre un Intent explícito y uno implícito? ¿Qué es el componente de un Intent explícito? | 10 |
| 2 | ¿Por qué `Observation` no es `Parcelable` en la Bitácora, y qué viaja en su lugar? | 10 |
| 3 | ¿Qué dos problemas tiene `intent.getParcelableExtra(name)` en la Bitácora, y por qué los resuelve `IntentCompat`? | 10 |
| 4 | ¿Por qué `createChooser` no da `ActivityNotFoundException` en un móvil, y `startActivity(send)` sí puede? | 10 |
| 5 | `resolveActivity` devuelve `null` en un Android 14 con Gmail instalado. ¿Qué falta y por qué? | 10 |
| 6 | Un `intent-filter` con `VIEW`, `BROWSABLE` y `<data>`, sin `DEFAULT`. ¿Abre `startActivity` esa actividad con el enlace? | 10 |
| 7 | ¿Cuándo es obligatorio `android:exported`, y qué significa `false`? | 10 |
| 8 | ¿Por qué `registerForActivityResult` va al crear la actividad y no al pulsar el botón? | 10 |
| 9 | ¿Qué devuelve un contrato si el usuario vuelve atrás? ¿Quién pone ese resultado? | 10 |
| 10 | Android mata la app mientras el usuario elige una foto. ¿Cómo llega la `Uri` a la actividad nueva? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. El explícito nombra la clase que se abre (`Intent(this, ObservationActivity::class.java)`); el implícito dice qué quieres hacer (acción, datos, tipo) y Android busca qué apps lo aceptan. El componente es el paquete de la app (el `applicationId`, `dev.bitacora.agro`) y el nombre de la clase.
2. Porque `Parcelable` es de Android y `core` no sabe nada de Android. Se pasa el JSON que ya escribe `data`, el mismo formato que el `SavedStateHandle`.
3. Está deprecado (con `allWarningsAsErrors`, no compila) y no comprueba el tipo. Su sustituto con `Class` solo existe desde la API 33, y con `minSdk = 26` lint lo para con `[NewApi]`. `IntentCompat.getParcelableExtra` usa la API nueva en Android 13 o superior y la vieja antes.
4. El selector (`ACTION_CHOOSER`) es del sistema y siempre existe: si ninguna app acepta el Intent, muestra la lista vacía. `startActivity(send)` sin ninguna app que acepte `send` lanza `ActivityNotFoundException`.
5. Falta `<queries>` en el manifiesto con ese Intent. Desde Android 11, una app no ve las demás apps instaladas salvo las que declara que le interesan. `startActivity` no lo necesita; preguntar, sí.
6. No: `startActivity` añade `CATEGORY_DEFAULT` a todo Intent implícito, y el filtro tiene que tener todas las categorías del Intent. `queryIntentActivities(intent, 0)` sí la encuentra; con `MATCH_DEFAULT_ONLY`, no.
7. Desde `targetSdk` 31, en toda actividad con `intent-filter`; si falta, la fusión del manifiesto se para. `false` significa que ninguna otra app puede abrirla, ni con un Intent explícito.
8. Porque, si el proceso muere, Android crea una actividad nueva y le entrega el resultado al arrancar. El callback tiene que estar registrado antes de `STARTED`, en el mismo orden, para que el registro recupere su `requestCode`. Registrar tarde lanza `IllegalStateException: ... LifecycleOwners must call register before they are STARTED`.
9. `null` (`PickLevel`) o lo que diga su `parseResult` para `RESULT_CANCELED`. No lo pone nadie: si la actividad se cierra sin `setResult`, el resultado es `RESULT_CANCELED` (`0`) sin datos.
10. El registro guardó en el `Bundle` del estado qué `requestCode` es de qué lanzador. La actividad nueva se crea con ese `Bundle`, vuelve a registrar el lanzador de `GetContent` al crearse, y el registro le entrega la `Uri` a su callback.

**Ejercicio 01, paso 7**: (a) La fusión del manifiesto (`processDebugMainManifest`) para con `android:exported needs to be explicitly specified for element <activity#dev.bitacora.ObservationActivity>`. Sin `intent-filter`, no: compila y la actividad no se exporta. (b) `'fun <T : Parcelable!> getParcelableExtra(p0: String!): T?' is deprecated`, y con `allWarningsAsErrors` es un error. La versión con `Class` compila, pero lint dice `Call requires API level 33 (current min is 26) [NewApi]`. `IntentCompat` elige la llamada según la versión de Android. (c) Falla `theDeepLinkOpensTheObservation` con `expected:<[dev.bitacora.ObservationActivity]> but was:<[]>`. Con `0`, la búsqueda encuentra la actividad aunque falte `DEFAULT`; `startActivity` busca como `MATCH_DEFAULT_ONLY`, y lanzaría `ActivityNotFoundException`. (d) Sí: Robolectric no simula la visibilidad de paquetes. En Android 14, `false`: sin `<queries>`, la Bitácora no ve Gmail. (e) Falla con `ActivityNotFoundException: android.intent.action.CHOOSER`: en el móvil de Robolectric no hay selector. En un móvil real, no: el selector es del sistema y siempre está.

**Ejercicio 02, paso 7**: (a) Fallan los que llaman a `changeLevel()`, con `IllegalStateException: LifecycleOwner ... is attempting to register while current state is RESUMED. LifecycleOwners must call register before they are STARTED.` El callback tiene que existir antes de que la actividad recreada arranque, para recibir el resultado tras la muerte del proceso. (b) Los dos de `changeLevel()` coinciden; el de `attachPhoto()` es otro, y el de la otra actividad, otro. Son aleatorios porque el `requestCode` lo asigna el registro a cada lanzador al registrarlo, y lo guarda en el `Bundle`: no los elige quien lanza, y no chocan con los de otras pantallas. (c) `false`: la actividad nueva no sabe de quién es ese `requestCode`. El usuario elegiría el nivel y la app lo perdería. (d) `thePickerReturnsTheChosenLevel` con `expected:<-1> but was:<0>`. `RESULT_OK` es `-1` y `RESULT_CANCELED`, `0`. (e) `ActivityNotFoundException: android.intent.action.GET_CONTENT`. Pasaría en un móvil sin ninguna app que acepte `GET_CONTENT` de imágenes (un perfil de empresa recortado, por ejemplo). Se evita capturando `ActivityNotFoundException` en `attachPhoto()` y avisando al usuario.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Intents de ida (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| `IntentCompat` y `toUri()`; lint sin errores; `<queries>` y un filtro con `DEFAULT` (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Resultados que vuelven (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Lanzadores como propiedades; el contrato no expone extras a quien lanza (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora abre y comparte

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: Intent de la lista, resultado OK, cancelado, la pantalla que devuelve, cierre sin observación, compartir, muerte del proceso | 25 |
| Intents | Contrato `EditObservation` con la observación en JSON de `data`; lanzador como propiedad; selector para compartir | 20 |
| Manifiesto y pantallas | `ObservationActivity` con `exported="false"` y comprobada en el APK; se cierra sin observación; textos en los recursos y en los dos idiomas | 15 |
| Adaptación al dominio | Textos en el idioma del dominio; README con lo que pide el paso 10 | 20 |
| Entrega | Rama `week-18`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `startActivityForResult` u `onActivityResult` con `@Suppress` | -10 |
| `registerForActivityResult` dentro de un listener | -10 |
| `core` depende de Android o `Observation` es `Parcelable` | -10 |
| `ObservationActivity` exportada sin motivo | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-18` → `main` en el repositorio del aprendiz.
