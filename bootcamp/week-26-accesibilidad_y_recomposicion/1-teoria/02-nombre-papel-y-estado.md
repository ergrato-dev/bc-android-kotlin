# Nombre, papel y estado

> De cada parada, TalkBack dice tres cosas: qué es ("Urgente"), qué papel tiene ("interruptor") y en qué estado está ("activado"). Un `Switch` de Material las trae hechas; una fila, un dibujo o un botón que se queda marcado, no. Y hay dos cosas que no son de una parada sino de la pantalla: dónde empieza cada parte y qué ha cambiado sin que nadie lo toque.

## 🎯 Objetivos

- Dar a un control hecho a mano su papel y su estado con `toggleable` y `Role`
- Hacer de una fila con un interruptor una sola parada
- Marcar encabezados, un botón seleccionado y una zona que avisa sola

## 1. El papel y el estado

`clickable` dice que algo se pulsa; `role` dice qué es. `toggleable` dice además si está activado o no, y TalkBack lo lee:

```kotlin
// "Seguir, no marcado, casilla de verificación"
Box(
    Modifier.toggleable(followed, role = Role.Checkbox, onValueChange = { onFollow() })
        .semantics { contentDescription = follow },
)
```

Los papeles son `Button`, `Checkbox`, `Switch`, `RadioButton`, `Tab`, `Image` y `DropdownList`. Con `selectable` (para elegir uno de varios) el estado es "seleccionado".

## 2. Una fila, una parada

Una fila con un texto y un `Switch` al lado son dos paradas: "Urgente", y después "interruptor, desactivado", sin nombre. Y el texto no se puede pulsar.

```kotlin
Row(Modifier.toggleable(urgent, role = Role.Switch, onValueChange = { onUrgent() })) {
    Text(stringResource(R.string.urgent), Modifier.weight(1f))
    Switch(checked = urgent, onCheckedChange = null)
}
```

La fila es el interruptor: une el texto y lleva el papel y el estado. El `Switch` se queda solo para verse: con `onCheckedChange = null` no se pulsa ni es otra parada.

> 🔬 Medido en el ejercicio 01: con el `Switch` suelto, el nodo "Urgente" no tiene `Role` ni `ToggleableState`, y pulsar el texto no cambia nada (fallan `urgentIsOneStop` y `theWholeRowTurnsUrgentOn`). Con la fila `toggleable`, el árbol unido tiene dos nodos que se activan: "Seguir" y "Urgente".

## 3. Seleccionado

En el detalle de la Bitácora, el nivel se elige con tres botones, y nada decía cuál es el que tiene la observación. Un botón no es una casilla: se queda como botón, con `selected`:

```kotlin
Button(onClick = { ... }, Modifier.semantics { selected = level == observation.level }) { ... }
```

> 🔬 Medido en el detalle de A-1 (Alta), con `@Config(qualifiers = "es")`: los nodos de TalkBack de los tres botones llevan `stateDescription` "No seleccionado", "No seleccionado" y "Seleccionado".

## 4. Encabezados

TalkBack tiene un gesto para saltar de encabezado en encabezado: es como se recorre una pantalla larga sin pasar por cada parada. Un `Text` es encabezado con `semantics { heading() }`. En la Bitácora, el título de la sesión y el código del detalle.

> 🔬 Medido en la Bitácora: con `heading()`, el nodo del título pasa a `isHeading=true`.

## 5. Lo que cambia solo

Al añadir o borrar una observación, el resumen cambia ("3 observaciones" pasa a "2"), pero TalkBack está en otra parada y no se entera. Una **región viva** (`liveRegion`) se lee sola cuando cambia:

```kotlin
Text(resources.summary(observations), Modifier.semantics { liveRegion = LiveRegionMode.Polite })
```

`Polite` espera a que TalkBack termine lo que está leyendo; `Assertive` lo interrumpe, y es para lo urgente (un error que impide seguir). Con un `Snackbar` no hace falta: ya se anuncia solo.

> 🔬 Medido en la Bitácora: con `LiveRegionMode.Polite`, el nodo del resumen pasa a `liveRegion=1` (`ACCESSIBILITY_LIVE_REGION_POLITE`).

## ✅ Comprueba

- [ ] ¿Qué dice TalkBack de un `Box` con `toggleable(role = Role.Checkbox)` y `contentDescription = "Seguir"`?
- [ ] ¿Por qué el `Switch` lleva `onCheckedChange = null` dentro de una fila `toggleable`?
- [ ] ¿Cómo dice un botón que está seleccionado sin dejar de ser un botón?
- [ ] ¿Cuándo `Polite` y cuándo `Assertive`?
