# Rúbrica de Evaluación — Semana 16: Actividades y ciclo de vida

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora que no pierde nada |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿En qué orden se llaman los métodos al arrancar una actividad, al irse al fondo y al volver? | 10 |
| 2 | ¿Dónde va `onSaveInstanceState` en una rotación, y desde qué versión de Android? | 10 |
| 3 | ¿Qué hace Android cuando cambia la configuración? Nombra tres cambios que no son el giro. | 10 |
| 4 | ¿Por qué `android:configChanges` no arregla la pérdida de estado? | 10 |
| 5 | ¿Qué es la muerte del proceso, cuándo pasa y qué conserva? | 10 |
| 6 | Campo de la actividad, propiedad del `ViewModel`, `SavedStateHandle`: ¿qué aguanta cada uno y para qué sirve? | 10 |
| 7 | ¿Qué cabe en un `Bundle` y cuánto? ¿Dónde va lo que no cabe? | 10 |
| 8 | ¿Cuándo recibe un `DefaultLifecycleObserver` sus eventos respecto a los métodos de la actividad? ¿Y si se registra tarde? | 10 |
| 9 | ¿Qué hace `repeatOnLifecycle(STARTED)` y qué problema evita? | 10 |
| 10 | ¿Por qué la Bitácora resuelve `android-all` con Gradle en lugar de dejar que lo descargue Robolectric? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Al arrancar: `onCreate`, `onStart`, `onResume`. Al irse al fondo: `onPause`, `onStop`. Al volver: `onStart`, `onResume` (no se vuelve a llamar a `onCreate`).
2. Después de `onStop` y antes de `onDestroy`: `onPause`, `onStop`, `onSaveInstanceState`, `onDestroy`. Es así desde Android 9 (API 28); antes iba antes de `onStop`.
3. Destruye la actividad y crea otra con la configuración nueva, para que los recursos se vuelvan a cargar bien. Otros cambios: el idioma, el modo oscuro, el tamaño de letra, el tamaño de la ventana (multiventana, plegables), un teclado físico.
4. Solo evita la recreación para los cambios que enumeras; los demás siguen recreando. Además no cubre la muerte del proceso, que pierde lo mismo, y te obliga a recargar a mano en `onConfigurationChanged` lo que depende de la configuración.
5. Android mata el proceso de una app en segundo plano cuando necesita memoria, sin llamar a `onDestroy`. Al volver el usuario, crea un proceso y una actividad nuevos con el `Bundle` guardado. Se conserva lo del `Bundle` (y el `SavedStateHandle`); se pierde todo lo que estaba en memoria: campos, `ViewModel`, singletons.
6. Campo: no aguanta nada, solo para lo que no hay que recuperar. `ViewModel`: aguanta el giro, no la muerte del proceso; para lo caro que se puede volver a calcular o cargar. `SavedStateHandle`: aguanta los dos; para lo mínimo que el usuario espera recuperar (borrador, filtro, id).
7. Tipos sencillos (primitivos, `String`, `Parcelable`, `Serializable` y listas de ellos), y poco: la documentación recomienda menos de 50 KB; si es demasiado grande, `TransactionTooLargeException`. Lo grande o lo que debe durar más va a disco o a una base de datos (Room, fase 5), y en el `SavedStateHandle` solo su id.
8. Al subir, después del método de la actividad; al bajar, antes. Si se registra tarde, `addObserver` le manda en ese momento los eventos que faltan hasta el estado actual (registrado en `onResume`: `onCreate`, `onStart`, `onResume` seguidos).
9. Ejecuta el bloque cada vez que la actividad llega a `STARTED` y lo cancela cada vez que baja (antes de `onStop`). Evita recoger un `Flow` con la app en segundo plano, que con `lifecycleScope.launch { collect }` sigue hasta `onDestroy`.
10. Porque Robolectric lo descarga a `~/.m2`, fuera de Gradle: no pasa por la verificación, no usa los repositorios declarados y no respeta `--offline`. Resuelto por Gradle, entra en `verification-metadata.xml` y en la caché, y `robolectric.offline` impide que Robolectric salga a la red.

**Ejercicio 01, paso 6**: (a) `onCreate, observer.onCreate, onStart, observer.onStart, onResume, observer.onResume, observer.onPause, onPause, observer.onStop, onStop, onSaveInstanceState, observer.onDestroy, onDestroy`. El observador se entera cuando la actividad ya ha terminado de subir, y antes de que empiece a bajar: nunca trabaja con una actividad a medias. (b) Fallan `rotatingCreatesANewActivity`, `theOldActivitySavesItsStateAfterStopping` y `theNewActivityKnowsItWasRestored`; se llama a `onConfigurationChanged` sobre la misma instancia. No es la forma porque solo cubre los cambios enumerados y no la muerte del proceso. (c) Recibe `onCreate`, `onStart` y `onResume` seguidos al registrarse, después de `onResume`. (d) Para guardar el estado más reciente: desde Android 9 se guarda cuando la actividad ya no se ve, y lo que el usuario hiciera hasta `onStop` queda dentro.

**Ejercicio 02, paso 5**: (a) Campo: ni giro ni muerte. `ViewModel`: giro sí, muerte no. `SavedStateHandle`: los dos. (b) Falla `theCountSurvivesProcessDeath`: el `ViewModel` pasa los giros porque Android lo guarda fuera de la actividad, pero muere con el proceso. (c) Sí pasan, pero el estado vuelve a vivir en la actividad: el `SavedStateHandle` lo deja en el `ViewModel`, junto a su lógica, y se prueba sin actividad. (d) No: el `Bundle` debe ser pequeño (menos de 50 KB) y uno demasiado grande provoca `TransactionTooLargeException`. Va a disco o a una base de datos, y en el estado, su id.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El ciclo de vida, medido (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| `super` en todos los métodos; el observador en su propia clase y registrado una vez (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Estado que sobrevive (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| La actividad no guarda estado; nada en `onSaveInstanceState` a mano (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora que no pierde nada

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: título, numeración y borrador vacío, borrador en blanco, giro, muerte del proceso, `guava` por la restricción, `android-all` alterado | 25 |
| Estado | Borrador y observaciones en el `SavedStateHandle` (las observaciones como JSON de `data`); la actividad no guarda estado | 20 |
| Build | Robolectric en `bitacora.android-application`; `guava` subida con una restricción y su `because`; `android-all` por Gradle en modo offline, compatible con la caché de configuración; memoria en `gradle.properties`; verificación regenerada por Gradle | 15 |
| Adaptación al dominio | Título con tu dominio; README con dónde vive cada dato y lo que pide el paso 9 | 20 |
| Entrega | Rama `week-16`, pull request con `test` y los componentes de la verificación antes y después | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `android:configChanges` para "arreglar" el giro | -10 |
| Robolectric configurado en `app` en lugar del convention plugin | -10 |
| `android-all` fuera de la verificación (sin `robolectric.offline`) | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-16` → `main` en el repositorio del aprendiz.
