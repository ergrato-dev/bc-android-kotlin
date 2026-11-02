# Animar sin recomponer

> Una animación cambia un valor 60 o 120 veces por segundo. Si cada cambio hace que Compose vuelva a ejecutar la función que lo usa, la animación cuesta mucho más de lo que se ve. Esta teoría es dónde se lee un valor animado: en la composición, en la medida o al dibujar. Y lo que no hay que animar.

## 🎯 Objetivos

- Explicar las tres fases de un fotograma de Compose
- Leer un valor animado en la fase de dibujo con `drawBehind` o `graphicsLayer`
- Decidir qué se anima y qué no

## 1. Tres fases

Cada fotograma, Compose hace tres cosas, en orden:

1. **Composición**: ejecuta las funciones `@Composable` cuyo estado ha cambiado, y decide qué hay.
2. **Medida y colocación**: cuánto ocupa cada cosa y dónde va.
3. **Dibujo**: pinta.

Un estado que se lee en una fase solo invalida esa fase y las siguientes. Si un color se lee en la composición, cada cambio recompone; si se lee al dibujar, solo vuelve a pintar.

## 2. Dónde se lee

```kotlin
// En la composición: el valor se lee al llamar a background.
val color by animateColorAsState(target, tween(300))
Box(Modifier.background(color))

// Al dibujar: el State se lee dentro de la lambda de drawBehind.
val color = animateColorAsState(target, tween(300))
Box(Modifier.drawBehind { drawRect(color.value) })
```

La diferencia es dónde se hace `.value`. Con `by`, se lee en la línea de `val`, durante la composición. Sin `by`, se guarda el `State`, y se lee en la lambda, que Compose ejecuta al dibujar.

> 🔬 Medido en la sonda, con un contador en cada función: durante una animación de 300 ms, la caja con `background(color)` se recompone 19 veces; la de `drawBehind { drawRect(color.value) }`, ninguna.

> 🔬 Medido en el ejercicio 01 (paso 5a): con `background(color.value)` en vez de `drawBehind`, los 8 tests siguen en verde. Los tests ven el resultado, no lo que cuesta.

Lo mismo con lo demás que se anima:

| Se anima | En la composición | Sin recomponer |
| --- | --- | --- |
| Color de fondo | `background(color)` | `drawBehind { drawRect(color.value) }` |
| Opacidad | `alpha(alpha)` | `graphicsLayer { alpha = a.value }` |
| Desplazamiento | `offset(x.dp, 0.dp)` | `offset { IntOffset(x.value, 0) }` |
| Escala, giro | `scale(s)` | `graphicsLayer { scaleX = s.value }` |

`AnimatedVisibility`, `animateItem` y `SwipeToDismissBox` ya lo hacen así por dentro.

## 3. Qué no animar

- **Lo que no dice nada.** Una animación explica un cambio: que algo apareció, se fue o pasó a otro estado. Si no hay cambio que explicar, sobra.
- **Lo que tarda.** Entre 150 y 300 ms para un cambio pequeño. Una animación que hace esperar al usuario para seguir es un estorbo.
- **Lo que es la única señal.** Con "Quitar animaciones", la escala es 0 y Compose salta al final ([teoría 01](01-animar-un-valor-y-el-reloj.md)). El texto, el color final o el aviso tienen que decir lo que pasó.

## ✅ Comprueba

- [ ] ¿En qué fase se lee `color` en `Modifier.background(color)`? ¿Y `color.value` dentro de `drawBehind`?
- [ ] ¿Por qué `val color by animateColorAsState(...)` con `drawBehind { drawRect(color) }` sigue recomponiendo?
- [ ] Una animación de 800 ms al abrir cada observación: ¿qué le dirías a quien la propone?
