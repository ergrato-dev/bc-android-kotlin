# Rúbrica de Evaluación — Semana 29: Hilt y DataStore

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: Hilt y las preferencias de la lista |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué tres cosas hacía `BitacoraApp` en la semana 28 que ahora hace Hilt? ¿Qué es Dagger, qué añade Hilt, y qué hace KSP? | 10 |
| 2 | ¿Para qué sirven `@HiltAndroidApp`, `@AndroidEntryPoint` y `@HiltViewModel`? ¿Qué error da cada una si falta, y cuándo sale? | 10 |
| 3 | `@Inject constructor`, `@Binds` y `@Provides`: ¿cuándo usas cada uno? ¿Por qué un módulo con `@Binds` es una `interface` y uno con `@Provides` puede ser un `object`? | 10 |
| 4 | ¿Qué es `@BindsInstance`? ¿Por qué en Hilt no lo escribes para el `Context`? ¿Qué es `@ApplicationContext`? | 10 |
| 5 | Tienes dos `File` en el grafo. ¿Qué dice Dagger, y cómo lo resuelves? ¿Por qué un *qualifier* propio y no `@Named`? | 10 |
| 6 | ¿Qué quiere decir `@Singleton`? ¿Cuántos repositorios hay con dos componentes? ¿Por qué `Planner` (o el ViewModel) no lleva scope? | 10 |
| 7 | ¿Qué pasa si quitas `@Singleton` del `@Provides` de los ajustes? ¿Por qué Dagger no lo detecta? ¿Y si el scope del binding no es el del componente? | 10 |
| 8 | ¿Qué resuelve DataStore frente a `SharedPreferences`? ¿Cuándo Preferences, cuándo Proto y cuándo una base de datos? | 10 |
| 9 | ¿Qué hace `ReplaceFileCorruptionHandler`, y por qué vale para preferencias y no para las observaciones? En una `DataMigration`, ¿qué pasa si `cleanUp` no borra lo viejo? | 10 |
| 10 | ¿Qué tests de la Bitácora no necesitan Hilt, y por qué? En `HiltTest`, ¿qué hacen `HiltTestApplication`, la regla con `order = 0` y `@TestInstallIn`? ¿Por qué `"+es"` y no `"es"`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Saber cómo se crea cada cosa, decidir cuánto vive (`by lazy` en la `Application`) y entregarla (la `viewModelFactory`). Dagger es un generador de código que lee las anotaciones y escribe al compilar las clases que crean y entregan las dependencias; si el grafo está mal, no compila. Hilt es Dagger con los componentes de Android ya hechos y conectados a sus clases. KSP ejecuta el compilador de Dagger dentro del build de Kotlin (sustituye a `kapt`).
2. `@HiltAndroidApp` genera el grafo de la app y lo arranca con la `Application`; `@AndroidEntryPoint` conecta la actividad al grafo y le da una factory que sabe crear `@HiltViewModel`; `@HiltViewModel` apunta el ViewModel en el grafo. Sin `@AndroidEntryPoint`: `Cannot create an instance of class dev.bitacora.BitacoraViewModel`. Sin `@HiltAndroidApp`: `Hilt Activity must be attached to an @HiltAndroidApp Application`. Sin `@HiltViewModel`, la factory de Hilt no lo conoce y falla igual que el primero. Los tres salen al ejecutar, en el primer test que abre la actividad.
3. `@Inject constructor` si la clase es tuya y concreta. `@Binds` para decir qué implementación (con `@Inject`) tiene una interfaz. `@Provides` para lo que se crea con código: una clase de una biblioteca, una factory, algo que necesita una ruta. `@Binds` no tiene cuerpo, Dagger solo lee la firma: va en algo abstracto. Las funciones `@Provides` tienen cuerpo y no necesitan estado: en un `object`, Dagger las llama sin instancia.
4. Un valor que se le da al componente al crearlo (`create(@BindsInstance dir: File)`) y que queda en el grafo para quien lo pida. Hilt crea él mismo el componente de la app y ya le mete la `Application`. `@ApplicationContext` es el *qualifier* de ese `Context`, el de la app, que vive con el proceso.
5. `[Dagger/DuplicateBindings]`, con los dos sitios. Un *qualifier* (`@PhotosDir`) en el `@Provides` y en quien lo pide. `@Named("...")` funciona, pero es un texto: una errata no la ve el compilador.
6. Una instancia por componente, no una en todo el programa. Con dos componentes, dos repositorios (`twoComponentsTwoRepositories`). `Planner` no tiene estado que compartir: un scope le costaría un cerrojo y una instancia guardada para nada. El ViewModel vive lo que dice su `ViewModelStore`, no Dagger.
7. Cada ViewModel recibe un DataStore nuevo sobre el mismo archivo; al abrir la actividad otra vez, `There are multiple DataStores active for the same file` y el filtro no sale. El grafo es correcto: lo que está mal es cuántas veces se crea, y eso Dagger no lo sabe. Con un scope que no es el del componente: `[Dagger/IncompatiblyScopedBindings]`, al compilar.
8. Lee con un `Flow` en segundo plano (no bloquea al cargar), escribe con `suspend` y en transacción (no en el hilo principal al pausar), avisa de un archivo roto y emite los cambios por el mismo `Flow`. Preferences para claves sueltas con tipo; Proto para un objeto con esquema que quieres validar; una base de datos para datos con relaciones, consultas o muchas filas.
9. Si el archivo no se puede leer, lo cambia por lo que devuelve (unas preferencias vacías) en vez de lanzar `CorruptionException`. Perder unos filtros es aceptable; perder las observaciones de la persona no, por eso aquellas se apartan a `.bad`. Sin borrar lo viejo, `shouldMigrate` sigue dando `true`, la migración corre en cada arranque y pisa lo que la persona cambió después.
10. Los del reducer, `visible()`, el ViewModel (con los *fakes* por el constructor) y el DataStore de `data`: llaman al constructor, y `@Inject` no cambia nada para quien lo hace. `HiltTestApplication` sustituye a `BitacoraApp` y usa el componente generado para el test; la regla crea ese componente antes de cada test y tiene que ir antes que cualquier otra que abra algo; `@TestInstallIn(replaces = [AppModule::class])` pone `FakeModule` en lugar de `AppModule` en todos los tests del *source set*. `"es"` sustituye lo de `robolectric.properties` (vuelve a 470 dp); `"+es"` lo añade.

**Ejercicio 01, paso 4**: (a) `[Dagger/MissingBinding]`, en `kspKotlin`: `ChecklistRepository` no tiene binding, con el camino desde `TripComponent.planner()` o `repository()`. (b) `[Dagger/DuplicateBindings]`, con los dos `@Binds`. (c) `[Dagger/IncompatiblyScopedBindings]`: un componente sin scope no puede guardar un binding con `@Singleton`, porque no tiene dónde. (d) El repositorio se envuelve una vez en `DoubleCheck.provider(...)` y siempre devuelve el mismo; el `Planner` es un `new Planner(...)` en cada llamada. `DoubleCheck` es el `by lazy` de `BitacoraApp`.

**Ejercicio 02, paso 5**: (a) `CorruptionException`; en la app, en quien recoge `data`: la pantalla se caería al abrir. (b) `IllegalStateException`: el formato de Preferences lo exige por el nombre, para no abrir por error otro archivo. (c) `theOldFileIsMigrated` encuentra el archivo viejo; en la app, la migración vuelve a correr al abrir y la persona ve `IMPERIAL` otra vez. (d) Cada ViewModel recibiría un DataStore nuevo sobre el mismo archivo y el segundo fallaría con `multiple DataStores active`; el grafo es correcto, el fallo es de cuántas instancias hay.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — El grafo de la salida (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 en verde | 13 |
| `@Binds` con la del archivo; `@Singleton` en el binding y en el componente, no en `Planner`; `./gradlew build` en verde (revisión del código) | 3 |
| `NOTAS.md`: paso 4 explicado, con las tres líneas `[Dagger/...]` | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Las preferencias que se quedan (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| `PreferenceDataStoreFactory` con el `scope` que recibe; una clave por campo y un `edit` por cambio; `corruptionHandler`; la migración con `cleanUp` (revisión) | 3 |
| `NOTAS.md`: paso 5 explicado, con las excepciones de (a) y (b) | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Hilt y las preferencias de la lista

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 28 y los nuevos del starter en verde, y mínimo 6 nuevos, con el valor esperado escrito a mano: añadir con filtro, `Replace` con filtro, ajustes que llegan del repositorio, el último `save` gana, el archivo roto se recupera, el grafo de test con "Solo altas" | 25 |
| Hilt | `@HiltAndroidApp` sin contenedor a mano; `AppModule` con `@Provides @Singleton` y `@ApplicationContext`; `@HiltViewModel` con `@Inject constructor`; `@AndroidEntryPoint` y `viewModels()` sin factory | 25 |
| DataStore y filtros | `DataStoreSettingsRepository` en `data` con `corruptionHandler`; los filtros en `visible()`, sin tocar la lista entera; guardar solo si los ajustes cambian | 15 |
| Adaptación al dominio | Los filtros y textos de tu dominio; README con lo que pide el paso 10 | 15 |
| Entrega | Rama `week-29`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Queda un `viewModelFactory` o un repositorio creado fuera del grafo | -10 |
| El DataStore de los ajustes sin `@Singleton` | -10 |
| Los filtros borran o reordenan las observaciones del estado (no solo lo que se ve) | -10 |
| `core` o `data` dependen de Android o de Hilt | -10 |
| Un test de la semana 28 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-29` → `main` en el repositorio del aprendiz.
