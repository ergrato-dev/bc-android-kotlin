// Paso 5: el módulo data. Lo mismo que core, más: el plugin alias(libs.plugins.kotlin.serialization),
//         api(project(":core")) (sus funciones usan los tipos de core), y
//         implementation(platform(libs.serialization.bom)) + implementation(libs.serialization.json)
//         (un detalle interno: no se expone).
