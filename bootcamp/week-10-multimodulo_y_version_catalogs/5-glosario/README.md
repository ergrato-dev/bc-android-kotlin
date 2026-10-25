# Glosario — Semana 10

| Término | Definición |
| --- | --- |
| **alias** | Acceso `alias(libs.plugins.x)` a un plugin del catálogo |
| **api** | Configuración de dependencia cuyos tipos forman parte de la API pública del módulo: quien dependa de él la ve al compilar |
| **apply false** | Declara un plugin con su versión en el raíz sin aplicarlo, para que los módulos lo apliquen sin versión |
| **BOM** | *Bill of Materials*: un artefacto que fija las versiones de un conjunto de bibliotecas (`platform(...)`) |
| **Bundle** | Grupo de bibliotecas del catálogo que se añade con una sola línea (`libs.bundles.testing`) |
| **Catálogo de versiones** | Archivo `gradle/libs.versions.toml` donde se escriben las versiones, bibliotecas y plugins una sola vez |
| **Ciclo de dependencias** | Dos módulos que se necesitan mutuamente; Gradle lo rechaza |
| **Classpath de compilación / de ejecución** | Las dos listas de dependencias de un módulo: lo que ve al compilar y lo que lleva al ejecutar |
| **Compile avoidance** | Gradle no recompila un módulo si la interfaz pública de sus dependencias no cambió |
| **dependencyResolutionManagement** | Bloque de `settings.gradle.kts` que centraliza los repositorios |
| **FAIL_ON_PROJECT_REPOS** | Modo que hace fallar el build si un módulo declara sus propios repositorios |
| **implementation** | Configuración de dependencia interna: quien depende del módulo no la ve al compilar |
| **include** | Función de `settings.gradle.kts` que declara un módulo (`include(":core")`) |
| **Módulo (subproyecto)** | Carpeta con su `build.gradle.kts` que forma parte de un build mayor |
| **Proyecto raíz** | El proyecto que agrupa los módulos; el de `settings.gradle.kts` |
| **Ruta de proyecto** | Nombre de un módulo con dos puntos delante: `:core`, `:data` |
| **settings.gradle.kts** | Archivo que decide el nombre del proyecto, los módulos y los repositorios |
| **subprojects { }** | Bloque del raíz que configura todos los módulos a la vez; el convention plugin es mejor |
| **TOML** | Formato de texto del catálogo: secciones `[versions]`, `[libraries]`, `[bundles]`, `[plugins]` |
| **version.ref** | Referencia a una entrada de `[versions]` desde una biblioteca o un plugin |
