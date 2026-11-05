# Rúbrica de Evaluación — Semana 27: Pantallas grandes y el mapa

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: list-detail y mapa |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Cuáles son los tramos de ancho, y dónde cambian? ¿Por qué no se decide por la orientación ni por "si es una tableta"? | 10 |
| 2 | ¿Qué devuelve `currentWindowAdaptiveInfoV2().windowSizeClass` en Robolectric sin qualifiers, y con `w900dp-h600dp`? ¿Por qué no dice 900? | 10 |
| 3 | ¿Qué es una escena en Navigation 3? ¿Cambia el back stack de la Bitácora entre un móvil y una tableta? ¿Cuántos paneles hay a 700 dp? | 10 |
| 4 | Con dos paneles y el comportamiento por defecto, ¿qué hacía atrás desde una observación, y por qué? ¿Qué lo arregla? | 10 |
| 5 | Con dos paneles y A-2 abierta, tocas A-3 y pulsas atrás. ¿Qué ves con `backStack += key`, y qué con `removeAll` antes? | 10 |
| 6 | ¿Qué es la postura de mesa, qué va arriba y qué abajo, y por qué gana al ancho? ¿En qué unidades vienen los `bounds` de la bisagra? | 10 |
| 7 | ¿Cómo se pone una bisagra de mentira en un test, y por qué la regla va por fuera de la de Compose? ¿Qué pasa si la pantalla se pinta en `onCreate`? Y al desplegar, ¿qué se pierde con `remember`? | 10 |
| 8 | ¿Qué dependencias lleva el mapa de la Bitácora y por qué cada una? ¿Qué permisos entran en el manifiesto, y quién los pone? | 10 |
| 9 | ¿Dónde va la clave de Maps, cómo llega al manifiesto, y qué la protege si va dentro del APK? | 10 |
| 10 | ¿Qué hace `GoogleMap` en Robolectric? ¿Qué partes del mapa se prueban en `core`, cuáles en Robolectric y cuáles solo en un móvil? ¿Por qué la cámara se mueve en `onMapLoaded`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Compact, menos de 600 dp; medium, de 600 a 840; expanded, de 840 a 1200; large y extra-large, 1200 y 1600 o más (`WindowSizeClass.WIDTH_DP_*_LOWER_BOUND`). La orientación no dice cuánto sitio hay (un móvil en horizontal mide unos 800 dp, medium), y una tableta puede tener la app en media pantalla, a 600: lo que cuenta es la ventana de la app.
2. `WindowSizeClass(minWidthDp=0, minHeightDp=0)` (320 x 470 dp) y `WindowSizeClass(minWidthDp=840, minHeightDp=480)`. La clase es el tramo, "al menos 840", no el tamaño.
3. Lo que `NavDisplay` pinta de una vez: una o varias entradas del back stack. El back stack no cambia (`[SessionKey, ObservationKey("A-2")]` en los dos); cambia la escena que hace `ListDetailSceneStrategy`. A 700 dp, uno, el último: dos solo desde expanded.
4. Cerraba la app: `PopUntilScaffoldValueChange` quita entradas hasta que cambie la forma de los paneles, y la sesión con A-2 y la sesión con el hueco son la misma forma, así que no había nada que quitar (`hasEnabledCallbacks()` era `false`). `BackNavigationBehavior.PopUntilContentChange`.
5. Con `+=`, el back stack es `[Sesión, A-2, A-3]` y atrás vuelve a A-2. Con `removeAll { it != SessionKey }` antes, es `[Sesión, A-3]` y atrás deja la sesión con el hueco. En el móvil no cambia nada.
6. Medio cerrado (`HALF_OPENED`) con la bisagra en horizontal: la mitad de abajo está sobre la mesa. Arriba, lo que se lee (el detalle); abajo, lo que se toca (la lista). Gana porque un plegable sobre la mesa puede ser expanded, y lo que quiere es arriba y abajo. Los `bounds` vienen en píxeles, de la ventana.
7. `WindowLayoutInfoPublisherRule` y `overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(FoldingFeature(activity, state = HALF_OPENED, orientation = HORIZONTAL))))`. Va por fuera (`RuleChain.outerRule(publisher).around(compose)`) para que esté puesta antes de que arranque la actividad. Con la pantalla pintada en `onCreate` de `MainActivity`, la bisagra no llegaba (medido, sin explicar): se pinta con `setContent` desde el test. Con `remember`, la elegida: tras recrear a 900 dp, la lista está, pero el detalle es el hueco.
8. `adaptive-navigation3` (el list-detail), `maps-compose` (el mapa en Compose), `play-services-maps` (ya viene con `maps-compose` en la 20.0.0; la directa la sube a la 20.1.0) y `fragment` 1.9.1 (`play-services-maps` declara la 1.0.0, y lint para con `InvalidFragmentVersionForActivityResult` por el `registerForActivityResult` de la actividad). Entran `INTERNET` y `ACCESS_NETWORK_STATE`, del manifiesto de `play-services-maps`.
9. En `~/.gradle/gradle.properties` o en `ORG_GRADLE_PROJECT_mapsApiKey`, nunca en el repositorio. Llega por `manifestPlaceholders["mapsApiKey"]` al `${mapsApiKey}` de la `meta-data` `com.google.android.geo.API_KEY` (con `-PmapsApiKey=prueba`, `aapt2 dump xmltree` enseña `android:value="prueba"`). La protege la restricción en Google Cloud: solo Maps SDK for Android, solo tu `applicationId` y tu SHA-1.
10. Crea un `MapView` con un aviso de que faltan los Servicios de Google Play; el contenido no se compone y `onMapLoaded` no llega. En `core`, `area()`; en Robolectric, cuántas salen, `LatLng`/`LatLngBounds`, la `meta-data`, el `MapView` y los permisos; en un móvil, los marcadores, la cámara, el globo y el mapa oscuro. En `onMapLoaded` porque `CameraUpdateFactory` sin mapa lanza `NullPointerException: CameraUpdateFactory is not initialized`.

**Ejercicio 01, paso 7**: (a) `tabletopWinsOverTheWidth`: `expected:<TABLETOP> but was:<TWO_PANES>`. `tabletopPutsTheDetailAboveTheHinge` sigue en verde porque 600 dp es medium, no expanded: la comprobación del ancho no gana. (b) `unfoldingKeepsTheSelection`: la lista está, pero a la derecha el hueco; `remember` no sobrevive a la recreación, `rememberSaveable` va al `Bundle`. (c) `backReturnsToTheList`: sin el `BackHandler`, atrás llega a la actividad, que se cierra, y la lista no vuelve. (d) Porque con la pantalla pintada en `onCreate` la bisagra de mentira no llega (teoría 03, apartado 3), y `UnfoldTest` necesita `MainActivity` para que la recreación vuelva a pintar la pantalla.

**Ejercicio 02, paso 6**: (a) `theKeyIsInTheManifest`; según la documentación de Maps, sin la entrada el mapa lanza una excepción al iniciarse en el móvil. (b) En la `meta-data` del manifiesto del APK, `android:value="prueba"`: la clave no es secreta; la protege la restricción por paquete y SHA-1 en Google Cloud. (c) Los pone `play-services-maps`, que ya está en el build, y entran solos al fusionar el manifiesto; el test avisa si una dependencia mete un permiso nuevo. (d) Los marcadores, la cámara sobre `bounds()` y el globo: en Robolectric no se componen. Se comprueban en un móvil con Play Services y una clave.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — La sesión que se adapta (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| `layoutFor` con la mesa primero; `currentWindowAdaptiveInfoV2`; la bisagra pasada a dp; `BackHandler` solo en un panel; `rememberSaveable`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado, con el mensaje del apartado (a) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — El mapa de la salida (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| La clave por `manifestPlaceholders`, sin valor en el proyecto; `bounds()` sin el builder vacío; los marcadores con `key`; la cámara en `onMapLoaded` (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con la salida de `aapt2` del apartado (b) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — List-detail y mapa

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada y sin `mapsApiKey` | 10 |
| Tests | Los de la semana 26 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: el ecuador, el hueco en inglés, elegir un nivel, el mapa tras borrar, desplegar, atrás desde el mapa | 25 |
| Pantallas grandes | Dos paneles desde expanded y uno en medium; el hueco; abrir otra cambia el detalle; atrás no cierra la app; los enlaces de la semana 24 siguen funcionando | 25 |
| Mapa | `area()` en `core`; un marcador por observación con posición, con su clave; la cámara en `onMapLoaded`; "N de M en el mapa"; la clave fuera del repositorio; la verificación regenerada y revisada en OSV | 15 |
| Adaptación al dominio | Los textos de tu dominio, en los dos idiomas; README con lo que pide el paso 9 | 15 |
| Entrega | Rama `week-27`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una clave de API en el repositorio, aunque sea en un commit anterior | -20 |
| `core` o `data` dependen de Android o de Play Services (por ejemplo, `LatLng` en `area()`) | -10 |
| Dos paneles decididos por la orientación o por el modelo, y no por la clase de tamaño | -15 |
| Un test de la semana 26 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-27` → `main` en el repositorio del aprendiz.
