# Claves y muerte del proceso

> Android mata el proceso de una app en segundo plano cuando necesita memoria, y al volver el usuario espera estar donde lo dejó: en la observación A-1, no en la lista. Lo que sobrevive es lo que se guardó en el estado de la actividad ([semana 16](../../week-16-actividades_y_ciclo_de_vida/README.md)). Esta teoría es cómo se guarda el back stack, qué tiene que cumplir una clave para poder guardarse, y por qué lleva un código y no la observación.

## 🎯 Objetivos

- Explicar por qué el back stack se crea con `rememberNavBackStack` y no con una lista cualquiera
- Escribir claves que se puedan guardar
- Decidir qué lleva una clave: lo justo para encontrar lo que se enseña

## 1. rememberNavBackStack

El back stack podría ser una `mutableStateListOf<NavKey>(SessionKey)` dentro de un `remember`: Compose repinta al cambiarla, y `NavDisplay` la acepta. Pero `remember` vive lo que vive la composición.

> 🔬 Medido en la sonda: con `remember { mutableStateListOf(...) }`, tras abrir A-1 y matar el proceso (`saveInstanceState` y una actividad nueva con ese `Bundle`), la app vuelve a la lista. Y atrás, en vez de volver, cierra la actividad: el back stack es otra vez de una clave.

> 🔬 Medido en la sonda: con `rememberNavBackStack(ListKey)`, la misma prueba vuelve a A-1, y atrás vuelve a la lista.

`rememberNavBackStack` guarda la lista en el estado de la actividad, como un `rememberSaveable`. Para eso tiene que convertir cada clave en algo que quepa en un `Bundle`: la serializa.

## 2. Claves serializables

Una clave es un `data object` (sin datos) o una `data class` (con ellos), `@Serializable` y `NavKey`. El plugin de serialización genera el serializador al compilar.

> 🔬 Medido en el ejercicio 01 (paso 6b): sin `@Serializable` en `ObservationKey`, compila y la app funciona, hasta que se guarda el estado: falla `theBackStackSurvivesProcessDeath` con `SerializationException: Serializer for class 'ObservationKey' is not found.` En el móvil, la app se cerraría al irse a segundo plano con una observación abierta.

El fallo no sale al compilar ni al navegar: sale al guardar. Por eso cada proyecto con Navigation 3 tiene un test que mata el proceso con una clave de cada tipo en el back stack.

## 3. Un código, no la observación

La tentación es meter en la clave lo que la pantalla necesita: `ObservationKey(val observation: Observation)`. Tres problemas:

- **No compila**, si `Observation` no es serializable.

  > 🔬 Medido en el ejercicio 01 (paso 6e): `Serializer was not found for type 'Observation?'`.

- **Es una copia.** Si alguien cambia la observación mientras está en el back stack, la clave sigue con la vieja. La verdad está en el `ViewModel`, una sola vez.
- **Ocupa.** El estado guardado viaja por Binder, con un búfer de 1 MB para todo el proceso, y la documentación de Android pide quedarse muy por debajo; una lista larga en una clave lo acerca.

Por eso la clave lleva lo justo para encontrar lo que se enseña, y la pantalla lo busca:

```kotlin
entry<ObservationKey> { key ->
    val observation = state.observations.find { it.code == key.code } ?: return@entry
    ObservationScreen(observation, ...)
}
```

Es lo mismo que en la semana 18 con el `Intent`: el código en los extras, no el objeto.

## 4. El primer fotograma tras la muerte

Tras la muerte del proceso, el back stack vuelve entero en el primer fotograma, pero lo que la pantalla busca puede no estar aún: el estado de la Bitácora sale del `SavedStateHandle` a través de un `StateFlow`, y `collectAsStateWithLifecycle` empieza por su valor inicial.

> 🔬 Medido en el proyecto: con `state.observations.first { it.code == key.code }` en vez de `find ... ?: return@entry`, fallan los 2 tests de la muerte del proceso con `NoSuchElementException: Collection contains no element matching the predicate.`

Un destino que busca por código tiene que aguantar no encontrarlo: un fotograma en blanco, y al siguiente ya está.

## ✅ Comprueba

- [ ] ¿Qué diferencia hay, para el usuario, entre `remember { mutableStateListOf(...) }` y `rememberNavBackStack(...)`?
- [ ] Quitas `@Serializable` de una clave. ¿Cuándo falla, y con qué?
- [ ] ¿Por qué `ObservationKey` lleva el código y no la observación? Da tres razones.
