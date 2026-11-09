# Proyecto — Hilt y las preferencias de la lista

⏱️ 4 h 30 min · Capa 28 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 28, `BitacoraApp` creaba los repositorios a mano y la actividad le pasaba al ViewModel lo que necesitaba con una `viewModelFactory`. Esta semana lo hace Hilt: un módulo dice cómo se crea cada repositorio y cuántos hay, y el ViewModel los pide por el constructor. Y la lista tiene dos filtros, "Primero las recientes" y "Solo altas", que se guardan en DataStore: al abrir la app otra vez siguen puestos.

## Dónde trabajas

En **tu repositorio**, rama `week-29`, a partir de lo que entregaste en la semana 28. El `starter/` es la Bitácora de la semana 28 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya:

- en el catálogo, Hilt 2.60.1, KSP 2.3.13 y `datastore-preferences-core` 1.3.0; en el build, los plugins `ksp` y `hilt` (con `apply false` en el raíz), `ksp(libs.hilt.compiler)` y `kspTest`, guava restringida a 33.7.2-jre en las dos, y `verification-metadata.xml` con los componentes nuevos;
- en `data`, `ListSettings`, `SettingsRepository` y `DataStoreSettingsRepository`, que de momento guarda en memoria;
- `BitacoraApp` crea también el repositorio de los ajustes, y `AppModule.kt` está vacío;
- en `Session.kt`, el campo `settings` del estado, el intent `ChangeSettings` (que no hace nada) y `visible()`, que devuelve la lista entera; `BitacoraScreen` ya pinta `state.visible()`;
- los textos `newest_first` y `only_high`, en los dos idiomas;
- `app/src/test/resources/robolectric.properties` con `qualifiers=h540dp`, y los `@Config` con `"+es"` (teoría 05, apartado 5).

Y tests nuevos:

- `data/src/test/kotlin/dev/bitacora/data/SettingsTest.kt`: el DataStore de los ajustes, en la JVM;
- `app/src/test/kotlin/dev/bitacora/SettingsTest.kt`: `visible()`, el reducer y el ViewModel con las preferencias;
- `app/src/test/kotlin/dev/bitacora/AccessibilityTest.kt`, que ahora cuenta los dos filtros;
- `app/src/testField/kotlin/dev/bitacora/HiltTest.kt`: la actividad con el grafo de test y `FakeModule`;
- `app/src/testField/kotlin/dev/bitacora/PreferencesTest.kt`: los filtros en la pantalla, y que sigan al cerrar y abrir.

Con la Bitácora de la semana 28, fallan 2 de 10 en `:data:test`, 13 de 78 en `testFieldDebugUnitTest` y 9 de 70 en `testDemoDebugUnitTest`. Dos tests de semanas anteriores ya cuentan con los filtros: `AccessibilityTest` espera 11 elementos pulsables (con los dos chips) y `DemoTest.theNextObservationIsTheFourth` cuenta con la altura de su fila. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :data:test :app:testFieldDebugUnitTest`. En `BitacoraApp` y `MainActivity`, cuenta las líneas que crean o entregan dependencias. Apúntalo para el README (teoría 01, apartado 1).
2. **DataStore** (`data/Settings.kt`): `PreferenceDataStoreFactory.create` con el `scope`, `produceFile = { file }` y `ReplaceFileCorruptionHandler { emptyPreferences() }`; una `booleanPreferencesKey` por campo; `settings` con `data.map`, y `save` con un `edit` (teoría 04).
3. **El grafo de la app**: `@HiltAndroidApp` en `BitacoraApp`, que se queda vacía; en `AppModule`, un `@Provides @Singleton` por repositorio, con `@ApplicationContext` para `filesDir` y las mismas rutas que antes (teorías 01 y 02).
4. **El ViewModel y la actividad**: `@HiltViewModel` y `@Inject constructor` en `BitacoraViewModel`; `@AndroidEntryPoint` en `MainActivity`, y `by viewModels()` sin bloque. Borra los imports que sobren: con `-Werror`, un import sin usar no rompe, pero lint lo marca.
5. **Las reglas** (`Session.kt`): `visible()` quita las que no son `HIGH` si `onlyHigh`, y le da la vuelta a la lista si `newestFirst`; `ChangeSettings` cambia `settings` y deja las observaciones (la misma instancia).
6. **Las preferencias en el ViewModel**: en `init`, recoge `preferences.settings` como las observaciones; en `onIntent`, si los ajustes de después son otros, `preferences.save(...)`.
7. **Los filtros** (`BitacoraScreen`): un parámetro `onSettingsChange: (ListSettings) -> Unit = {}`, y encima de la lista una fila con dos `FilterChip` (`selected`, `onClick` con un `copy` del campo, `label` con el texto). En la actividad, `onSettingsChange = { notes.onIntent(BitacoraIntent.ChangeSettings(it)) }`.
8. **Los tests**: `./gradlew :data:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo? Y `./gradlew build`.
9. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - en el reducer, con "Solo altas", añadir una Baja: hay 4 observaciones y se ven 2;
   - en el reducer, con "Solo altas", `Replace` de A-1 a Baja: se ve solo A-3;
   - en el ViewModel, un cambio que llega del repositorio de los ajustes (no de un intent) sale en el estado;
   - en `data`, dos `save` seguidos y abrir otra vez: el segundo;
   - en `data`, un archivo roto, un `save` y abrir otra vez: lo guardado;
   - en `HiltTest`, con el `SettingsRepository` del grafo de test en "Solo altas", la A-7 (Media) no sale.
10. **Explica en tu README** lo que contaste en el paso 1 y dónde está ahora cada cosa; por qué los dos repositorios llevan `@Singleton` y `BitacoraViewModel` no; qué es preferencia y qué es dato en tu dominio, y por qué van en sitios distintos; y qué pasaría con los filtros si el archivo de DataStore se rompiese.
11. **Abre el pull request** `week-29` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, ningún `viewModelFactory` ni contenedor a mano, los repositorios en `AppModule` con `@Singleton`, el ViewModel con `@HiltViewModel`, DataStore en `data` con `corruptionHandler`, los filtros que solo cambian lo que se ve, y tests con valores esperados escritos a mano.

## Pistas

- `Cannot create an instance of class dev.bitacora.BitacoraViewModel`: falta `@AndroidEntryPoint` en la actividad.
- `Hilt Activity must be attached to an @HiltAndroidApp Application`: falta `@HiltAndroidApp`, o el manifiesto no tiene `android:name=".BitacoraApp"`.
- `[Dagger/MissingBinding] ... SettingsRepository`: el `@Provides` no está en `AppModule`, o no devuelve la interfaz.
- `There are multiple DataStores active for the same file` en `theFiltersSurviveClosingTheApp`: falta `@Singleton` en el `@Provides` de los ajustes (teoría 03, apartado 3).
- `everythingClickableHasALabelAndRoomForAFinger` da `expected:<11> but was:<9>` hasta el paso 7: faltan los dos chips.
- `theNextObservationIsTheFourth` falla hasta el paso 7: `robolectric.properties` ya cuenta con la altura de la fila de los filtros, y sin ella caben más filas de las que espera el test.
- Si `undoWithAFilterPutsItBackInTheWholeList` falla, `Delete` o `Undo` trabajan sobre `visible()`: tienen que usar la lista entera.
- `./gradlew :app:testFieldDebugUnitTest --tests '*HiltTest'` para ir test a test.
