// Paso 1: el convention plugin de las bibliotecas (core, data). Aplica id("bitacora.kotlin-base") y `java-library`,
//         y añade las dependencias de prueba sacándolas del catálogo. Aquí no existe `libs.junit.bom`: pide el catálogo
//         con val libs = versionCatalogs.named("libs") y usa libs.findLibrary("junit-bom").get() y
//         libs.findBundle("testing").get().
