plugins {
    id("bitacora.kotlin-base")
    application
}

// Paso 4: todo programa de la Bitácora lleva su build-info. BuildInfoPlugin vive en este mismo build, así que no se
//         puede pedir en plugins { } (teoría 01): se aplica por su clase, aquí, con pluginManager.apply(...).
