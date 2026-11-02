# Proyecto — El tema y el formulario

⏱️ 4 h 30 min · Capa 22 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 22, la Bitácora pintaba con el morado de ejemplo de Material, encima de una barra de acción de la plataforma que repetía el título, y de noche seguía siendo blanca. Esta semana tiene tema: los colores de tu marca (o los del fondo de pantalla, desde Android 12), claros u oscuros como el sistema, también en la ventana que se ve antes de que Compose pinte. Los niveles tienen su color de día y de noche, que no cambia con el fondo de pantalla. El texto tiene su papel en la escala de tipos. Y el formulario avisa antes de guardar: con una descripción vacía, los niveles no se pueden pulsar; con una de más de 80 caracteres, el campo marca el error y enseña la cuenta.

El `ViewModel` solo cambia en una línea: tampoco guarda una descripción de más de 80.

## Dónde trabajas

En **tu repositorio**, rama `week-23`, a partir de lo que entregaste en la semana 22. El `starter/` es la Bitácora de la semana 22 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya hechos:

- `Color.kt`: los esquemas `LightColors` y `DarkColors` de la Bitácora agro, generados a partir de un verde;
- `res/values/colors.xml`: los colores de los niveles, de día;
- `MAX_DESCRIPTION` (80), en `core`;
- `Theme.kt`, con `BitacoraTheme` vacío: deja el tema de ejemplo.

Y tests nuevos, para los dos sabores:

- `app/src/test/kotlin/dev/bitacora/ThemeTest.kt`: la marca, el oscuro, el color dinámico, el fondo de la pantalla y de la insignia de noche, píxel a píxel (`@GraphicsMode(NATIVE)`), y la ventana sin barra de acción y oscura de noche;
- `app/src/test/kotlin/dev/bitacora/FormTest.kt`: los niveles apagados sin descripción, el error y la cuenta con 81 caracteres, el título en `titleLarge`, y el `ViewModel` que no guarda una descripción demasiado larga.

Con la Bitácora de la semana 22, fallan 12 de 24 en `testDemoDebugUnitTest` y 12 de 43 en `testFieldDebugUnitTest`. Los demás, incluidos dos de los nuevos, siguen en verde. Copia esos tests a tu repositorio.

## Pasos

1. **La marca de tu dominio**: genera tus esquemas en [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) a partir de un color de tu dominio y sustituye los de `Color.kt`, con los 19 roles de la Bitácora agro (teoría 01). Apunta el color de partida en el README.
2. **La ventana**: `res/values/themes.xml` con `Theme.Bitacora` (`parent="android:Theme.Material.Light.NoActionBar"`), `res/values-night/themes.xml` con el mismo nombre y `android:Theme.Material.NoActionBar`, y `android:theme="@style/Theme.Bitacora"` en la `<application>` (teoría 02).
3. **El tema**: `BitacoraTheme` elige el esquema dinámico con `dynamicColor` y la API 31 o más, y si no, el de la marca, claro u oscuro (teoría 03). En `MainActivity`, `BitacoraScreen` va dentro de `BitacoraTheme`.
4. **Los niveles y la fila** (`Levels.kt`, `ObservationRow.kt`): `internal val Level.color: Int` con `R.color.level_*`, como `label` y `short`. La insignia, con `clip(MaterialTheme.shapes.small)` y `background(colorResource(level.color))`, en ese orden. `res/values-night/colors.xml` con los colores de noche de tu dominio. La ubicación de la fila, con `bodySmall` y `color = MaterialTheme.colorScheme.onSurfaceVariant`.
5. **La pantalla** (`BitacoraScreen.kt`): la raíz es un `Surface(Modifier.fillMaxSize())` con la `Column` y su margen dentro. El título, con `titleLarge`; el resumen, con `titleMedium`.
6. **El formulario**: el campo pasa a `OutlinedTextField`, con `KeyboardCapitalization.Sentences`. Con `val tooLong = state.draft.length > MAX_DESCRIPTION`: `isError = tooLong` y, solo si `tooLong`, `supportingText` con la cuenta (`R.string.counter`, `"81 / 80"`, en `values/strings.xml` con `translatable="false"`). Los niveles, con `enabled = state.draft.isNotBlank() && !tooLong`.
7. **El `ViewModel`**: `add` devuelve `null` también con más de `MAX_DESCRIPTION`.
8. **Los tests**: `./gradlew :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug`, hasta el verde. Después, `./gradlew build`. No entra ninguna dependencia nueva: `material3` ya traía todo, y los gráficos nativos de Robolectric ya estaban en la verificación desde la semana 16. Si `verification-metadata.xml` pide algo, has añadido una biblioteca que no hacía falta.
9. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y la cuenta o el porqué en un comentario (en `app/src/test`):
   - de día y sin color dinámico, el fondo de la pantalla en (160, 350) es `LightColors.surface`;
   - la ubicación de una fila mide 12 sp (`bodySmall`) y el resumen 16 sp (`titleMedium`), con `GetTextLayoutResult` (teoría 04);
   - con 81 caracteres, el error de la semántica dice `"Entrada no válida"` en español;
   - la esquina (0, 0) de la insignia no es del color del nivel y el píxel (2, 20) sí: la insignia está recortada;
   - con `darkTheme = true` y el color dinámico, `primary` es el de `dynamicDarkColorScheme(context)`;
   - el `ViewModel` guarda una descripción de exactamente 80 caracteres.
10. **Explica en tu README** qué colores de tu dominio siguen el fondo de pantalla y cuáles no, y por qué; qué veía el usuario de noche antes del paso 2 y del paso 5; y por qué la regla de los 80 caracteres está en `core` y la comprueban la pantalla y el `ViewModel`.
11. **Abre el pull request** `week-23` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, el esquema entero de tu marca, el color dinámico con su comprobación de versión, el `Surface` en la raíz, la ventana de día y de noche, los niveles en recursos, el formulario que avisa, y tests con valores esperados escritos a mano.

## Pistas

- Robolectric tiene Android 16: con `dynamicColor = true` (el valor por defecto), los colores son los de su fondo de pantalla, no los tuyos. Los tests de la marca pasan `dynamicColor = false` (teoría 03).
- Para un test de noche y en español, `@Config(qualifiers = "es-night")`.
- `theScreenIsDarkAtNight` mira el píxel (160, 350), en medio de la lista vacía: si da `0.98039216`, falta el `Surface`.
- No añadas una `TopAppBar` ni dejes la cuenta siempre visible: en los 470 dp de alto de Robolectric no queda sitio para la segunda fila y falla `aRowShowsItsBadgeAndPlace`, de la semana 22 (teoría 05). La `TopAppBar` es además experimental en Material 3 1.4.0.
- `LanguagesTest` sigue buscando los botones por su papel: los niveles apagados siguen siendo botones, y en `field`, con el borrador vacío, están apagados.
- `./gradlew :app:testFieldDebugUnitTest --tests '*ThemeTest'` para ir test a test.
