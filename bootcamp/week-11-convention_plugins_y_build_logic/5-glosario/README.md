# Glosario — Semana 11

| Término | Definición |
| --- | --- |
| **apply(from = ...)** | Aplica un script suelto a un proyecto; sin `plugins { }` ni accesos tipados. Lo sustituyen los convention plugins |
| **build-logic** | Nombre habitual del build incluido que contiene los convention plugins de un proyecto |
| **Build incluido** (*included build*) | Un build completo, con su `settings.gradle.kts`, que otro build usa; aquí, como fuente de plugins |
| **buildSrc** | Carpeta especial que Gradle compila siempre, de forma implícita, y cuyas clases ven todos los scripts del build |
| **Cargador de clases** (*classloader*) | Lo que carga las clases de un plugin; los módulos buscan primero en el del raíz |
| **Convention plugin** | Plugin propio que aplica las convenciones del equipo (JDK, opciones, pruebas) a un tipo de módulo |
| **findLibrary / findBundle** | Métodos de `VersionCatalog` que buscan una entrada por su nombre con guiones y devuelven un `Optional` |
| **Gradle Plugin Portal** | Repositorio de plugins de Gradle (`plugins.gradle.org`); el que usa `pluginManagement` por defecto |
| **Id de plugin** | Nombre con el que se pide un plugin: `id("bitacora.kotlin-base")`. En un precompiled script plugin, el nombre del archivo |
| **includeBuild** | Función de `pluginManagement { }` que incluye un build como fuente de plugins |
| **kotlin-dsl** | Plugin de Gradle, sin versión propia, que compila los `.gradle.kts` de `src/main/kotlin` como plugins |
| **kotlin-gradle-plugin** | El plugin de Kotlin pedido como biblioteca (`org.jetbrains.kotlin:kotlin-gradle-plugin`), para usarlo dentro de `build-logic` |
| **No value present** | Mensaje de `Optional.get()` vacío: un `findLibrary` con un nombre que no está en el catálogo |
| **orElseThrow** | Método de `Optional` que lanza la excepción que tú eliges si está vacío; da un mensaje mejor que `get()` |
| **Plugin** | Código que recibe un proyecto y lo configura: tareas, extensiones, configuraciones |
| **pluginManagement** | Bloque de `settings.gradle.kts` que decide de dónde salen los plugins; Gradle lo evalúa antes que el resto |
| **Precompiled script plugin** | Convention plugin escrito como archivo `.gradle.kts` y compilado por `kotlin-dsl` |
| **Script plugin** | Script `.gradle.kts` suelto que se aplica con `apply(from = ...)` |
| **versionCatalogs** | Extensión del proyecto con los catálogos del build; `versionCatalogs.named("libs")` lo pide por nombre |
| **VersionCatalog** | El catálogo visto desde código: `findLibrary`, `findBundle`, `findVersion`, `findPlugin` |
