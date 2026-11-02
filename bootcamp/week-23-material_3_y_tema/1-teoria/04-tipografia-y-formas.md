# Tipografía y formas

> El tema decide también cómo es el texto y cómo son las esquinas. Igual que con los colores, en Material 3 no se elige un tamaño de letra por pantalla sino un **papel**: esto es un título, esto es el cuerpo, esto es una etiqueta. Y el tamaño no es tuyo del todo: el usuario puede subir la letra del sistema, y tu pantalla tiene que aguantarlo. Esta teoría es la escala de tipos, lo que pasa con la letra grande y las formas.

## 🎯 Objetivos

- Elegir el estilo de un texto por su papel en la escala de tipos
- Explicar qué hace el tamaño de letra del sistema con `sp`, y por qué no se fija el alto de un texto
- Usar las formas del tema y comprobar en un test el estilo y las esquinas

## 1. La escala de tipos

`MaterialTheme.typography` tiene 15 estilos, cinco papeles en tres tamaños:

| Papel | Para qué | En la Bitácora |
| --- | --- | --- |
| `display` | Números o frases grandes, pocas | — |
| `headline` | El encabezado de una pantalla | — |
| `title` | Títulos de secciones, barras, diálogos | el título (`titleLarge`), el resumen (`titleMedium`) |
| `body` | El texto | la línea de cada observación (`bodyLarge`), la ubicación (`bodySmall`) |
| `label` | Botones, pestañas, etiquetas | lo pone el propio `Button` (`labelLarge`) |

> 🔬 Medido en la sonda: `bodyLarge` es 16 sp con 24 sp de alto de línea, `titleLarge` 22 sp, `labelLarge` 14 sp.

```kotlin
Text(title, style = MaterialTheme.typography.titleLarge)
```

Un `Text` sin `style` usa `LocalTextStyle`, que `MaterialTheme` pone a `bodyLarge`. Fuera de `MaterialTheme`, el estilo no tiene tamaño.

> 🔬 Medido en la sonda: con `MaterialTheme(typography = Typography(bodyLarge = TextStyle(fontSize = 18.sp)))`, un `Text` sin estilo mide 18 sp, y `titleLarge` sigue en 22: lo que no das, se queda como venía, como en `lightColorScheme`. Sin `MaterialTheme`, el `fontSize` del estilo es `Unspecified`.

La tipografía propia (una fuente de la marca) va en `res/font` y se pasa en `Typography(...)` con `FontFamily(Font(R.font.x))`. La Bitácora usa la del sistema.

## 2. La letra del sistema

Los tamaños de letra van en `sp`, no en `dp`: `sp` se multiplica por la escala de letra que el usuario elige en los ajustes de accesibilidad. Desde Android 14 la escala no es lineal: los textos grandes crecen menos que los pequeños, para que un título no se coma la pantalla.

> 🔬 Medido en la sonda, con `@Config(fontScale = 2f)`: un `Text` de una línea pasa de 16 a 30 dp de alto, no a 32; un `titleLarge`, de 36 a 45 dp. Un `OutlinedTextField` con texto de apoyo pasa de 118 a 130 dp.

Consecuencias:

- no fijes el alto de nada que tenga texto (`height(48.dp)` en una fila con dos líneas recorta la segunda con la letra grande). Usa `heightIn(min = ...)`, o deja que mida lo que mida;
- los tests de posición que dependen del alto de un texto valen para la escala 1: en tus tests, calcula con lo que no es texto (márgenes, insignias, botones) o fija la escala;
- prueba la pantalla al menos una vez con `fontScale = 2f`.

Robolectric mide el texto a su manera (teoría 01 de la semana 22): el alto de un texto en un test no es el del móvil. Compara estilos y posiciones de inicio, no altos de texto.

## 3. Comprobar el estilo en un test

El árbol de semántica no tiene el tamaño de letra, pero un texto deja pedir su `TextLayoutResult`, que lleva el estilo con el que se compuso:

```kotlin
val layout = mutableListOf<TextLayoutResult>()
rule.onNodeWithText("Bitácora agro v1.0.0").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
assertEquals(22.sp, layout.single().layoutInput.style.fontSize)
```

> 🔬 Medido en el proyecto: el título da 22 sp, el resumen 16 sp y la ubicación de una fila 12 sp. Para un texto dentro de una fila pulsable, `onNodeWithText(..., useUnmergedTree = true)`: la fila junta los textos de sus hijos (semana 22).

## 4. Las formas

`MaterialTheme.shapes` tiene cinco tamaños de esquina, de `extraSmall` a `extraLarge`. Los componentes eligen el suyo: un `Button` es una píldora.

> 🔬 Medido en la sonda: `shapes.small` es `RoundedCornerShape` de 8 dp; `shapes.medium`, de 12 dp. La forma de un `Button`, en el árbol de semántica, es `RoundedCornerShape` con `CornerSize(size = 50.0%)` en las cuatro esquinas.

Para dar forma a algo tuyo, recórtalo con `clip` antes de pintar el fondo:

```kotlin
Box(modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(colorResource(level.color)))
```

El orden importa como siempre (semana 22, teoría 02): `background` antes de `clip` pinta el cuadrado entero.

> 🔬 Medido en el proyecto: con `clip(shapes.small)`, el píxel (0, 0) de la insignia es el del fondo (`#FAFAFA` en el test) y el (2, 20), en el borde a media altura, es el del nivel (`#F4C7C3`).

## ✅ Comprueba

- [ ] El resumen de la sesión: ¿`titleMedium` o `bodyLarge`? ¿Por qué no `fontSize = 16.sp`?
- [ ] Con la letra al doble, ¿cuánto crece un texto de 16 sp? ¿Qué le pasa a una fila con `height(56.dp)` y dos líneas?
- [ ] ¿Por qué `clip` va antes que `background`?
