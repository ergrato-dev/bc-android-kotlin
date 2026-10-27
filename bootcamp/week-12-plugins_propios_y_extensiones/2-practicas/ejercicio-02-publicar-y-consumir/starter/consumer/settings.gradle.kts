// Paso 2: dile a este build de dónde salen los plugins: pluginManagement { repositories { maven { url = uri(...) } } },
//         con la carpeta que publicaste (relativa a consumer/). Solo esa: nada de gradlePluginPortal().

rootProject.name = "consumer"
