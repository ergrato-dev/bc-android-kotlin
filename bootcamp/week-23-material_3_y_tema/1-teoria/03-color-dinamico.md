# Color dinámico

> Desde Android 12, el sistema saca una paleta del fondo de pantalla del usuario, y las apps pueden usarla: el teclado, los ajustes y tu app con los mismos tonos. Es una decisión de diseño, no técnica: la app se parece al móvil del usuario, y deja de parecerse a tu marca. Esta teoría explica cómo se pide, por qué necesita una comprobación de versión y qué colores no deberían cambiar nunca.

## 🎯 Objetivos

- Pedir el esquema dinámico y caer en el de la marca cuando no lo hay
- Leer el error de lint `NewApi` y arreglarlo con una comprobación de versión
- Decidir qué colores siguen el fondo de pantalla y cuáles no

## 1. El esquema dinámico

`dynamicLightColorScheme(context)` y `dynamicDarkColorScheme(context)` devuelven un `ColorScheme` entero, con los mismos roles que el de la marca, calculado por el sistema. Solo existen desde Android 12 (API 31):

```kotlin
@Composable
fun BitacoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

Es la forma que propone la plantilla de Android Studio. Por debajo de la API 31, la marca. `dynamicColor` vale `true` por defecto, y un test o una pantalla que necesite la marca pasa `false`.

## 2. NewApi

La Bitácora tiene `minSdk = 26`. Llamar a una API de la 31 sin comprobar la versión compila, porque se compila contra la `compileSdk` 37, pero en un Android 11 fallaría al ejecutarse. Lint lo para:

> 🔬 Medido en el ejercicio 01 (paso 6a): sin el `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`, `./gradlew :app:lintDebug` da `Error: Call requires API level 31 (current min is 26): dynamicLightColorScheme [NewApi]`, otro igual para `dynamicDarkColorScheme`, y `Lint found errors in the project; aborting build.` Es un error: `./gradlew build` no termina.

Lint reconoce la comprobación de `SDK_INT` en el mismo `when`: no hace falta `@RequiresApi` ni anotar nada más.

## 3. En Robolectric

El Android de los tests es el 16 (API 36): hay color dinámico, y Robolectric da una paleta propia.

> 🔬 Medido en la sonda: en Robolectric, `dynamicLightColorScheme(context).primary` es `#475D92` y `dynamicDarkColorScheme(context).primary` es `#B0C6FF`. No es el morado de ejemplo ni tu marca.

Así que un test que comprueba la marca tiene que pedirla: `BitacoraTheme(dynamicColor = false)`. Y un test del color dinámico no compara con `#475D92`, que es un detalle de Robolectric, sino con lo que diga `dynamicLightColorScheme(context)`:

```kotlin
assertEquals(dynamicLightColorScheme(context).primary, primary)
assertNotEquals(LightColors.primary, primary)
```

> 🔬 Medido en el ejercicio 01 (paso 6c): con `dynamicColor = false` por defecto, falla `onAndroid12AndUpTheColorsAreDynamic` con `expected:<Color(0.2784314, 0.3647059, 0.57254905, ...)> but was:<Color(0.23137255, 0.4117647, 0.22352941, ...)>`: el azul del fondo de pantalla de Robolectric contra el verde de la marca.

Para probar la rama de la API 26 a la 30 haría falta otro `android-all` (`@Config(sdk = [30])`), otra descarga de cientos de MB en la verificación. Esta semana no se hace: la rama es `darkTheme -> DarkColors`, y la prueban los tests con `dynamicColor = false`.

## 4. Qué no cambia

El color dinámico cambia los roles del tema. No debería cambiar lo que tiene significado:

- los colores de los niveles: "Alta" es rojiza con cualquier fondo de pantalla. Por eso son recursos (`colorResource`), con su versión de noche, y no roles del esquema ([teoría 02](02-modo-oscuro.md));
- el color de error, que el esquema dinámico también trae, pero que el usuario tiene que reconocer;
- el icono de la app, que es la marca en el escritorio.

> 🔬 Medido en el proyecto: con el color dinámico, la insignia de "Alta" sigue siendo `#F4C7C3` de día: sale de los recursos, no del esquema.

Si el dominio necesita colores que sigan el tema (un "aviso" que no es un error), Material propone añadir roles propios con un `CompositionLocal`. La Bitácora no los necesita.

## ✅ Comprueba

- [ ] ¿Por qué compila `dynamicLightColorScheme(context)` con `minSdk = 26`, y quién lo para?
- [ ] ¿Por qué un test de la marca pasa `dynamicColor = false`?
- [ ] ¿Qué colores de tu dominio no deberían seguir el fondo de pantalla?
