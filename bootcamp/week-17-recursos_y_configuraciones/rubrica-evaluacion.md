# Rúbrica de Evaluación — Semana 17: Recursos y configuraciones

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora habla tu idioma |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué hace aapt2 con `res/`, y qué son los números que aparecen en el manifiesto del APK? | 10 |
| 2 | ¿Por qué `dev.bitacora.R` no tiene los recursos de AndroidX? ¿Cómo usarías uno? | 10 |
| 3 | ¿Qué pasa si un texto solo está en `values-es`? ¿Y si un `integer` solo está en `values-land`? | 10 |
| 4 | ¿Por qué `values-night-land` no es un nombre válido? | 10 |
| 5 | Tablet en horizontal, con `values-land` y `values-sw600dp`. ¿Cuál gana y por qué? | 10 |
| 6 | ¿Qué carpeta elige un móvil en `es-AR` entre `values`, `values-es` y `values-b+es+419`? ¿Y uno en `es-ES`? | 10 |
| 7 | ¿Qué formas de plural usa el español y qué pasa si falta una? | 10 |
| 8 | ¿Qué hacen `generateLocaleConfig` y `resources.properties`, y qué gana el usuario? | 10 |
| 9 | ¿Qué capas tiene un icono adaptativo y para qué sirve `monochrome`? | 10 |
| 10 | ¿Por qué `localeFilters += listOf("en", "es")` puede romper una app con `values-b+es+419`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Compila cada recurso a binario y después los enlaza todos, los de la app y los de sus bibliotecas: da un número a cada recurso, escribe `resources.arsc` en el APK y genera la clase `R`. En el manifiesto compilado, `@string/app_name` se convierte en una referencia a su número (`@ref/0x7f0b0001`).
2. Porque la `R` de cada módulo no es transitiva: solo tiene los recursos del propio módulo. Uno de una biblioteca se nombra con la `R` de la biblioteca (`androidx.core.R.string...`). Así un recurso nuevo no obliga a recompilar los módulos que dependen de ese.
3. Un texto solo traducido no llega a la `R` de compilación: `Unresolved reference`. El `integer` sí compila, pero en la configuración que no lo tiene (vertical) falla en ejecución con `Resources$NotFoundException`. Todo recurso necesita valor en `values/`.
4. Los calificadores van en un orden fijo y la orientación va antes que el modo oscuro: `values-land-night`. En otro orden, el build se para con `Invalid resource directory name`.
5. `values-sw600dp`: Android recorre los calificadores en orden y el ancho mínimo va antes que la orientación. Al encontrar una carpeta con `sw600dp`, descarta las que no lo tienen, incluida `values-land`. Para otra cosa en la tablet en horizontal hace falta `values-sw600dp-land`.
6. `es-AR`: `values-b+es+419`, porque Argentina está en el español de Latinoamérica, la región más cercana disponible. `es-ES`: `values-es`.
7. `one`, `many` (los millones: "1000000 de observaciones") y `other`. Si falta `many`, Android usa `other` y escribe "1000000 observaciones". El cero usa `other`.
8. `generateLocaleConfig` hace que AGP genere la lista de idiomas de la app (`res/xml/_generated_res_locale_config.xml`) y la declare en el manifiesto con `android:localeConfig`. `resources.properties` (`unqualifiedResLocale`) dice de qué idioma es `values/`; sin él, el build se para. El usuario de Android 13 o superior puede elegir en los ajustes el idioma de la app, distinto del de su móvil.
9. `background` (un color o una imagen opaca), `foreground` (el dibujo, transparente) y `monochrome` (opcional, una silueta). Android recorta y anima las dos primeras con la forma del fabricante. Con los iconos temáticos de Android 13 o superior, el sistema pinta `monochrome` con los colores del fondo de pantalla.
10. Porque un idioma en el filtro no incluye sus regiones: `es-r419` desaparece del APK, y un usuario de Latinoamérica ve los textos de `values-es` ("móvil" en lugar de "celular"). Hay que añadir `b+es+419` al filtro.

**Ejercicio 01, paso 7**: (a) Falla `spanishHasItsOwnPlurals` con `expected:<1000000 [de ]observaciones> but was:<1000000 []observaciones>`: sin `many`, Android usa `other`. (b) Sí: Android busca la región más cercana del mismo idioma, y México y Argentina son las dos de Latinoamérica. Pero `es-rMX` dice "México", y el texto es para toda Latinoamérica: `b+es+419` lo dice bien. (c) `values-night-land` da `Invalid resource directory name`; `values-land-night` funciona. Los calificadores tienen un orden fijo, y la orientación va antes que el modo oscuro. (d) Porque el ancho mínimo va antes que la orientación en ese orden: Android descarta primero las carpetas sin `sw600dp`. (e) Sin `values/integers.xml` compila, pero `landscapeHasTwoColumns` falla con `Resources$NotFoundException` al leer las columnas en vertical. El texto solo en `values-es` no compila: `Unresolved reference 'only_es'`. Un texto sin valor por defecto no llega a la `R` de compilación; otro recurso sí, y falla en ejecución.

**Ejercicio 02, paso 7**: (a) 87 configuraciones de texto: casi todas son los textos de `androidx.core`, que entran con `androidx.activity`. El `localeConfig` lista solo los de la app: `en-US`, `es` y `es-419`. (b) Pierde `es-r419`; un usuario de Argentina ve "Sincroniza cuando el móvil tenga cobertura". (c) `Bitácora`: el `values/` de debug sustituye al `values/` de main, pero el móvil en español usa `values-es`, que solo existe en main. Haría falta `src/debug/res/values-es/strings.xml`. (d) Lint avisa con `[MonochromeLauncherIcon]`, que no para el build. Con los iconos temáticos activados, el icono no se pinta con los colores del sistema como los demás. (e) No falla: `lintVitalRelease` solo comprueba los problemas fatales, y `MissingTranslation` es un error, no fatal. `lintDebug` sí se para.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Cada recurso en su carpeta (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Ni Kotlin ni `values/` tocados; las carpetas regionales solo con lo que cambia (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Idiomas e icono en el APK (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Icono con las tres capas; lint sin errores y sin `tools:ignore` (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora habla tu idioma

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: botones y pista, plural, segundo idioma, idioma sin traducir, `localeConfig`, icono, lint | 25 |
| Recursos | Ningún texto de la interfaz en Kotlin; formato con argumentos numerados; plurales con todas las formas del idioma; traducción completa | 20 |
| Build | `generateLocaleConfig` en `bitacora.android-application`; `localeFilters` en la app con las regiones; icono adaptativo con `monochrome`; verificación regenerada por Gradle | 15 |
| Adaptación al dominio | Textos en el idioma del dominio; README con los idiomas del APK y lo que pide el paso 10 | 20 |
| Entrega | Rama `week-17`, pull request con `test` y lo que ha cambiado en la verificación | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una frase construida pegando trozos en Kotlin | -10 |
| `translatable="false"` o `tools:ignore="MissingTranslation"` para que lint no se queje | -10 |
| `core` o `data` dependen de Android para los textos | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-17` → `main` en el repositorio del aprendiz.
