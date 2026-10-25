// Paso 2: el módulo data. Parte de la estructura de core y añade:
//         - los plugins kotlin("jvm"), kotlin("plugin.serialization") y `java-library`
//         - api(project(":core")): data muestra los tipos de core en su API (encode/decode los usan), así que
//           quien dependa de data tiene que ver core también
//         - implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.12.0")) y
//           implementation("org.jetbrains.kotlinx:kotlinx-serialization-json"): un detalle interno, no se expone
//         - las mismas dependencias de prueba, el bloque kotlin { } y tasks.test de core
