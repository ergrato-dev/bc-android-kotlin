# Intents explícitos

> Hasta ahora, la Bitácora tenía una sola pantalla. Una app de verdad tiene varias, y cada actividad es una puerta de entrada que puede abrir Android, tu app u otra app. Ninguna crea a otra con su constructor: le pide a Android que la abra con un `Intent`, un mensaje que dice qué quieres hacer y con qué datos.

## 🎯 Objetivos

- Abrir otra actividad de tu app con un Intent explícito
- Pasarle datos con extras: tipos simples, `Parcelable` y el JSON de `data`
- Leer un extra sin APIs deprecadas y sin romper el `minSdk`

## 1. El Intent

Un `Intent` explícito nombra la clase que quieres abrir:

```kotlin
startActivity(Intent(this, ObservationActivity::class.java))
```

No llamas a `ObservationActivity()`. Android crea la actividad, la pone encima de la tuya en la pila de la tarea y le da su ciclo de vida, el de la semana 16. La tuya pasa a `onStop`. Cuando el usuario vuelve atrás, la de arriba se destruye y la tuya vuelve a `onStart`.

Para Android, el Intent explícito es un **componente**: el paquete de la app y el nombre de la clase.

```text
ComponentInfo{dev.bitacora.agro/dev.bitacora.ObservationActivity}
```

> 🔬 Medido en la sonda con `shadowOf(activity).nextStartedActivity.component`. El paquete es el `applicationId` (`dev.bitacora.agro`), no el `namespace` (`dev.bitacora`).

Toda actividad tiene que estar en el manifiesto. Si no está, `startActivity` falla:

```text
android.content.ActivityNotFoundException
```

> 🔬 Medido en la sonda, con Robolectric en modo estricto (teoría 05).

## 2. Los extras

Los datos viajan en los **extras** del Intent, un `Bundle` de clave y valor, como el de `onSaveInstanceState` de la semana 16:

```kotlin
const val EXTRA_NOTE = "dev.bitacora.extra.NOTE"

startActivity(Intent(this, ObservationActivity::class.java).putExtra(EXTRA_NOTE, note))
```

En la otra actividad, `intent` es el Intent con el que se abrió:

```kotlin
val code = intent.getStringExtra(EXTRA_CODE)
```

La clave lleva el paquete delante (`dev.bitacora.extra.NOTE`) porque el Intent puede salir de tu app. Dos apps que usen `"note"` se pisarían.

Un `Bundle` solo guarda tipos que Android sabe copiar: números, textos, sus arrays, otros `Bundle`, y objetos `Parcelable` o `Serializable`. Una `data class` de Kotlin no es ninguno de esos.

## 3. `Parcelable` con `@Parcelize`

`Parcelable` es la forma de Android de copiar un objeto entre procesos. Escribirlo a mano son veinte líneas por clase. El plugin de Kotlin **Parcelize** las genera:

```kotlin
plugins {
    alias(libs.plugins.kotlin.parcelize) // org.jetbrains.kotlin.plugin.parcelize, con la versión de Kotlin
}
```

```kotlin
@Parcelize
data class Note(val code: String, val level: String, val text: String) : Parcelable
```

> 🔬 Medido en el ejercicio 01: con AGP 9.4.1, que compila Kotlin él solo, Parcelize funciona aplicando el plugin 2.4.21 en la app (y `apply false` en el raíz, como Kotlin). La `Note` llega igual a la otra actividad.

Pero `Parcelable` es de Android, y el `core` de la Bitácora no sabe nada de Android. `Observation` no puede ser `Parcelable` sin romper esa regla. Hay dos salidas:

- Pasar los campos uno a uno, como tipos simples.
- Pasar el JSON que ya escribe `data`, como hace el `ViewModel` de la semana 16 con el `SavedStateHandle`.

El proyecto usa la segunda: `core` sigue sin Android, y el formato es el mismo que el del estado guardado.

## 4. Leer un `Parcelable`

`getParcelableExtra(name)` está deprecado desde Android 13, porque no comprueba el tipo. Con `allWarningsAsErrors`, el build se para:

```text
e: .../MainActivity.kt:49:23 'fun <T : Parcelable!> getParcelableExtra(p0: String!): T?' is deprecated. Deprecated in Java.
```

Su sustituto, `getParcelableExtra(name, Note::class.java)`, solo existe desde la API 33. Con `minSdk = 26`, lint lo para:

```text
MainActivity.kt:49: Error: Call requires API level 33 (current min is 26): android.content.Intent#getParcelableExtra [NewApi]
```

> 🔬 Medido en la sonda, las dos cosas.

La salida es `IntentCompat`, de `androidx.core` (llega con `androidx.activity`). Usa la API nueva en Android 13 o superior, y la vieja antes:

```kotlin
val note = IntentCompat.getParcelableExtra(intent, EXTRA_NOTE, Note::class.java)
```

Es el patrón de las bibliotecas `*Compat` de AndroidX: una sola llamada que funciona en todas las versiones desde tu `minSdk`.

## 5. Cuánto cabe

Los extras pasan por Binder, el mecanismo con el que se hablan los procesos de Android. Su búfer es de 1 MB por proceso, compartido por todo lo que esté en tránsito. Un Intent demasiado grande falla con `TransactionTooLargeException`. Robolectric no lo simula: no hay Binder en la JVM.

La regla que se deduce: **en un extra van identificadores y datos pequeños**. Una foto no va en un extra: va su `Uri`, como verás con `GetContent` en la teoría 04.

## ✅ Comprueba

- [ ] ¿Por qué no se crea una actividad con su constructor?
- [ ] ¿Por qué `Observation` no es `Parcelable`? ¿Qué pasa la Bitácora en su lugar?
- [ ] Un compañero usa `intent.getParcelableExtra("note")`. ¿Qué dos problemas tiene, y qué usarías?
