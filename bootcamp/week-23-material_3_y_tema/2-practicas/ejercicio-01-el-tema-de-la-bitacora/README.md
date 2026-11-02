# Ejercicio 01 — El tema de la Bitácora

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

La Bitácora con sus colores: los de la marca, o los del fondo de pantalla desde Android 12; claros u oscuros como el sistema; una pantalla que de noche es oscura de verdad, también antes de que Compose pinte; y unos niveles que de noche no deslumbran. Todo comprobado en la JVM, píxel a píxel.

## Cómo está montado

`starter/` es una app Android de un solo módulo con Compose y Robolectric, como en la semana 22. `MainActivity` pinta `SessionScreen` (el título, las tres observaciones de ejemplo y "Añadir") dentro de `BitacoraTheme`. `Color.kt` trae ya hechos los esquemas de la marca, `LightColors` y `DarkColors`. `values/colors.xml` tiene los colores de los niveles de día. `BitacoraTheme` todavía no hace nada: deja el tema de ejemplo.

Los tests (`app/src/test/kotlin/dev/bitacora/ThemeTest.kt`, no los toques) llevan `@GraphicsMode(GraphicsMode.Mode.NATIVE)`: Robolectric pinta de verdad y `captureToImage()` da los píxeles (teoría 01). Al empezar, los 9 fallan.

## Pasos

1. **`BitacoraTheme`** (`Theme.kt`): con `dynamicColor` y desde Android 12 (`Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`), `dynamicDarkColorScheme(context)` o `dynamicLightColorScheme(context)` según `darkTheme`, con `LocalContext.current`; si no, `DarkColors` o `LightColors`. Pásalo a `MaterialTheme(colorScheme = ...)`.
2. **El `Surface`** (`SessionScreen.kt`): la raíz de la pantalla pasa a ser un `Surface(modifier.fillMaxSize())`, con la `Column` y su margen dentro.
3. **Los niveles** (`ObservationRow.kt`): el color de `LevelBadge` sale de `colorResource(...)`, con una propiedad privada `Level.color: Int` que devuelve `R.color.level_low`, `level_medium` o `level_high`. Crea `res/values-night/colors.xml` con los de noche: más oscuros, que se lea la letra encima (por ejemplo `#FF1E5B3F`, `#FF6B5300` y `#FF8C1D18`).
4. **La ventana**: `res/values/themes.xml` con `<style name="Theme.Bitacora" parent="android:Theme.Material.Light.NoActionBar" />`, `res/values-night/themes.xml` con el mismo nombre y `parent="android:Theme.Material.NoActionBar"`, y `android:theme="@style/Theme.Bitacora"` en la `<application>` del manifiesto.
5. **Ejecuta** `./gradlew test` (9 de 9) y `./gradlew build`, que pasa también lint.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la comprobación de `SDK_INT` del `when`. ¿Compila? ¿Qué dice `./gradlew :app:lintDebug`, y termina `./gradlew build`? ¿Qué pasaría en un Android 11? Restáuralo.
   - (b) Cambia el `Surface` por un `Box(modifier.fillMaxSize())`. ¿Qué test falla, y qué dos colores compara? ¿De qué color sale el texto, y por qué? En un móvil de verdad, con la ventana oscura del paso 4, ¿qué vería el usuario? Restáuralo.
   - (c) Pon `dynamicColor: Boolean = false` por defecto. ¿Qué test falla? ¿Qué colores compara? Restáuralo.
   - (d) Quita `values-night/colors.xml`. ¿Qué test falla, y con qué mensaje? Restáuralo.
   - (e) Quita `android:theme` del manifiesto. ¿Qué dos tests fallan? ¿Qué vería el usuario arriba de la pantalla? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Call requires API level 31 (current min is 26)` en lint | Falta la comprobación de `Build.VERSION.SDK_INT` en la rama del color dinámico (teoría 03) |
| `withoutDynamicColorItIsTheBrand` da `0.40392157, 0.3137255, 0.6431373` | Es `#6750A4`, el morado de ejemplo: `MaterialTheme` no recibe tu esquema |
| `theScreenIsDarkAtNight` da `0.98039216` | Falta el `Surface`: lo que se ve es la ventana de la actividad del test (teoría 02) |
| `thereIsNoActionBar` sigue fallando | El atributo es `android:theme` en `<application>`, y el `parent` lleva `NoActionBar` |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.
