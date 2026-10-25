// Este es el build que editas. Se ejecuta a mano con:   ../gradlew -p sandbox <tarea>
// (desde la carpeta starter/), y los tests lo ejecutan en una copia temporal.
//
// Paso 1: una clase de tarea PROPIA, `CountLines : DefaultTask` (abstracta, dentro de este archivo):
//         - source: RegularFileProperty, entrada (@InputFile)
//         - report: RegularFileProperty, salida (@OutputFile)
//         - una acción (@TaskAction) que cuenta las líneas NO vacías de source, quita la primera (la cabecera) y
//           escribe "lines=<N>" y un salto de línea en report. Nunca negativo.
// Paso 2: registra `countObservations` (tipo CountLines): source = data/observations.csv (layout.projectDirectory),
//         report = build/reports/lines.txt (layout.buildDirectory). Con el archivo de ejemplo da lines=3.
// Paso 3: `summary` depende de countObservations e imprime "Resumen: <contenido del informe>" (sin salto
//         final). Lee el informe con un Provider (countObservations.flatMap { it.report }), no con `project`.
// Paso 4: la cabecera es configurable: una propiedad skipHeader: Property<Boolean>, ENTRADA (@Input), que sale
//         de -PskipHeader (providers.gradleProperty(...).map { it.toBoolean() }) y vale true si no se pasa.
//         Con -PskipHeader=false se cuenta también la cabecera (lines=4).
// Paso 5: hazla cacheable: @CacheableTask en la clase y @PathSensitive(PathSensitivity.NONE) en source. Después de
//         `clean`, un segundo `countObservations --build-cache` no se ejecuta: FROM_CACHE.

plugins {
    base
}
