// Un build que usa el plugin publicado, como lo usaría cualquier otro equipo.
// Paso 3: añade el plugin con su id y su versión: id("bitacora.build-info") version "1.0.0".
plugins {
    java
}

version = "3.1.0"

// Paso 3 (continuación): configura la extensión: buildInfo { domain = "obra" }.
