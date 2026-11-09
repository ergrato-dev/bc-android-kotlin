# Ejercicio 02 — Las preferencias que se quedan

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Las preferencias de las salidas (las unidades y si hay recordatorios) se guardan en memoria y se pierden al cerrar. Al terminar, viven en un DataStore de preferencias: se leen como un `Flow`, cada cambio toca su clave, un archivo roto no tumba nada, y lo que guardaba la versión anterior en un `.properties` pasa a DataStore una vez. Todo en la JVM: `datastore-preferences-core` no necesita Android.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con `datastore-preferences-core` 1.3.0 y `kotlinx-coroutines` 1.11.0. Trae hecho `TripSettings.kt` (`Units` y `TripSettings`). `SettingsStore` recibe el archivo, el `CoroutineScope` donde trabajará DataStore y, si lo hay, el archivo viejo; de momento guarda en un `MutableStateFlow`, con comentarios `Paso N`.

Los tests (`src/test/kotlin/dev/bitacora/SettingsStoreTest.kt`, no los toques) usan un `@TempDir` y el `backgroundScope` de `runTest`; `session` imita cerrar la app cancelando el scope. Al empezar, fallan 5 de 7.

## Pasos

1. **DataStore**: `PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })`; una clave por campo (`stringPreferencesKey("units")` con el nombre del `enum`, `booleanPreferencesKey("reminders")`); `settings` es `data.map { ... }`, con los valores por defecto de `TripSettings` si falta la clave; cada `set` es un `edit` que cambia solo su clave (teoría 04, apartado 2).
2. **El archivo roto**: `corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }` (apartado 4).
3. **La migración**: una `DataMigration<Preferences>` con el archivo viejo. `shouldMigrate`, si existe; `migrate` lo lee con `java.util.Properties` (`units=imperial`, `reminders=true`) y lo pone en las claves; `cleanUp` lo borra. Va en `migrations = listOfNotNull(...)`: sin archivo viejo, ninguna (apartado 5).
4. Ejecuta `./gradlew test` (7 de 7) y `./gradlew build`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Quita el `corruptionHandler`. ¿Qué excepción da `aBrokenFileGivesTheDefaults`? En una app, ¿dónde se vería? Restáuralo.
   - (b) Cambia el nombre del archivo en el test a `trip.prefs`. ¿Qué pasa, y por qué crees que DataStore lo comprueba? Restáuralo.
   - (c) Deja `cleanUp` vacío y ejecuta `theOldFileIsMigrated`. ¿Qué falla? Ahora piensa en la app: la persona cambia a `METRIC` y vuelve a abrir. ¿Qué ve? Restáuralo.
   - (d) `twoStoresOnOneFileFail`: ¿qué pasaría en la Bitácora si `AppModule` crease el repositorio de los ajustes sin `@Singleton`? ¿Por qué Dagger no lo detecta?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `whatIsSavedIsInTheFile` falla con `multiple DataStores active` | El test cierra el primero antes de abrir el segundo; si falla igual, `SettingsStore` no usa el `scope` que recibe |
| `oneChangeKeepsTheOther` pierde los recordatorios | En el `edit`, cambia solo una clave; no crees unas preferencias nuevas |
| `theOldFileIsMigrated` da `METRIC` | `Units.valueOf` espera `IMPERIAL`: pasa lo del archivo a mayúsculas |
| `withoutAFileTheDefaults` falla | Una clave que falta es `null`: el valor por defecto con `?:` |

## Entrega

Los 7 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.
