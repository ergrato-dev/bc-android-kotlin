// Paso 3: el módulo app (el programa). Plugins kotlin("jvm") y application, repositories { mavenCentral() },
//         el bloque kotlin { } de core, y:
//         - implementation(project(":data")): app usa data, y NO declara core a propósito (lo ve por el api de data)
//         - application { mainClass.set("MainKt") }
