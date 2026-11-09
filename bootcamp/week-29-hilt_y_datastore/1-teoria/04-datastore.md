# DataStore

> Las observaciones son datos de la persona y tienen su repositorio. Cómo quiere ver la lista (las recientes primero, solo las altas) no es un dato: es una preferencia. Durante años eso iba en `SharedPreferences`; hoy va en DataStore, que guarda en un archivo, se lee como un `Flow` y escribe con corrutinas, sin bloquear el hilo principal.

## 🎯 Objetivos

- Saber qué resuelve DataStore frente a `SharedPreferences`, y cuándo usar Preferences o Proto
- Crear un DataStore de preferencias con `PreferenceDataStoreFactory`, leerlo y escribirlo
- Respetar la regla de un DataStore por archivo
- Recuperarse de un archivo roto y migrar datos de una versión anterior

## 1. Por qué no `SharedPreferences`

| | `SharedPreferences` | DataStore |
| --- | --- | --- |
| Leer | Síncrono; la primera lectura bloquea mientras carga el XML | `Flow`, en segundo plano |
| Escribir | `apply()` vuelve en seguida, pero escribe al pausar la actividad, en el hilo principal | `suspend`, con transacción |
| Errores | Un archivo roto se ignora sin avisar | Lanza `CorruptionException`, o lo que diga tu *handler* |
| Cambios | Un *listener* | El mismo `Flow` emite el valor nuevo |

DataStore viene en dos sabores: **Preferences**, claves con tipo y sin esquema (lo de la Bitácora), y **Proto**, un objeto con esquema de protobuf o el *serializer* que escribas tú. Para dos booleanos, Preferences. Para algo con estructura que quieres validar, Proto. Para datos con relaciones, consultas o muchas filas, una base de datos (semana 30).

## 2. Crear, leer y escribir

`datastore-preferences-core` es Kotlin puro: va en `data`, que no depende de Android, y se prueba en la JVM.

```kotlin
private val NEWEST_FIRST = booleanPreferencesKey("newest_first")
private val ONLY_HIGH = booleanPreferencesKey("only_high")

class DataStoreSettingsRepository(file: File, scope: CoroutineScope) : SettingsRepository {
    private val store = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
        produceFile = { file },
    )

    override val settings: Flow<ListSettings> = store.data.map {
        ListSettings(newestFirst = it[NEWEST_FIRST] ?: false, onlyHigh = it[ONLY_HIGH] ?: false)
    }

    override suspend fun save(settings: ListSettings) {
        store.edit {
            it[NEWEST_FIRST] = settings.newestFirst
            it[ONLY_HIGH] = settings.onlyHigh
        }
    }
}
```

- La **clave** lleva el tipo: `booleanPreferencesKey`, `stringPreferencesKey`, `intPreferencesKey`… Una clave que no está da `null`: el valor por defecto lo pones tú con `?:`.
- `data` lee el archivo la primera vez que alguien lo recoge, y emite otra vez con cada `edit`.
- `edit` es una transacción: lee lo último, aplica tu bloque y escribe el archivo entero; si dos `edit` llegan a la vez, van uno detrás de otro.
- El `scope` es donde DataStore hace su trabajo. Cuando ese scope termina, el DataStore se cierra y el archivo queda libre.

En una app Android sin capas, lo normal es el delegado `val Context.dataStore by preferencesDataStore("settings")`, que crea el archivo en `files/datastore/settings.preferences_pb`. La Bitácora usa la factory con la misma ruta, porque el repositorio está en `data` y no conoce `Context`.

> 🔬 Medido: con un nombre que no termina en `.preferences_pb`, `PreferenceDataStoreFactory.create` lanza `IllegalStateException` al leer.

## 3. Uno por archivo

DataStore guarda en memoria lo último que leyó y confía en ser el único que escribe. Por eso no deja abrir dos sobre el mismo archivo en el mismo proceso:

> 🔬 Medido en `twoStoresOnOneFileFail` (proyecto y ejercicio 02): el segundo, al leer, lanza `IllegalStateException: There are multiple DataStores active for the same file`.

Quien crea el DataStore tiene que crearlo una vez: en la Bitácora, el `@Provides @Singleton` de `AppModule` (teoría 03). En los tests, cada test usa un archivo de `@TempDir` y el `backgroundScope` de `runTest`, que se cancela al terminar.

## 4. Un archivo roto

> 🔬 Medido: sin `corruptionHandler`, un archivo con basura hace que `data` lance `CorruptionException`, y la pantalla que lo recoge se cae.

`ReplaceFileCorruptionHandler { emptyPreferences() }` lo cambia por unas preferencias vacías: la persona pierde sus filtros y la app sigue. Para preferencias es razonable; para datos de la persona, no (por eso las observaciones, en la semana 28, se apartaban a `.bad`).

## 5. Migrar desde la versión anterior

Si la versión anterior guardaba en otro sitio (`SharedPreferences`, un `.properties`), una `DataMigration` lo pasa a DataStore antes de la primera lectura:

```kotlin
private class LegacyMigration(private val legacy: File) : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences) = legacy.exists()

    override suspend fun migrate(currentData: Preferences): Preferences {
        val old = Properties().apply { legacy.reader().use(::load) }
        return currentData.toMutablePreferences().apply {
            old.getProperty("units")?.let { this[UNITS] = it.uppercase() }
        }.toPreferences()
    }

    override suspend fun cleanUp() {
        legacy.delete()
    }
}
```

`shouldMigrate` decide; `migrate` devuelve las preferencias nuevas; `cleanUp` borra lo viejo cuando ya está escrito. Si `cleanUp` no borra, la migración vuelve a correr en cada arranque y pisa lo que la persona cambió después. `theMigrationOverwritesWhatIsThere`, en el ejercicio 02, lo mide: con el archivo viejo ahí, gana lo suyo. Para `SharedPreferences`, AndroidX trae `SharedPreferencesMigration`, que ya hace las tres cosas.

## Para recordar

- Preferencias en DataStore, no en `SharedPreferences`: `Flow` para leer, `edit` para escribir.
- Las claves llevan el tipo; lo que falta es `null` y el valor por defecto es tuyo.
- Un DataStore por archivo y por proceso: créalo una vez.
- Un `corruptionHandler` para no caerse, y una `DataMigration` que borre lo viejo en `cleanUp`.
