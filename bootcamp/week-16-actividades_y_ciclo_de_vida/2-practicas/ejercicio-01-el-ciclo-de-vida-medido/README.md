# Ejercicio 01 — El ciclo de vida, medido

⏱️ 2 h · Teoría previa: 01, 02 y 05

## Objetivo

Ver con tus propios tests en qué orden llama Android a una actividad: al arrancar, al irse al fondo, al volver, al girar el móvil y al cerrarse. Primero con los métodos de la actividad (`onStart`, `onStop`...), después con un observador del ciclo de vida, y comprobar cómo se intercalan los dos.

## Cómo está montado

`starter/` es una app Android de un solo módulo. `MainActivity` tiene una lista, `events`, donde se apunta lo que pasa, y `LifecycleLogger` es un observador vacío. Los tests (`app/src/test/kotlin/dev/bitacora/LifecycleTest.kt`, no los toques) se ejecutan en la JVM con Robolectric. `ActivityScenario` arranca la actividad y la lleva de un estado a otro, y `RuntimeEnvironment.setQualifiers("+land")` gira el móvil. Todos empiezan en rojo.

`app/build.gradle.kts` ya trae Robolectric configurado (teoría 05). La primera ejecución descarga `android-all`, unos 200 MB, a la caché de Gradle.

## Pasos

1. **Los métodos** (`MainActivity`): apunta `"onCreate"` en `events` dentro de `onCreate`. Sobrescribe `onStart`, `onResume`, `onPause`, `onStop`, `onSaveInstanceState` y `onDestroy`. Cada uno llama a `super` y apunta su nombre.
2. **¿Restaurada?**: `restored = savedInstanceState != null` en `onCreate`.
3. **El observador** (`LifecycleLogger`): implementa los seis métodos de `DefaultLifecycleObserver`. Cada uno apunta `"observer.onCreate"`, `"observer.onStart"`...
4. **Regístralo**: `lifecycle.addObserver(LifecycleLogger(events))` en `onCreate`.
5. **Ejecuta** `./gradlew test` (7 de 7).
6. **`NOTAS.md`** (en `starter/`):
   - (a) Copia la lista que espera `theOldActivitySavesItsStateAfterStopping`. ¿Por qué el observador va **después** del método al subir y **antes** al bajar?
   - (b) Añade `android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"` a la actividad en el manifiesto. ¿Qué tests fallan y qué método se llama ahora al girar? Quítalo: ¿por qué no es la forma de "arreglar" la rotación?
   - (c) Mueve `lifecycle.addObserver(...)` de `onCreate` a `onResume`. ¿Qué apunta el observador y cuándo? Restáuralo.
   - (d) ¿Por qué `onSaveInstanceState` va después de `onStop` y no antes de `onPause`?
   - (e) Opcional, con un móvil: añade `Log.d("Bitacora", ...)` en cada método, instala la app y mira `adb logcat -s Bitacora` mientras la abres, pulsas Inicio, vuelves y la giras.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Failed to interact with raw FileDescriptor internals; perhaps JRE has changed?` | Se ha perdido el `--add-opens` de `testOptions` en `app/build.gradle.kts` (teoría 05) |
| Un test dice `expected:<[onCreate, observer.onCreate, ...` y falta `onCreate` | Lo apuntas antes de `super.onCreate(...)`, o no lo apuntas |
| Los `observer.` salen dos veces | Registras el observador en `onCreate` y también en otro sitio |
| `theOldActivitySavesItsStateAfterStopping` falla solo por `onSaveInstanceState` | Falta `super.onSaveInstanceState(outState)`, o no lo apuntas |
| `theNewActivityKnowsItWasRestored` falla | `restored` se calcula antes de `onCreate` o se queda en `false` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 6.
