plugins {
    id("bitacora.kotlin-base")
    application
}

// Un plugin binario de este mismo build no se puede pedir en plugins { }: se aplica por su clase.
pluginManager.apply(BuildInfoPlugin::class.java)
