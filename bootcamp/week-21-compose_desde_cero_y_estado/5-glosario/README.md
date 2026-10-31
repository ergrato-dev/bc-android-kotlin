# Glosario — Semana 21

| Término | Definición |
| --- | --- |
| **BOM de Compose** | `androidx.compose:compose-bom`: fija la versión de todas las bibliotecas de Compose a la vez |
| **collectAsState** | Convierte un `StateFlow` en un `State` de Compose; recoge siempre, también con la app detrás |
| **collectAsStateWithLifecycle** | Como `collectAsState`, pero solo recoge mientras la actividad está al menos en `STARTED` |
| **combine** | Junta varios `Flow` en uno que emite cada vez que cambia alguno |
| **Composable** | Función marcada con `@Composable` que describe un trozo de pantalla a partir de sus parámetros y su estado |
| **Composición** | El árbol de composables que pinta la pantalla, con el estado que guardan en él |
| **createAndroidComposeRule** | Regla de test que lanza una actividad y busca en lo que pinta con Compose |
| **createComposeRule** | Regla de test que pinta un composable suelto en una actividad vacía |
| **createEmptyComposeRule** | Regla de test que no pinta nada y busca en lo que pinten las actividades del test |
| **Fuente de verdad** | El único sitio donde vive un dato; dos copias del mismo dato acaban diciendo cosas distintas |
| **getStateFlow** | Da un `StateFlow` de una clave del `SavedStateHandle`, que cambia cuando se escribe esa clave |
| **mutableStateOf** | Crea un estado de Compose: leerlo apunta la dependencia y escribirlo programa una recomposición |
| **Plugin del compilador de Compose** | `org.jetbrains.kotlin.plugin.compose`: transforma los composables al compilar; lleva la versión de Kotlin |
| **Recomposer** | La parte del runtime de Compose que vuelve a ejecutar los composables cuando cambia el estado que leyeron |
| **Recomposición** | Volver a ejecutar los composables que leyeron un estado que ha cambiado |
| **remember** | Guarda un valor en la composición entre recomposiciones; se pierde al destruirse la actividad |
| **rememberSaveable** | Como `remember`, y además guarda el valor en el `Bundle`: sobrevive al giro y a la muerte del proceso |
| **Saver** | Dice a `rememberSaveable` cómo convertir un objeto en algo que cabe en un `Bundle`, y cómo volver |
| **Semántica** | El árbol de nodos con propiedades (texto, seleccionado, papel) y acciones que ven los tests y TalkBack |
| **setContent** | Función de `activity-compose` que pone un composable como contenido de la actividad |
| **SharingStarted.WhileSubscribed** | `stateIn` recoge mientras alguien recoja el resultado, y un tiempo después del último |
| **Sin estado** (*stateless*) | Composable que recibe todo lo que pinta y avisa de los eventos; no decide nada |
| **State hoisting** | Subir el estado de un composable a quien lo usa: el estado baja como parámetros y los eventos suben como lambdas |
| **stateIn** | Convierte un `Flow` en un `StateFlow` con un valor inicial, en un ámbito |
| **StateRestorationTester** | Simula en un test el viaje del estado de `rememberSaveable` por el `Bundle` |
| **ui-test-manifest** | Biblioteca que añade al manifiesto de debug la actividad vacía de `createComposeRule` |
| **UiState** | La clase con todo lo que pinta una pantalla, que el `ViewModel` publica como un `StateFlow` |
| **UnrememberedMutableState** | Error de lint: un `mutableStateOf` creado durante la composición sin `remember` |
| **viewModel()** | Función de `lifecycle-viewmodel-compose` que da el `ViewModel` del dueño más cercano, la actividad |
