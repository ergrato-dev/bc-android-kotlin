# Proyecto — La Bitácora verificada y rápida

⏱️ 4 h · Capa 13 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Cierra la fase 2.

## Qué construyes

El build de la Bitácora, terminado para la fase 2. Verifica cada artefacto que usa, también los plugins de `build-logic`. Reutiliza la configuración entre ejecuciones y construye los módulos en paralelo. Y tienes medido, con números, qué ha ganado. Es el build sobre el que llegará Android en la semana 15.

## Dónde trabajas

En **tu repositorio**, rama `week-14`, a partir de lo que entregaste en la semana 13. El `starter/` es la Bitácora de la semana 13 resuelta. En `src/test/kotlin/Project.kt` hay una función nueva, `tamper` (ya hecha), que estropea en la copia el sha256 de un artefacto, y en `BuildTest.kt`, un test de ejemplo. Copia eso a tu repositorio; tu código de `core`, `data`, `app` y `build-logic` no cambia.

## Pasos

1. **Mide antes**: `./gradlew clean build -x test --no-build-cache --profile` tres veces. Apunta en tu README el tiempo de configuración y el total de las dos últimas.
2. **La verificación**: `./gradlew --write-verification-metadata sha256 help` en la raíz. Comprueba que el archivo tiene `gradle-kotlin-dsl-plugins` (el de `build-logic`) y que `./gradlew build` sigue en verde.
3. **Caché de configuración y paralelo** (`gradle.properties`): `org.gradle.configuration-cache=true` y `org.gradle.parallel=true`. Si algo de tu build no es compatible, Gradle lo dice con un informe; arréglalo, no lo silencies.
4. **Mide después**: repite el paso 1 y añade una medición más, la del día a día: cambia una línea de `core` y ejecuta `./gradlew :app:run`. Apunta las dos en tu README.
5. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - un plugin de `build-logic` alterado (`gradle-kotlin-dsl-plugins-6.7.6.jar`) hace fallar hasta `help`,
   - una dependencia nueva sin apuntar en el archivo hace fallar la compilación del módulo que la declara, nombrando sus artefactos,
   - `:app:run` dos veces: la segunda dice `Configuration cache entry reused.` y sigue imprimiendo `Bitácora <tu dominio> v1.0.0`,
   - `build -x test` entero es compatible con la caché de configuración (`Configuration cache entry stored.`),
   - con `help --info`, la línea de parámetros dice `configurationCache=true` y `parallelProjectExecution=true`,
   - en `:app`, `dependencyInsight` de `kotlin-stdlib` da la 2.4.21 por resolución de conflictos.
6. **Explica en tu README** qué artefactos cubre `verification-metadata.xml` que no son bibliotecas de tu programa, y qué harías si un día un sha256 deja de coincidir.
7. **Abre el pull request** `week-14` → `main` con la salida de `./gradlew test`, tus mediciones de antes y después, y el diff de `gradle/verification-metadata.xml` (o su número de componentes, si es muy largo).

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, verificación con `sha256` y sin `lenient`, caché de configuración y paralelo activados sin silenciar problemas, mediciones de antes y después, y tests del build con valores calculados a mano.

## Pistas

- `./gradlew :app:dependencyInsight ...` lleva el proyecto delante: sin `:app:`, pregunta al proyecto raíz, el banco de pruebas, no a `app`.
- Los nombres de los artefactos, para `tamper`, están en el propio archivo: `grep 'artifact name' gradle/verification-metadata.xml`.
- Para la dependencia nueva vale cualquiera de un BOM que ya usas, por ejemplo `kotlinx-serialization-cbor` en `data`. El test no la añade a tu repositorio: cambia la copia.
- Con la caché de configuración activada, el primer `:app:run` de cada test dice `Configuration cache entry stored.` y el segundo, `reused`.
- Si una medición del paso 4 sale peor que la del paso 1, no es un fallo del ejercicio: apúntalo y explica qué crees que ha pasado. Medir es eso.
