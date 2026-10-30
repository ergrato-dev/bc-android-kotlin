# Glosario — Semana 17

| Término | Definición |
| --- | --- |
| **aapt2** | Herramienta del SDK que compila y enlaza los recursos, escribe `resources.arsc` y genera la clase `R` |
| **AdaptiveIconDrawable** | El icono adaptativo ya cargado: fondo, primer plano y, opcionalmente, versión monocroma |
| **androidResources** | Bloque de AGP para los recursos de la app: `localeFilters`, `generateLocaleConfig`... |
| **anydpi** | Calificador de densidad para recursos que valen para todas, como los vectores y el icono adaptativo |
| **b+es+419** | Calificador de idioma en formato BCP 47: español de Latinoamérica |
| **Calificador** | Lo que va detrás del guion en una carpeta de recursos (`-es`, `-land`, `-night`) y dice para qué configuración es |
| **CLDR** | Datos de Unicode con las reglas de cada idioma, entre ellas las formas de plural |
| **@Config(qualifiers)** | Anotación de Robolectric que fija la configuración del móvil en un test o en una clase |
| **Fusión de recursos** | Paso del build (`mergeDebugResources`) que junta los recursos de los source sets y las bibliotecas |
| **generateLocaleConfig** | Opción de AGP que genera la lista de idiomas de la app y la declara en el manifiesto |
| **getQuantityString** | Lee un plural: elige la forma según el número y el idioma |
| **Icono adaptativo** | Icono en capas que cada fabricante recorta con su forma y que el sistema puede animar o colorear |
| **lintDebug** | La tarea de lint de la variante debug; se para con los errores (`./gradlew lint` en AGP 9) |
| **lintVitalRelease** | Lint reducido que ejecuta `assembleRelease`; solo se para con los problemas fatales |
| **localeConfig** | Lista de idiomas de la app (`android:localeConfig`) que usan los ajustes de Android 13 o superior |
| **localeFilters** | Lista de idiomas que AGP deja en el APK; el resto, de la app y de las bibliotecas, se quita |
| **MissingTranslation** | Error de lint: un texto de `values/` sin traducir en un idioma que la app tiene |
| **mipmap** | Carpeta para el icono de la app |
| **monochrome** | Capa del icono adaptativo que Android 13 o superior pinta con los colores del sistema |
| **Plural** | Recurso (`<plurals>`) con una forma por categoría de cantidad: `one`, `many`, `other`... |
| **R** | Clase generada con un número por recurso del módulo; no incluye los de las bibliotecas |
| **resources.arsc** | La tabla de recursos del APK: cada recurso, sus configuraciones y sus valores |
| **resources.properties** | Archivo de `res/` que dice el idioma de `values/` (`unqualifiedResLocale`) |
| **Resources$NotFoundException** | Fallo al pedir un recurso que no tiene valor para la configuración actual ni en `values/` |
| **sw600dp** | Calificador de ancho mínimo: el lado corto de la pantalla mide 600 dp o más |
| **Valor por defecto** | El recurso de la carpeta sin calificadores (`values/`): lo que se usa cuando ninguna otra encaja |
