# Proyecto — La Bitácora con caché de build

⏱️ 4 h · Capa 12 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora deja de repetir trabajo. Con la caché de build activada, un `clean` no obliga a recompilar, una segunda copia del repositorio aprovecha lo que compiló la primera y, con la caché remota, tu máquina descarga lo que construyó el CI. Además arreglas un fallo que ya tenías: los tests del raíz no se volvían a ejecutar cuando cambiaba el código de los módulos.

## Dónde trabajas

En **tu repositorio**, rama `week-13`, a partir de lo que entregaste en la semana 12. El `starter/` es la Bitácora de la semana 12 resuelta, con lo nuevo en `src/test/kotlin`: `Project.kt` (ya hecho) da a cada test su propia caché local, con `relocated()` y `otherMachine()` como en los ejercicios, y `CacheServer.kt` (ya hecho) es el servidor del ejercicio 02. Copia esos archivos a tu repositorio; tu código de `core`, `data`, `app` y `build-logic` no cambia.

## Pasos

1. **Activa la caché** (`gradle.properties`): `org.gradle.caching=true`, junto a `version`.
2. **La caché remota** (`settings.gradle.kts`): la del ejercicio 02, con la URL en `bitacora.cacheUrl`, `isPush` solo con `CI` y las credenciales de `CACHE_USER` y `CACHE_PASSWORD`.
3. **Las entradas de `:test`** (`build.gradle.kts` raíz): declara los archivos del proyecto como entradas de `tasks.test` con `inputs.files(fileTree(...))`, rutas relativas y fuera `build/`, `.gradle/`, `.kotlin/`, `.git/` y `.idea/` (teoría 03). Compruébalo: cambia un archivo de `core/` y `./gradlew test` tiene que ejecutar `:test`.
4. **Revisa tus tareas propias**: `generateBuildInfo` ya es `@CacheableTask` y no tiene archivos de entrada. Si en las semanas 9 y 10 añadiste otras tareas, pasa por la lista de comprobación de la teoría 05.
5. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - una copia en otra carpeta (`relocated()`) saca `FROM-CACHE` los tres `compileKotlin` y `generateBuildInfo`,
   - cambiar la versión en `gradle.properties` ejecuta solo `generateBuildInfo` (`SUCCESS`) y deja los `compileKotlin` en `UP_TO_DATE`; volver a la versión anterior lo saca `FROM-CACHE`,
   - después de `clean`, `:app:run` imprime `Bitácora <tu dominio> v1.0.0` con `generateBuildInfo` en `FROM_CACHE`,
   - el CI sube resultados y otra máquina (`otherMachine()`, sin `CI`) los descarga: `FROM_CACHE`, sin subir nada,
   - con el servidor caído, el build termina bien y avisa (`The remote build cache was disabled during the build due to errors.`),
   - con la contraseña mala, el servidor rechaza la petición y no se sube nada.
6. **Explica en tu README** qué hacía mal `:test` antes del paso 3 y por qué la caché lo empeoraba.
7. **Abre el pull request** `week-13` → `main` con la salida de `./gradlew test` y la de `./gradlew clean build` ejecutada dos veces seguidas (la segunda, con los `FROM-CACHE`).

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, la caché activada en el repositorio, la remota sin secretos y con push solo desde el CI, `:test` con sus entradas declaradas y tests del build con valores calculados a mano.

## Pistas

- `assertEquals(TaskOutcome.FROM_CACHE, r.outcome(it), it)`: el tercer argumento es el mensaje, así sabes qué tarea falló cuando compruebas varias en un bucle.
- `CacheServer().use { it.url }` te da la URL de un servidor que ya está cerrado: es el servidor caído.
- `server.stores.get()` antes y después del build de la otra máquina tiene que dar lo mismo.
- Cada test arranca uno o varios builds de la Bitácora: la suite tarda un par de minutos. Mientras escribes uno, ejecútalo solo con `./gradlew test --tests "BuildTest.<nombre>"`.
- Si un test que espera `SUCCESS` sale `FROM_CACHE`, revisa que no reutilice la caché de otro: `Project()` crea una nueva; `relocated()` la comparte.
