# Ejercicio 01 — Cada recurso en su carpeta

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

Dar a la app textos, plurales, colores y números distintos según el idioma, la región, el modo oscuro, la orientación y el tamaño de pantalla, sin escribir ni un `if` en Kotlin. Todo se resuelve con carpetas de recursos con calificadores. Los tests fijan la configuración del "móvil" y comprueban qué recurso elige Android.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Robolectric ya configurado como en la semana 16. En `app/src/main/res/values/` están los recursos por defecto, en inglés: `strings.xml` (con el plural `observations`), `colors.xml` e `integers.xml`. `MainActivity` solo lee recursos.

Los tests (`app/src/test/kotlin/dev/bitacora/ResourcesTest.kt`, no los toques) usan `@Config(qualifiers = "es")` para fijar la configuración de todo un test y `RuntimeEnvironment.setQualifiers(...)` para cambiarla a mitad. `theDefaultIsEnglish` empieza en verde y tiene que seguir así. Los otros 6 empiezan en rojo.

**Regla del ejercicio**: no se toca `values/` (salvo lo que pida `NOTAS.md`), ni Kotlin, ni los tests. Todo lo que añades son carpetas nuevas en `res/`.

## Pasos

1. **Español** (`values-es/strings.xml`): `app_name` = `Bitácora` y `sync_hint` = `Sincroniza cuando el móvil tenga cobertura`.
2. **Los plurales en español**, en el mismo archivo: `observations` con `one` (`%d observación`), `many` (`%d de observaciones`) y `other` (`%d observaciones`). Las cantidades que existen dependen del idioma: el español usa `many` para los millones (teoría 03).
3. **Latinoamérica** (`values-b+es+419/strings.xml`): solo `sync_hint`, con `celular` en lugar de `móvil`. El resto lo hereda de `values-es`.
4. **Modo oscuro** (`values-night/colors.xml`): `background` = `#121212`.
5. **Columnas**: `columns` = 2 en horizontal (`values-land`) y 3 en pantallas de al menos 600 dp de ancho mínimo (`values-sw600dp`). El test de tablet también gira: no crees `values-sw600dp-land` y comprueba qué carpeta gana.
6. **Ejecuta** `./gradlew test` (7 de 7).
7. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la línea `many` de `values-es`. ¿Qué test falla, con qué mensaje y por qué? Vuelve a ponerla.
   - (b) Renombra `values-b+es+419` a `values-es-rMX`. ¿Sigue recibiendo Argentina (`es-rAR`) la palabra `celular`? ¿Por qué? ¿Qué nombre describe mejor lo que quieres decir? Restáuralo.
   - (c) Crea `values-night-land/integers.xml` con `columns` = 5. ¿Qué dice el build? ¿Y si la carpeta se llama `values-land-night`? ¿Qué regla hay detrás? Bórrala.
   - (d) Con `sw600dp` y `land` a la vez, ¿por qué gana `values-sw600dp`?
   - (e) Borra `values/integers.xml`. ¿Compila? ¿Qué test falla y con qué excepción? Ahora haz lo mismo con un texto: añade `<string name="only_es">Solo es</string>` solo en `values-es` y úsalo desde Kotlin (`R.string.only_es`). ¿Qué cambia? Deja todo como estaba.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Invalid resource directory name` | Los calificadores van en un orden fijo: idioma antes que tamaño, tamaño antes que orientación, orientación antes que modo oscuro (teoría 02) |
| `Duplicate resources` | Has definido el mismo nombre dos veces en la misma carpeta, en dos archivos distintos |
| `latinAmericaSaysCelular` falla en `es-rES` | Has puesto `celular` en `values-es`; va en `values-b+es+419` |
| `latinAmericaSaysCelular` falla en `app_name` | Has copiado `values-es` entero en `b+es+419` con otro nombre de app; ahí solo va lo que cambia |
| Un test falla con `Resources$NotFoundException` | El recurso no tiene valor para esa configuración y tampoco en `values/` |
| `spanishTranslatesTheApp` falla en el título | El título de la actividad sale de `android:label` del manifiesto, que apunta a `@string/app_name` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.
