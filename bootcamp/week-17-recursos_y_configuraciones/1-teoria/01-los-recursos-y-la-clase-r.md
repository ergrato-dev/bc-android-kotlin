# Los recursos y la clase R

> Hasta ahora, la Bitácora escribía sus textos en Kotlin: `"Descripción"`, `"LOW"`. Eso funciona en un solo idioma, con un solo tamaño de pantalla y en modo claro. Android separa lo que se muestra del código que lo usa. Los textos, colores, dimensiones e iconos van en `res/`, y el código solo los nombra.

## 🎯 Objetivos

- Conocer las carpetas de `res/` y qué tipo de recurso va en cada una
- Explicar qué es la clase `R` y por qué solo contiene los recursos de tu módulo
- Saber qué pasa cuando un recurso no tiene valor por defecto

## 1. `res/`

| Carpeta | Qué va | Cómo se usa |
| --- | --- | --- |
| `values/` | Textos (`<string>`), plurales (`<plurals>`), colores, dimensiones, números, booleanos. El nombre del archivo da igual: `strings.xml`, `colors.xml`... | `getString(R.string.title)` |
| `drawable/` | Imágenes: vectores XML, PNG, formas | `getDrawable(R.drawable.ic_launcher_foreground)` |
| `mipmap/` | El icono de la app (teoría 04) | `@mipmap/ic_launcher` en el manifiesto |
| `layout/` | Pantallas en XML. La Bitácora no las usa: crea las vistas en código hasta que llegue Compose, en la fase 4 | `setContentView(R.layout.main)` |
| `xml/` | XML de configuración que lee Android, como la lista de idiomas (teoría 03) | `@xml/...` |

Desde XML, un recurso se nombra con `@tipo/nombre` (`@string/app_name`). Desde Kotlin se nombra con `R.tipo.nombre`, que es un `Int`.

```xml
<resources>
    <string name="title">Bitácora %1$s v%2$s</string>
    <plurals name="observations">
        <item quantity="one">%d observación</item>
        <item quantity="other">%d observaciones</item>
    </plurals>
</resources>
```

```kotlin
title = getString(R.string.title, domain, version)
val total = resources.getQuantityString(R.plurals.observations, count, count)
```

## 2. De `res/` al APK

AGP le pasa `res/` a **aapt2**, la herramienta del SDK que ya viste en la semana 15. aapt2 trabaja en dos pasos:

1. **Compila** cada archivo a un formato binario.
2. **Enlaza** todos los de la app y sus bibliotecas. A cada recurso le da un número (`0x7f0b0001`), escribe la tabla `resources.arsc` dentro del APK y genera la clase `R` con esos números.

En el manifiesto del APK ya no queda ningún nombre de recurso, solo los números:

```text
android:label="@ref/0x7f0b0001"
android:icon="@ref/0x7f090000"
```

> 🔬 Medido con `apkanalyzer manifest print` en el ejercicio 02. `apkanalyzer resources configs --type string` lista para qué configuraciones hay textos, y `apkanalyzer resources value` lee el valor de cada una.

## 3. Una `R` por módulo

Cada módulo tiene su propia clase `R`, en su `namespace` (`dev.bitacora.R`). Con AGP moderno esa clase **no es transitiva**: solo tiene los recursos del módulo, no los de sus bibliotecas. `androidx.core` trae un texto que se llama `status_bar_notification_info_overflow`, pero `dev.bitacora.R` no lo tiene:

```text
e: .../Lib.kt:2:22 Unresolved reference 'status_bar_notification_info_overflow'.
```

> 🔬 Medido en la sonda de esta semana.

Un recurso de una biblioteca se nombra con la `R` de la biblioteca (`androidx.core.R.string...`). La razón que da Google es el tiempo de build: con `R` pequeñas, un recurso nuevo en un módulo no obliga a recompilar los módulos que dependen de él.

## 4. Sin valor por defecto

La carpeta `values/` sin calificadores es el **valor por defecto**: lo que Android usa cuando ninguna otra carpeta encaja con el móvil (teoría 02). Si falta, pasan dos cosas distintas según el tipo de recurso:

- **Un texto que solo está traducido** (`values-es`, por ejemplo) no llega a la `R` con la que se compila tu código: `Unresolved reference 'only_es'`.
- **Cualquier otro recurso** sin valor por defecto (un `integer` que solo está en `values-land`) compila, pero en la configuración que no lo tiene falla en ejecución:

```text
android.content.res.Resources$NotFoundException: Resource ID #0x7f060000
```

> 🔬 Medido en el ejercicio 01, paso 7e. En un móvil, esa excepción cierra la app, y solo para los usuarios con esa configuración.

La regla que se deduce: **todo recurso tiene valor en `values/`**, y las demás carpetas solo cambian lo que tiene que cambiar.

## ✅ Comprueba

- [ ] ¿Qué diferencia hay entre `@string/app_name` y `R.string.app_name`?
- [ ] ¿Por qué `dev.bitacora.R` no tiene los textos de AndroidX?
- [ ] Un compañero pone un `integer` solo en `values-land`. ¿Compila? ¿Quién ve el fallo, y cuándo?
