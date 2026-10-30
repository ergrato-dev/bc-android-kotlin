# Cambios de configuración

> Giras el móvil y lo que habías escrito desaparece. No es un fallo de Android: es lo que hace por diseño. Cuando cambia la **configuración**, destruye la actividad y crea otra. Entender por qué evita la tentación de desactivarlo.

## 🎯 Objetivos

- Explicar qué es un cambio de configuración y qué hace Android con la actividad
- Saber qué se pierde y qué no en una recreación
- Explicar por qué `android:configChanges` no es la solución

## 1. La configuración

La **configuración** (`Configuration`) es todo lo que puede cambiar cómo se ve la app: la orientación, el tamaño de la ventana, el idioma, el modo oscuro, el tamaño de letra, si hay teclado físico. La pantalla depende de ella: los textos en otro idioma, otro layout en horizontal, otros colores en modo oscuro.

Cuando cambia, Android hace lo más simple y seguro: **destruye la actividad y crea otra** con la configuración nueva, y así todos los recursos se vuelven a cargar bien. Desde fuera es la misma pantalla. Por dentro es otro objeto.

```text
vieja: onPause → onStop → onSaveInstanceState → onDestroy
nueva: onCreate(savedInstanceState != null) → onStart → onResume
```

> 🔬 Medido en el ejercicio 01 con `RuntimeEnvironment.setQualifiers("+land")`, que en Robolectric gira el móvil: la actividad de después no es la de antes (`assertNotSame`), y su `onCreate` recibe un `Bundle`.

## 2. Qué se pierde

| Dónde está el dato | ¿Sobrevive al giro? |
| --- | --- |
| Un campo de la actividad (`var count = 0`) | No: es otro objeto |
| El `Bundle` de `onSaveInstanceState` | Sí: llega al `onCreate` de la nueva |
| Un `ViewModel` | Sí: Android lo guarda aparte y se lo da a la nueva (teoría 03) |
| El `SavedStateHandle` de un `ViewModel` | Sí |

> 🔬 Medido con Robolectric en el ejercicio 02: el campo vuelve a `0`; el `Bundle`, el `ViewModel` y su `SavedStateHandle` conservan el valor. `onCleared()` del `ViewModel` no se llama en el giro; sí al cerrar la actividad.

El giro no es el único cambio. El usuario cambia el idioma del sistema, activa el modo oscuro o redimensiona la ventana en un plegable o en una tablet, y la actividad se recrea igual. Probar el giro es la forma más rápida de probar todos.

## 3. La tentación: `configChanges`

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize" />
```

Con esa línea, Android no recrea la actividad para esos cambios: llama a `onConfigurationChanged` y deja que tú te las arregles.

> 🔬 Medido: con `configChanges`, al girar sigue siendo la misma instancia y se llama a `onConfigurationChanged`. En el ejercicio 01 fallan los tres tests que esperan una actividad nueva.

Parece que arregla la pérdida de estado, pero no la arregla:

- Solo cubre los cambios que enumeras. El idioma, el modo oscuro o la densidad siguen recreando la actividad.
- No cubre la **muerte del proceso** (teoría 03), que pierde lo mismo y es más difícil de ver.
- Los recursos que dependen de la configuración (un layout en horizontal, textos en otro idioma) ya no se recargan solos: lo tienes que hacer tú en `onConfigurationChanged`.

Hay casos en que tiene sentido (un reproductor de vídeo que no quiere cortar la reproducción al girar), pero como decisión consciente, no como parche para el estado. Compose, en la fase 4, maneja algunos cambios sin recrear, y aun así el estado se guarda como en la teoría 03.

## ✅ Comprueba

- [ ] Nombra tres cambios de configuración que no son el giro.
- [ ] Un compañero añade `configChanges` "porque se perdía el texto al girar". ¿Qué le contestas? ¿Qué caso sigue roto?
- [ ] ¿Por qué Android prefiere recrear la actividad a avisarla del cambio?
