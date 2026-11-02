# Rúbrica de Evaluación — Semana 23: Material 3 y tema

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: el tema y el formulario |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es un rol de color? ¿Con qué roles pinta un `Button` su fondo y su texto, y qué regla siguen las parejas? | 10 |
| 2 | Defines `lightColorScheme(primary = Color(0xFF2E7D32))` y nada más. ¿De qué color quedan `onPrimary`, `primaryContainer` y `secondary`? ¿Cómo se define un esquema? | 10 |
| 3 | ¿De dónde saca su color un `Text` que no recibe `color`? ¿Qué pasa de noche si la pantalla no tiene un `Surface` en la raíz? | 10 |
| 4 | ¿Qué ve el usuario antes de que Compose pinte el primer fotograma? ¿Qué tenía la Bitácora de la semana 22 ahí, y cómo se arregla sin AppCompat? | 10 |
| 5 | Los colores de los niveles: ¿por qué no son roles del esquema? ¿Cómo tienen una versión de noche, y cómo se leen en Compose? | 10 |
| 6 | Escribe la condición del color dinámico. ¿Qué pasa si quitas la comprobación de la versión: compila, qué dice lint, y qué pasaría en un Android 11? | 10 |
| 7 | En un test de Robolectric, `BitacoraTheme {}` no da los colores de tu marca. ¿Por qué? ¿Cómo se prueba la marca, y cómo el color dinámico sin escribir un color de Robolectric? | 10 |
| 8 | ¿Qué estilo de la escala de tipos usa un `Text` sin `style` dentro de `MaterialTheme`? Con la letra del sistema al doble, ¿cuánto crece un texto de 16 sp, y qué no debes hacer por eso? | 10 |
| 9 | `isError = true` en un `OutlinedTextField`: ¿qué cambia para quien mira, qué para TalkBack, y qué no impide? ¿Qué pasa con `onDone`? | 10 |
| 10 | Elegir el nivel antes de guardar: ¿`Button`, `SegmentedButton` o `FilterChip`? ¿Qué `Role` tiene cada uno? ¿Por qué la regla de los 80 caracteres está en `core` y la comprueba también el `ViewModel`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Un color con un papel (`primary`, `surface`, `error`...), no con un componente. El `Button` pinta el fondo con `primary` y el texto con `onPrimary`: en la sonda, el borde es `#6750A4` y el centro de la letra `#FFFFFF`. Lo que va encima de un `X` se pinta con `onX`, y el contraste lo decide el esquema una vez.
2. `onPrimary`, `#FFFFFF`; `primaryContainer`, `#EADDFF`; `secondary`, `#625B71`. Lo que no se da se queda con el valor de ejemplo, morado. Un esquema se genera entero, en claro y en oscuro, con Material Theme Builder a partir de un color de la marca.
3. De `LocalContentColor`, que deja quien lo contiene (`Surface`, `Scaffold`, `Button`) con la pareja de su fondo. Fuera de un `Surface` vale `#000000` también de noche: sin él, texto negro sobre la ventana oscura. En el test, sin `Surface`, el fondo es el de la ventana de la actividad vacía, `#FAFAFA`, y el texto `#000000`; con él, `#141218` y `#E6E0E9` (los del esquema oscuro de ejemplo).
4. La ventana de la actividad, con el fondo de su tema (`android:theme`). La Bitácora no declaraba ninguno: tenía una barra de acción de la plataforma (`WindowDecorActionBar`) con el título repetido, y el fondo `#EEEDF4` de día y de noche. Se arregla con `Theme.Bitacora` en `values/themes.xml` (`android:Theme.Material.Light.NoActionBar`) y en `values-night/themes.xml` (`android:Theme.Material.NoActionBar`): sin barra de acción, fondo `#FAFAFA` de día y `#303030` de noche.
5. Porque tienen significado (lo grave que es una observación) y no deben cambiar con el fondo de pantalla. Son recursos: `values/colors.xml` y `values-night/colors.xml`, y se leen con `colorResource(R.color.level_high)`, que da `#F4C7C3` de día y `#8C1D18` de noche.
6. `dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`. Sin la comprobación compila, porque se compila contra la API 37. Lint da `Error: Call requires API level 31 (current min is 26): dynamicLightColorScheme [NewApi]` y para el build. En un Android 11 fallaría al ejecutarse: el método no existe.
7. Robolectric es Android 16: con `dynamicColor = true` por defecto, el esquema es el de su fondo de pantalla (`primary` `#475D92`). La marca se prueba con `dynamicColor = false`. El dinámico se compara con `dynamicLightColorScheme(context).primary` y con `assertNotEquals(LightColors.primary, ...)`, no con `#475D92`.
8. `bodyLarge` (16 sp, 24 sp de alto de línea), que `MaterialTheme` pone en `LocalTextStyle`. Con `fontScale = 2f`, un texto de una línea pasa de 16 a 30 dp en la sonda, no a 32: desde Android 14 la escala no es lineal. No se fija el alto de nada que tenga texto (`height(56.dp)` recorta), y los tests no dependen del alto de un texto.
9. Pinta el borde, la etiqueta y el texto de apoyo con `error`. TalkBack lee `Error = 'Entrada no válida'` (en inglés, `'Invalid input'`). No impide escribir ni guardar: `onDone` se llama igual, y por eso lleva `if (canSave)`.
10. `SegmentedButton`, en una `SingleChoiceSegmentedButtonRow`: una sola elección que luego se guarda (`Role.RadioButton`, `Selected`). `Button` es una acción (`Role.Button`); `FilterChip`, un filtro que se suma (`Role.Checkbox`). La regla es del dominio, así que vive en `core`. La pantalla avisa antes, pero el `ViewModel` es quien guarda: si no la comprueba, una llamada que no pase por la pantalla la salta.

**Ejercicio 01, paso 6**: (a) Compila. `lintDebug` da dos errores, `Call requires API level 31 (current min is 26): dynamicDarkColorScheme [NewApi]` y lo mismo para `dynamicLightColorScheme`, y `Lint found errors in the project; aborting build.`: `build` no termina. En un Android 11, al abrir la app, el método no existiría. (b) `theScreenIsDarkAtNight`: `expected:<Color(0.0627451, 0.078431375, 0.05882353, ...)> but was:<Color(0.98039216, ...)>`, el `surface` oscuro de la marca (`#10140F`) contra `#FAFAFA`, el fondo de la actividad vacía del test. El texto sale negro: fuera de un `Surface`, `LocalContentColor` es `#000000`. En el móvil, con la ventana a `#303030`, texto negro sobre gris oscuro. (c) `onAndroid12AndUpTheColorsAreDynamic`: `expected:<Color(0.2784314, 0.3647059, 0.57254905, ...)> but was:<Color(0.23137255, 0.4117647, 0.22352941, ...)>`, el azul del fondo de pantalla de Robolectric contra el verde de la marca. (d) `theBadgeHasItsNightColor`: `Values should be different. Actual: Color(0.95686275, 0.78039217, 0.7647059, ...)`: de noche, el mismo `#F4C7C3`. (e) `thereIsNoActionBar` (`expected null, but was:<com.android.internal.app.WindowDecorActionBar@...>`) y `theWindowIsDarkAtNight` (`Expected value to be true.`). Arriba, la barra de acción de la plataforma con el nombre de la app.

**Ejercicio 02, paso 6**: (a) `doneDoesNotSaveWhatCannotBeSaved`: `expected:<0> but was:<1>`. Con el teclado podría guardar una descripción sin nivel, o de más de 80, aunque el botón esté apagado. (b) Ningún test falla: cambia el aspecto (relleno en vez de borde), no la semántica. Los tests comprueban lo que hace el formulario, no cómo se ve. (c) `theLevelsAreOneChoice`: `Failed to assert the following: (Role = 'RadioButton')`, y el nodo tiene `Role = 'Checkbox'`. TalkBack anunciaría tres casillas que se pueden marcar a la vez. (d) `"Entrada no válida"`; sin el calificador, `"Invalid input"`. (e) Crece, pero menos del doble: la escala no es lineal desde Android 14 y, en el campo, el borde y los márgenes son dp, no sp.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El tema de la Bitácora (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| La comprobación de versión en la rama dinámica; el `Surface` en la raíz; los niveles con `colorResource`; el tema de la ventana en `values` y `values-night`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — El formulario de una observación (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| `canSave` calculado una vez y usado en el botón y en `onDone`; `SegmentedButton` con `itemShape`; estilos del tema, sin `fontSize` a mano (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con los dos tests propios | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — El tema y el formulario

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 22 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: el fondo de día, los estilos, el error en español, la insignia recortada, el dinámico oscuro, el límite de 80 | 25 |
| Tema | El esquema entero de tu marca, claro y oscuro; el dinámico con la comprobación de versión; el `Surface` en la raíz; la ventana sin barra de acción, de día y de noche | 25 |
| Formulario | `OutlinedTextField` con error y cuenta solo cuando sobra; los niveles apagados sin una descripción válida; `MAX_DESCRIPTION` en `core` y comprobado en el `ViewModel` | 10 |
| Adaptación al dominio | Los colores de tu marca y de tus niveles, de día y de noche; README con lo que pide el paso 10 | 20 |
| Entrega | Rama `week-23`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `lightColorScheme` con solo `primary`, o colores a mano (`Color(...)`) en los componentes en vez de roles | -10 |
| Los colores de los niveles en el esquema, o sin versión de noche | -10 |
| `@SuppressLint("NewApi")` o `@RequiresApi` para callar a lint en vez de comprobar la versión | -10 |
| `fontSize` escrito a mano donde hay un estilo de la escala | -5 |
| La regla de los 80 caracteres solo en la pantalla | -10 |
| Un test de la semana 22 o del starter borrado o debilitado para que pase | -20 |
| `core` o `data` dependen de Android o de Compose | -10 |
| Una dependencia nueva que no hacía falta (AppCompat, Material Components para Views...), o un sha256 añadido a mano | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-23` → `main` en el repositorio del aprendiz.
