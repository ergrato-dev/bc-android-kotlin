# Ejercicio 02 — Caché remota con push desde el CI

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Configurar una caché remota HTTP en la que solo el CI sube resultados, con las credenciales fuera del repositorio, y comprobar que otra máquina reutiliza lo que construyó el CI.

## Cómo está montado

`starter/sandbox/` es el ejercicio 01 resuelto. Tú escribes la caché remota en `sandbox/settings.gradle.kts`. Los tests (`src/test/kotlin/RemoteTest.kt`, no los toques) arrancan un servidor de caché en `127.0.0.1` con la clase `CacheServer` (ya hecha), que pide el usuario `bitacora` y la contraseña `s3cret` y cuenta lo que sube, lo que baja y lo que rechaza. Cada `Sandbox` es una máquina, con su propia carpeta, su propia caché local y sus propias variables de entorno (`env`); las de tu terminal (`CI`, `CACHE_USER`, `CACHE_PASSWORD`) no llegan a los tests. Uno pasa desde el principio: sin URL, nadie habla con el servidor.

## Pasos

1. **La URL** (`sandbox/settings.gradle.kts`): `providers.gradleProperty("bitacora.cacheUrl").orNull`. Sin URL, no declares caché remota.
2. **La caché remota**: `buildCache { remote<HttpBuildCache> { ... } }` con esa `url`, y `isPush` solo si existe la variable de entorno `CI`.
3. **Las credenciales**: `credentials { }` con `CACHE_USER` y `CACHE_PASSWORD`, leídas con `providers.environmentVariable(...)`.
4. **Ejecuta** `./gradlew test` (7 de 7).
5. **`NOTAS.md`** (en `starter/`):
   - (a) Los tests usan `http://127.0.0.1:<puerto>/cache/`. Cambia en `CacheServer.kt` la `url` para que use `localhost` y ejecuta los tests. Copia el error. ¿Por qué Gradle acepta una y no la otra? Restáuralo.
   - (b) Cambia `isPush` a `true` siempre. ¿Qué test falla? Describe qué podría acabar en la caché del equipo si cualquiera sube. Restáuralo.
   - (c) En `aWrongPasswordDisablesTheRemoteButNotTheBuild`, el build termina bien con la contraseña mala. ¿Es una buena decisión de Gradle? ¿Cómo te enterarías en el CI de que la caché no funciona?
   - (d) ¿Dónde pondrías, en tu máquina, la URL de la caché del equipo para no escribir `-Pbitacora.cacheUrl` cada vez? ¿Y la contraseña?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `theCiPushesItsResults` falla con 0 | `isPush` no es `true` cuando existe `CI`, o la URL no llega: revisa el nombre `bitacora.cacheUrl` |
| `aDeveloperOnlyPulls` falla con 1 en `stores` | `isPush` es `true` siempre |
| `Using insecure protocols with remote build cache, without explicit opt-in, is unsupported.` | La URL no es `127.0.0.1`: Gradle solo acepta `http` hacia esa dirección |
| `rejected` vale 1 en un test que no lo espera | Las credenciales no salen de `CACHE_USER` y `CACHE_PASSWORD` |
| `withoutAUrlTheServerIsNotUsed` falla | Declaras la caché remota aunque no haya URL: ponla dentro de un `if` |
| `thePasswordIsNotInTheRepository` falla | La contraseña está escrita en el archivo, o no la lees con `environmentVariable("CACHE_PASSWORD")` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.
