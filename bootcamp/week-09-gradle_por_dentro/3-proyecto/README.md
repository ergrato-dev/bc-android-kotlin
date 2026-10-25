# Proyecto — El build de la Bitácora

⏱️ 4 h · Capa 8 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Tu Bitácora deja de compilarse "como sea" y pasa a tener un **build con identidad**: una versión y un dominio que viven en `gradle.properties`, una tarea propia que los escribe en un `build-info.properties` que el programa lee al arrancar, y una tarea `dist` que empaqueta el resultado. Todo cacheable, reproducible y compatible con la caché de configuración.

## Dónde trabajas

En **tu repositorio**, rama `week-09`. El `starter/` trae un proyecto mínimo (un `Main.kt` que lee `build-info.properties`, un `README.md` y los tests); **copia lo que necesites a tu proyecto** y usa el nombre de tu dominio. Lo nuevo está en `build.gradle.kts` (los pasos, en comentarios) y en `gradle.properties`.

Los tests ejecutan el build en una carpeta temporal (clase `Project`, ya hecha). Tu `Main.kt` sigue siendo tu Bitácora; lo único que necesita es leer `build-info.properties`, como el del `starter/`.

## Pasos

1. **Propiedades**: `bitacora.version` y `bitacora.domain` en `gradle.properties`, leídas con `providers.gradleProperty`. Si falta alguna, el build falla al configurar con `Falta la propiedad <nombre> en gradle.properties`.
2. **`GenerateBuildInfo`**: tarea propia con dos entradas (`appVersion`, `domain`) y una salida (`outputDir`). Escribe `build-info.properties` con `version=...` y `domain=...`.
3. **Regístrala** como `generateBuildInfo` y añade su carpeta a los recursos de `main`. `./gradlew run` imprime `Bitácora <dominio> v<versión>`.
4. **Reproducible**: no escribes nada, pero lo compruebas con un test (Gradle 9 ya lo hace por defecto).
5. **`dist`**: un zip con el jar, `build-info.properties` y `README.md`, sin carpetas.
6. **`printVersion`**: imprime `bitacora <versión>`, compatible con la caché de configuración.
7. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - el contenido exacto de `build-info.properties`,
   - `run` imprime la línea esperada,
   - `dist` es reproducible (mismo SHA-256 tras `clean` y tocar la fecha de una entrada),
   - cambiar el dominio regenera `generateBuildInfo` y **no** recompila `compileKotlin`,
   - una propiedad ausente falla con el mensaje exacto,
   - la caché de configuración se guarda y se reutiliza, y `printVersion` funciona con ella.
8. **Abre el pull request** `week-09` → `main` con la salida de `./gradlew test` y la de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, `register` y nunca `create`, entradas y salidas declaradas, ningún `project` dentro de una acción, tareas con grupo y descripción, tests con valores calculados a mano.

## Pistas

- `providers.gradleProperty("x")` devuelve un `Provider`; `.orNull` es `null` si no existe. Para el error: `?: error("...")`.
- `Property<String>` se asigna con `=`: `appVersion = bitacoraVersion`.
- El `@OutputDirectory` se escribe con `dir.mkdirs()` antes de crear el archivo.
- `sourceSets.main { resources.srcDir(tarea.map { it.outputDir }) }` conecta la tarea con los recursos: `processResources` pasa a depender de ella.
- En un test, `File.setLastModified(...)` cambia la fecha de un archivo sin cambiar su contenido.
- Para comprobar la caché de configuración: `Configuration cache entry stored` la primera vez y `Configuration cache entry reused` la segunda, en `result.output`.
- Para los saltos de línea de `build-info.properties`, recuerda el `\n` final de cada línea.
