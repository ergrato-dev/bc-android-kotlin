# Glosario — Semana 23

| Término | Definición |
| --- | --- |
| **colorResource** | Lee un color de los recursos en Compose, según la configuración del momento: de día o de noche |
| **ColorScheme** | La lista de roles de color de un tema de Material 3 |
| **contentColorFor** | Busca en el esquema la pareja `onX` de un color de fondo `X` |
| **Color dinámico** | Desde Android 12 (API 31), el esquema que el sistema calcula a partir del fondo de pantalla |
| **darkColorScheme / lightColorScheme** | Crean un esquema oscuro o claro; los roles que no se dan se quedan con el valor de ejemplo |
| **dynamicDarkColorScheme / dynamicLightColorScheme** | Devuelven el esquema del fondo de pantalla; solo existen desde la API 31 |
| **Escala de tipos** | Los 15 estilos de Material 3: `display`, `headline`, `title`, `body` y `label`, en tamaño grande, mediano y pequeño |
| **fontScale** | La escala de letra que elige el usuario; multiplica los `sp`, de forma no lineal desde Android 14 |
| **GetTextLayoutResult** | Acción de semántica que da el resultado de componer un texto, con su estilo; sirve para comprobar estilos en un test |
| **GraphicsMode.NATIVE** | Anotación de Robolectric que usa los gráficos nativos de Android: `captureToImage()` da píxeles de verdad |
| **ImeAction** | El botón de acción del teclado (`Done`, `Next`, `Search`...); lo que hace se decide en `keyboardActions` |
| **isError** | Parámetro de un campo de texto: lo pinta con el color de error y lo dice en la semántica; no impide escribir |
| **isSystemInDarkTheme** | Dice si el sistema está en modo oscuro, leyendo el `uiMode` de la configuración |
| **KeyboardOptions** | Cómo es el teclado de un campo: tipo, mayúsculas, acción |
| **LocalContentColor** | El color de lo que va encima, que dejan `Surface`, `Button`... para los `Text` y los iconos de dentro; negro fuera de todos |
| **LocalTextStyle** | El estilo de un `Text` sin `style`; `MaterialTheme` lo pone a `bodyLarge` |
| **MaterialTheme** | Pone un esquema de color, una tipografía y unas formas a disposición de todo lo que va dentro |
| **NewApi** | Error de lint: una llamada a una API más nueva que la `minSdk`, sin comprobar la versión |
| **NoActionBar** | Temas de la plataforma sin barra de acción, para apps que pintan su propio título |
| **OutlinedTextField** | Campo de texto de Material 3 con borde, etiqueta, texto de apoyo y estado de error |
| **Rol de color** | Un color con un papel (`primary`, `surface`, `error`...), no con un componente; los de encima son `onX` |
| **SegmentedButton** | Botón de una fila de opciones; en una `SingleChoiceSegmentedButtonRow` se elige una sola (`Role.RadioButton`) |
| **Shapes** | Los cinco tamaños de esquina del tema, de `extraSmall` a `extraLarge` |
| **sp** | Unidad del tamaño de letra: dp multiplicados por la escala de letra del usuario |
| **supportingText** | La línea debajo de un campo de texto, para una cuenta o una explicación |
| **Surface** | Pinta un fondo del esquema y deja la pareja en `LocalContentColor`; la raíz de cada pantalla |
| **values-night** | Carpeta de recursos que Android usa cuando el sistema está en modo oscuro |
| **Window background** | El fondo de la ventana, del tema del manifiesto: lo que se ve antes de que Compose pinte |
