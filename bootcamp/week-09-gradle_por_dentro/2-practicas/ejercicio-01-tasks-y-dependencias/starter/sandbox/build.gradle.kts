// Este es el build que editas. Se ejecuta a mano con:   ../gradlew -p sandbox <tarea>
// (desde la carpeta starter/), y los tests lo ejecutan en una copia temporal.
//
// Paso 1: `hello`. Imprime "Hola desde Gradle" con doLast. Dale group = "bitacora" y una description.
// Paso 2: `stage` (tipo Copy). Copia SOLO los .csv de data/ a build/stage, renombrados a obs-<nombre>
//         (a.csv -> obs-a.csv). Usa layout.buildDirectory para el destino.
// Paso 3: `bundle` (tipo Zip). Empaqueta lo que produce `stage` en build/dist/bitacora-<version>.zip. Usa
//         from(stage): así Gradle sabe, sin que lo digas, que bundle depende de stage.
//         No hace falta que lo configures: desde Gradle 9 los archivos son reproducibles por defecto.
// Paso 4: `announce`. Imprime "Paquete listo: <nombre del zip>". Tiene que ejecutarse SIEMPRE justo después de
//         bundle, aunque solo pidas `bundle` (finalizedBy). Si se pasa -Pquiet, se OMITE (onlyIf con
//         providers.gradleProperty, no con project.hasProperty).
// Paso 5: `release`: una tarea de ciclo de vida, sin acciones, que depende de bundle.
// Paso 6: `reset` (tipo Delete): borra la carpeta build.

version = "1.0.0"
