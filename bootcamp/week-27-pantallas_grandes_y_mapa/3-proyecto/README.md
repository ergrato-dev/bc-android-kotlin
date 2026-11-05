# Proyecto — List-detail y mapa

⏱️ 4 h 30 min · Capa 26 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Cierra la fase 4.

## Qué construyes

Hasta la semana 26, la Bitácora era una columna. En una tableta o un plegable abierto, la sesión ocupaba todo el ancho, y abrir una observación la tapaba. Y las coordenadas eran dos números. Esta semana, con 840 dp o más, la sesión queda a la izquierda y lo abierto a la derecha; en un móvil, todo sigue como estaba. Y hay un mapa con las observaciones que tienen ubicación, que se abre como una observación más.

## Dónde trabajas

En **tu repositorio**, rama `week-27`, a partir de lo que entregaste en la semana 26. El `starter/` es la Bitácora de la semana 26 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya en el catálogo `adaptive` 1.3.0, `maps-compose` 9.0.1, `play-services-maps` 20.1.0 y `fragment` 1.9.1; el build todavía no los usa.

Y tests nuevos o cambiados:

- `core/src/test/kotlin/dev/bitacora/core/AreaTest.kt`: el área de las que tienen posición;
- `app/src/testDemo/kotlin/dev/bitacora/AdaptiveTest.kt`: a 900 dp, la sesión con el hueco, el detalle al lado, abrir otra, atrás, un enlace y el mapa; a 700 dp, un panel;
- `app/src/test/kotlin/dev/bitacora/MapTest.kt`: la clave en el manifiesto y el `MapView`;
- `AccessibilityTest`, `LanguagesTest` y el `BuildTest` del raíz: el botón "Mapa" y los permisos de red.

Con la Bitácora de la semana 26, fallan 3 de 9 en `:core:test`, 5 de 52 en `testFieldDebugUnitTest`, 9 de 52 en `testDemoDebugUnitTest` y `onlyTheLocationAndMapPermissionsShip` en `:test`. Copia esos tests a tu repositorio.

## Pasos

1. **El build** (`app/build.gradle.kts`): `adaptive-navigation3`, `maps-compose`, `play-services-maps` y `fragment` (teoría 04, apartado 2).
2. **La clave y la verificación**: `manifestPlaceholders["mapsApiKey"] = providers.gradleProperty("mapsApiKey").getOrElse("")` y la `meta-data` `com.google.android.geo.API_KEY` en el manifiesto. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest` y `git diff --stat gradle/verification-metadata.xml`. ¿Cuántos componentes entran, y de quién son? Pásalos por [OSV](https://osv.dev) antes de seguir.
3. **El área** (`core`): `List<Observation>.area()`, la esquina suroeste y la noreste (el mínimo y el máximo de cada coordenada) de las que tienen posición, o `null`. Sin Play Services: `core` no depende de Android.
4. **Los destinos** (`Navigation.kt`, `MainActivity`): `MapKey`; y abrir un detalle cambia el que hay: `backStack.removeAll { it != SessionKey }` antes de añadirlo (teoría 02, apartado 3).
5. **El list-detail**: `sceneStrategies = listOf(rememberListDetailSceneStrategy(backNavigationBehavior = BackNavigationBehavior.PopUntilContentChange))`; la sesión con `ListDetailSceneStrategy.listPane(detailPlaceholder = { ... })`, con "Elige una observación" centrado; las observaciones y el mapa con `detailPane()`. `@OptIn(ExperimentalMaterial3AdaptiveApi::class)` en `onCreate` (teoría 02).
6. **El mapa**: el botón "Mapa" a la derecha de "Compartir" (`onMap`), y `MapScreen.kt` con el encabezado, "N de M en el mapa", el `GoogleMap` con un marcador por observación con posición (el código de título, la línea de debajo, y el globo abre la observación) y la cámara sobre `area()` en `onMapLoaded` (teoría 04, apartados 4 y 5). Los textos nuevos, en los dos idiomas.
7. **Los tests**: `./gradlew :core:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew build`, sin la propiedad `mapsApiKey`.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - en `core`, el área de dos observaciones a los dos lados del ecuador (una con latitud positiva y otra negativa);
   - a 900 dp, en inglés (`@Config(qualifiers = "en-w900dp-h600dp")`), el hueco dice `"Choose an observation"`;
   - a 900 dp, elegir un nivel en el detalle lo guarda, cierra el detalle y deja el hueco;
   - a 900 dp, el mapa de la demo dice `"1 de 3 en el mapa"`, y tras borrar A-1 con la acción de TalkBack, `"0 de 2 en el mapa"`;
   - de 320 a 900 dp (`RuntimeEnvironment.setQualifiers` y `recreate()`) con A-2 abierta, la sesión y A-2 a la vez (teoría 03, apartado 4);
   - con un móvil (320 dp), atrás desde el mapa vuelve a la sesión.
9. **Explica en tu README** qué hace atrás con dos paneles y el comportamiento por defecto, y por qué; qué entró en `verification-metadata.xml` en el paso 2 y qué viste en OSV; y qué del mapa no prueba ningún test (teoría 05, apartado 4). Si tienes un móvil con Play Services y una clave restringida, apunta lo que comprobaste en él.
10. **Abre el pull request** `week-27` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, el list-detail con el hueco y atrás que no cierra la app, abrir otra que cambia el detalle, el mapa con sus marcadores y su cámara, `area()` en `core`, la clave fuera del repositorio, la verificación regenerada y revisada, y tests con valores esperados escritos a mano.

## Pistas

- `mediumShowsOnePane` pasa desde el principio: a 700 dp, la Bitácora de la semana 26 ya es un panel. Es una guarda para que no pongas dos paneles en medium.
- Si atrás cierra la app con dos paneles, mira el `backNavigationBehavior` (teoría 02, apartado 4).
- Si `lintFieldDebug` para con `InvalidFragmentVersionForActivityResult`, falta `fragment` (teoría 04, apartado 2).
- `MapTest` busca el `MapView` por el nombre de su clase: así compila antes del paso 1.
- `RuntimeEnvironment.setQualifiers("w900dp-h600dp")` cambia todos los qualifiers, también el idioma: en un test con `@Config(qualifiers = "es")`, `"es-w900dp-h600dp"`.
- `./gradlew :app:testDemoDebugUnitTest --tests '*AdaptiveTest'` para ir test a test.
