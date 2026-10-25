# Proyecto — La Bitácora en tres módulos

⏱️ 4 h · Capa 9 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Tu Bitácora, que era un solo proyecto, pasa a ser un build de **tres módulos** con un **catálogo de versiones** y los **repositorios centralizados**: `core` (el modelo, las reglas y los informes), `data` (el archivo JSON versionado de la semana 8) y `app` (el programa). Y aprendes a **probar el propio build**.

## Dónde trabajas

En **tu repositorio**, rama `week-10`. El `starter/` trae el esqueleto: el raíz que ejecuta los tests de TestKit (`src/test`, ya hecho), el código mínimo de cada módulo, un catálogo a medias y los `build.gradle.kts` de los módulos con los pasos en comentarios. **Copia tu código de las semanas 1 a 8** a los módulos y usa los nombres de tu dominio.

## Pasos

1. **`settings.gradle.kts`**: `dependencyResolutionManagement` con `FAIL_ON_PROJECT_REPOS` y `mavenCentral()`, y `include(":core", ":data", ":app")`. Borra el bloque `repositories` del `build.gradle.kts` raíz.
2. **Catálogo**: añade `serialization` (1.12.0), `serialization-bom`, `serialization-json` y el plugin `kotlin-serialization`.
3. **Raíz**: `alias(libs.plugins.kotlin.serialization) apply false`.
4. **`core`**: tu modelo, validación y informes en Kotlin **puro**, sin depender de nadie.
5. **`data`**: tu archivo JSON versionado (semana 8): `api(project(":core"))`; serialization como `implementation`. Las funciones públicas usan los tipos de `core`; los tipos de serialización no salen del módulo.
6. **`app`**: el programa. `implementation(project(":data"))`, `application { mainClass }`. No declara `core`.
7. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - `:app:run` produce la salida esperada de tu dominio,
   - `core` no depende de ningún módulo (informe de dependencias),
   - `kotlinx-serialization` no está en el `compileClasspath` de `app` y sí en su `runtimeClasspath`,
   - un cambio de cuerpo en `core` no recompila `data` ni `app`,
   - cada versión aparece una vez en el catálogo,
   - un módulo con `repositories { }` hace fallar el build con el mensaje de Gradle,
   - (opcional) una dependencia de `core` hacia `data` es un ciclo.
8. **Pega en tu README** el resultado de `./gradlew :app:dependencies --configuration compileClasspath` y explica qué módulos ve `app` y por qué.
9. **Abre el pull request** `week-10` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, `core` sin dependencias, `api` solo donde un tipo sale en la API, ninguna versión escrita a mano, repositorios centralizados, tests con valores calculados a mano.

## Pistas

- `./gradlew projects` te dice qué ve Gradle como módulos.
- El raíz **aplica** `kotlin-jvm` (para ejecutar los tests) y **no** aplica `kotlin-serialization` (`apply false`); los módulos aplican ambos con `alias`.
- Un módulo que depende de otro mediante `api` hace que sus dependientes vean los tipos. Si ves `Cannot access class`, mira `api` / `implementation`.
- En un test, `project.file("core/build.gradle.kts").appendText("...")` cambia el módulo copiado sin tocar el tuyo.
- Para un cambio de cuerpo, reemplaza una expresión por otra equivalente en un archivo de `core/src/main`.
- Para contar versiones en el catálogo, ignora las líneas de comentario (`#`).
- Los informes de dependencias escriben los módulos como `project ':core'`.
