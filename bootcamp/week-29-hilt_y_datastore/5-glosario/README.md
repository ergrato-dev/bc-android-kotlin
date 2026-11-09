# Glosario — Semana 29

| Término | Definición |
| --- | --- |
| **@AndroidEntryPoint** | Conecta una actividad (o fragmento, vista, servicio) al grafo de Hilt y le da una factory que sabe crear los `@HiltViewModel` |
| **@ApplicationContext** | *Qualifier* de Hilt para el `Context` de la app, que vive con el proceso |
| **Binding** | La respuesta del grafo a "¿cómo consigo un X?": un `@Inject constructor`, un `@Binds` o un `@Provides` |
| **@Binds** | Función abstracta de un módulo que dice qué implementación tiene una interfaz; Dagger solo lee la firma |
| **@BindsInstance** | Mete en el grafo un valor que se da al crear el componente |
| **Componente** | En Dagger, el grafo que se puede crear y al que se le piden cosas; en Hilt, uno por cada tipo de clase de Android, ya hechos |
| **CorruptionException** | La excepción de DataStore cuando el archivo no se puede leer, si no hay `corruptionHandler` |
| **Dagger** | Generador de código para DI: lee las anotaciones y escribe al compilar las clases que crean y entregan las dependencias |
| **DataMigration** | Paso que DataStore ejecuta antes de la primera lectura para traer datos de otro sitio: `shouldMigrate`, `migrate` y `cleanUp` |
| **DataStore** | Biblioteca de AndroidX para guardar datos pequeños en un archivo, con lectura por `Flow` y escritura `suspend` en transacción |
| **DoubleCheck** | La clase de Dagger que guarda la instancia de un binding con scope: el `by lazy` generado |
| **edit** | La escritura de Preferences DataStore: lee lo último, aplica el bloque y escribe el archivo entero |
| **Grafo de dependencias** | Todas las clases que se pueden crear y lo que necesita cada una |
| **Hilt** | Dagger con los componentes de Android hechos y conectados a sus clases |
| **@HiltAndroidApp** | En la `Application`: genera el grafo de la app y lo arranca con ella |
| **@HiltAndroidTest** | En una clase de test: Hilt genera un componente de la app solo para ella |
| **HiltAndroidRule** | La regla de JUnit que crea el componente de test antes de cada test; va con `order = 0` |
| **HiltTestApplication** | La `Application` de los tests de Hilt, en lugar de la de la app |
| **@HiltViewModel** | Apunta un ViewModel con `@Inject constructor` en el grafo de Hilt |
| **@InstallIn** | Dice en qué componente de Hilt va un módulo |
| **KSP** | *Kotlin Symbol Processing*: ejecuta procesadores de anotaciones como Dagger dentro del build de Kotlin; sustituye a `kapt` |
| **@Module** | Clase con bindings que no salen de un `@Inject constructor` |
| **Preferences DataStore** | DataStore de claves con tipo y sin esquema (`booleanPreferencesKey`…), en un archivo `.preferences_pb` |
| **Proto DataStore** | DataStore de un objeto con esquema (protobuf u otro *serializer*) |
| **@Provides** | Función de un módulo que crea lo que devuelve; sus parámetros salen del grafo |
| **Qualifier** | Anotación que distingue dos bindings del mismo tipo (`@ApplicationContext`, `@Named`, una propia) |
| **ReplaceFileCorruptionHandler** | Le dice a DataStore qué usar en lugar de un archivo roto |
| **Scope** | Cuántas instancias hay de un binding: sin scope, una por petición; con scope, una por componente |
| **@Singleton** | El scope de `SingletonComponent`: una instancia por componente de la app, es decir, por proceso |
| **SingletonComponent** | El componente de Hilt de la app: nace en `Application.onCreate` y vive con el proceso |
| **@TestInstallIn** | Módulo de test que sustituye a otro (`replaces`) en todos los `@HiltAndroidTest` del *source set* |
| **ViewModelComponent** | El componente de Hilt de cada ViewModel; trae su `SavedStateHandle` |
