# Proyecto — La Bitácora en módulos

⏱️ 5 h · Capa 32 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora termina la fase 5 con la misma app y otro build. `:core` y `:data` se parten en cuatro módulos de Kotlin puro: `:core:model` (el dominio), `:core:database` (Room), `:core:network` (Ktor) y `:core:data` (el repositorio, que junta los dos). El mapa sale de la app a `:feature:map`, una biblioteca de Android con sus textos, sus dependencias de Google Maps y sus tests. La app se queda con lo que junta las piezas: la actividad, la navegación, Hilt y WorkManager.

```
:app ──► :feature:map ──► :core:model
  │
  └────► :core:data ──► :core:database ──► :core:model
                   └──► :core:network
```

Nada cambia para quien usa la app: el APK es el mismo, y los tests de la app siguen en verde sin tocar su lógica (solo los `import`).

## Dónde trabajas

En **tu repositorio**, rama `week-33`, a partir de lo que entregaste en la semana 32. El `starter/` es la Bitácora de la semana 32 resuelta. Trae ya:

- en `gradle/verification-metadata.xml`, los componentes que entran al partir los módulos (teoría 02, apartado 5; teoría 03, apartado 8). El catálogo no cambia: no hay bibliotecas nuevas.

Y tests nuevos:

- `src/test/kotlin/ModulesTest.kt`, en la raíz, con TestKit: qué módulos hay, qué ve la app al compilar, dónde viven los textos del mapa y que el mapa tiene sus propios tests (teoría 04, apartado 5).

Con la Bitácora de la semana 32 y estos cambios, fallan 5 de los 6 de `ModulesTest`; los 13 de `BuildTest` y los de los módulos siguen en verde. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'ModulesTest'`. ¿Por qué pasa ya `theCoreModulesShowOnlyWhatTheirSignaturesNeed`? Mira el `build.gradle.kts` de `data`. ¿Qué haría falta para que fallase?
2. **`:core:model`**: mueve `core/` a `core/model/` y el paquete `dev.bitacora.core` a `dev.bitacora.model`, en todo el proyecto (también en `app/compose-stability.conf`). En `settings.gradle.kts`, `include(":core:model", ...)`. Compila antes de seguir.
3. **`:core:database`**: las entidades, el DAO y `BitacoraDatabase`, en `dev.bitacora.database`, con KSP y el plugin de Room. Mueve también `data/schemas/` y renombra su carpeta al nombre nuevo de la clase. `MigrationTest` va aquí: su segundo test lee con el DAO, no con el repositorio (teoría 02, apartados 1 y 6).
4. **`:core:network`**: `Network.kt` sin `RefreshResult`, en `dev.bitacora.network`. `ObservationsResponse`, `internal`. Ktor core en `api`; el JSON en `implementation` (teoría 02, apartados 2 y 3).
5. **`:core:data`**: el repositorio, `RefreshResult`, el builder `bitacora()`, las traducciones entre la entidad, el servidor y el dominio, las preferencias y el JSON, con sus tests. ¿Qué es `api` y qué `implementation`? Decide mirando las firmas públicas. Compila: ¿qué error da `toObservation()`, y por qué no lo daba antes? (Teoría 02, apartado 4.) Cambia `app` a `implementation(project(":core:data"))` y borra `data/`.
6. **Los convention plugins**: en `build-logic`, `bitacora.android-base` con lo común de la app y las bibliotecas (sobre `CommonExtension`), `bitacora.android-application` que lo usa, y `bitacora.android-library` con `testOptions.targetSdk` (teoría 03, apartado 7). Comprueba que la app sigue igual: `./gradlew :app:testFieldDebugUnitTest`.
7. **`:feature:map`**: biblioteca con `bitacora.android-library` y el plugin de Compose, namespace `dev.bitacora.map`. Mueve `MapScreen` (con `snippet: (Observation) -> String` en vez de `line()`, teoría 04, apartado 1), los textos `map` y `on_map` en los dos idiomas, y Maps de la app a la biblioteca. En la app, `MapScreen` con su `import` y el botón «Mapa» con `MapR` (teoría 03, apartado 3).
8. **Los tests del mapa**: `feature/map/src/test/kotlin/dev/bitacora/map/MapScreenTest.kt`, con `createComposeRule` y `ui-test-manifest` en `debugImplementation` de la biblioteca.
9. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo que no traiga el starter? Si entra, ¿de qué módulo? Después, `./gradlew build`.
10. **Escribe al menos 5 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
    - en `:feature:map`, cuántas hay en el mapa en español, y en inglés;
    - en `:feature:map`, sin ninguna observación con lugar: «0 de N»;
    - en `:core:network`, que hasta ahora no tiene tests: `toNetworkError()` de una `IOException` es `OFFLINE`, y de una excepción que no es de la red, `null` (sus dependencias de test, en su `build.gradle.kts`);
    - en la raíz, `:feature:map` no ve `:core:data` al compilar;
    - en la raíz, un cambio en el cuerpo de `MapScreen` no recompila la app (`:app:compileFieldDebugKotlin` sale `UP-TO-DATE`; teoría 05, apartado 2).
11. **Explica en tu README** por qué `bitacora()` está en `:core:data` y no en `:core:database`; qué tiene `api` cada módulo y por qué; por qué el mapa recibe `snippet` en vez de llamar a `line()`; qué tareas se ejecutan al cambiar el cuerpo de `MapScreen` (pega la salida); y qué módulo partirías después y por qué.
12. **Abre el pull request** `week-33` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): los seis módulos del plan, cada uno con su paquete; ningún módulo `core` con Android; `api` solo para lo que sale en las firmas; `ObservationsResponse` `internal`; `:feature:map` como biblioteca con su namespace, sus textos, sus dependencias de Maps y sus tests; los convention plugins compartiendo lo común; la app y sus tests en verde sin cambios de lógica; el APK con lo mismo; sin warnings de Kotlin, `lintFieldDebug` en verde, y tests con valores esperados escritos a mano.

## Pistas

- `Smart cast to 'Double' is impossible, because 'latitude' is a public API property declared in different module.`: copia la propiedad a un `val` local (teoría 02, apartado 4).
- `Schema '1.json' required for migration was not found at the schema out folder: core/database/schemas/dev.bitacora.database.BitacoraDatabase. Cannot generate auto migrations.`: la carpeta de `schemas/` sigue con el nombre viejo de la clase (paso 3).
- `Dependency verification failed for configuration ':core:network:testRuntimeClasspath'`: el módulo resuelve una versión que antes no se usaba. Si el starter no la trae, añádela con `--write-verification-metadata` (paso 9).
- `Unresolved reference 'map'` en `BitacoraScreen.kt`: el texto es del mapa; usa su `R` (paso 7).
- `Unresolved reference 'MapScreen'` en `MainActivity.kt`: falta `import dev.bitacora.map.MapScreen`. Con la pantalla en otro paquete, ya no basta con estar en `dev.bitacora`.
- `Namespace 'dev.bitacora' is used in multiple modules and/or libraries`: la biblioteca necesita su propio namespace.
- Los tests del mapa fallan con `Path is not a file: .../android-all-instrumented-17-...jar`: falta `testOptions.targetSdk = 36` en la biblioteca (paso 6).
- `None of the following candidates is applicable` en `testOptions { }` de `bitacora.android-base`: en `CommonExtension`, `testOptions` es una propiedad: `testOptions.unitTests...`.
- `theMapLibrariesStayInsideTheFeature` ve `maps-compose` en la app: siguen las dependencias de Maps en `app/build.gradle.kts`.
- `:app:lintAnalyzeFieldDebugUnitTest` falla con `Unexpected failure during lint analysis`: repítelo solo; en la medición pasó a la segunda (teoría 05, apartado 4).
- `./gradlew :feature:map:testDebugUnitTest --tests '*MapScreenTest'` y `./gradlew :test --tests 'ModulesTest'` para ir test a test.
