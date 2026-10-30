# Idiomas

> Traducir no es solo cambiar palabras. Hay textos con huecos para datos, plurales que cambian de forma según el idioma, regiones que hablan distinto y usuarios que quieren la app en un idioma diferente al del móvil. Android resuelve cada una de esas cosas, pero solo si la app le da la información.

## 🎯 Objetivos

- Escribir textos con formato y plurales que funcionen en cualquier idioma
- Organizar las traducciones por idioma y región
- Detectar traducciones que faltan con lint
- Dejar que el usuario elija el idioma de la app con `generateLocaleConfig`

## 1. Formato: el orden lo pone la traducción

```xml
<string name="title">Bitácora %1$s v%2$s</string>
```

`%1$s` es el primer argumento y `%2$s` el segundo. Numerarlos permite que una traducción los cambie de sitio sin tocar el código. Nunca construyas una frase pegando trozos en Kotlin (`getString(R.string.level) + ": " + count`): en otro idioma el orden o los signos pueden ser distintos. Una frase entera es un recurso entero.

## 2. Plurales

Cada idioma agrupa las cantidades a su manera. Android trae las reglas de **CLDR** (el estándar de Unicode) y elige la forma con `getQuantityString`:

| Idioma | Formas | Ejemplos |
| --- | --- | --- |
| Inglés | `one`, `other` | 1 observation; 0 y 2 observations |
| Español | `one`, `many`, `other` | 1 observación; 1000000 **de** observaciones; 0 y 2 observaciones |

```xml
<plurals name="observations">
    <item quantity="one">%d observación</item>
    <item quantity="many">%d de observaciones</item>
    <item quantity="other">%d observaciones</item>
</plurals>
```

> 🔬 Medido con Robolectric: en español, `1000000` usa `many`. Sin la línea `many`, Android usa `other` y escribe `1000000 observaciones`. El cero usa `other` en los dos idiomas: no es `zero`, que solo existe en idiomas como el árabe.

El número va dos veces en `getQuantityString(R.plurals.observations, count, count)`: el primero elige la forma, el segundo rellena `%d`.

## 3. Idioma y región

| Carpeta | Para quién |
| --- | --- |
| `values-es` | Todo el español |
| `values-es-rMX` | Español de México |
| `values-b+es+419` | Español de Latinoamérica (`419` es el código de la ONU para la región) |

`b+` es la forma moderna de escribir el idioma, con las etiquetas BCP 47 separadas por `+`. Es la única que admite regiones como `419`, que no son un país.

Una carpeta regional solo lleva lo que cambia. `values-b+es+419` tiene `celular` donde `values-es` dice `móvil`, y el resto lo hereda de `values-es`.

> 🔬 Medido con Robolectric, que simula Android 16: un móvil en `es-AR`, `es-MX` o `es-US` elige `values-b+es+419`; uno en `es-ES` elige `values-es`. Según la documentación, Android busca así la región más cercana desde Android 7. Si en lugar de `b+es+419` tienes `values-es-rMX`, Argentina también lo elige: es la variante más cercana que hay. Funciona, pero el nombre dice "México" cuando quieres decir "Latinoamérica".

## 4. Lo que falta: lint

Si `values/` tiene un texto que `values-es` no tiene, el usuario en español ve ese texto en el idioma por defecto, en medio de la pantalla traducida. Lint lo encuentra:

```text
.../res/values/strings.xml:4: Error: "only_default" is not translated in "es" (Spanish) [MissingTranslation]
```

> 🔬 Medido: `./gradlew lint` (en AGP 9, `lintDebug`) se para con este error. El `lintVitalRelease` que ejecuta `assembleRelease` solo comprueba los problemas fatales, y deja pasar este: el APK de release sale igual. Lo mide el ejercicio 02, paso 7e.

Si un texto no debe traducirse (un nombre propio, un formato técnico), márcalo con `translatable="false"` en `values/`.

## 5. El idioma de la app, no el del móvil

Desde Android 13, el usuario puede elegir el idioma de cada app en *Ajustes > Sistema > Idiomas > Idiomas de las aplicaciones*. Para que tu app aparezca ahí, el manifiesto tiene que declarar qué idiomas tiene. AGP lo genera:

```kotlin
android {
    androidResources {
        generateLocaleConfig = true
    }
}
```

AGP necesita saber de qué idioma son los textos de `values/`, y eso lo dice `res/resources.properties`:

```properties
unqualifiedResLocale=en-US
```

Sin ese archivo, el build se para:

```text
Execution failed for task ':app:extractDebugSupportedLocales'
> No resources.properties file found. See https://developer.android.com/r/studio-ui/build/automatic-per-app-languages
```

> 🔬 Medido en el ejercicio 02. Con el archivo, el APK lleva `res/xml/_generated_res_locale_config.xml` con `en-US`, `es` y `es-419`, y el manifiesto apunta a él con `android:localeConfig`. Solo lista los idiomas de **tu** app, no los de las bibliotecas.

Para cambiar de idioma desde dentro de la app, o en versiones anteriores a Android 13, existe `AppCompatDelegate.setApplicationLocales` (biblioteca `appcompat`). La Bitácora no lo necesita todavía.

## ✅ Comprueba

- [ ] ¿Por qué `%1$s` y no `%s`?
- [ ] ¿Qué forma de plural usa el español para 0? ¿Y para un millón?
- [ ] Tu app tiene `values` (inglés), `values-es` y `values-b+es+419`. ¿Qué carpeta usa un móvil en `es-CO`? ¿Y uno en `pt-BR`?
- [ ] ¿Qué pasa si lint encuentra una traducción que falta y solo ejecutas `assembleRelease`?
