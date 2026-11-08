# Glosario — Semana 28

| Término | Definición |
| --- | --- |
| **ATOMIC_MOVE** | Opción de `Files.move` que hace el cambio de nombre de un golpe: quien lee ve el archivo de antes o el nuevo, nunca uno a medias |
| **backgroundScope** | El ámbito de `runTest` para corrutinas que acompañan al test (recoger un flujo); se cancela solo al terminar |
| **Channel** | Una cola de corrutinas: lo que entra espera a que alguien lo saque, y cada elemento sale una vez, a uno solo. `BUFFERED` le da sitio para 64 |
| **Contenedor (DI)** | El sitio que crea las dependencias y las entrega; en la DI a mano de la Bitácora, `BitacoraApp` |
| **Efecto (effect)** | Lo que pasa una vez y no tiene que volver a salir al pintar de cero: un aviso, abrir el selector de compartir |
| **Evento como estado** | La alternativa de Google a los efectos: el evento va en el `UiState` y la pantalla avisa cuando lo ha consumido |
| **Fake** | Una implementación simple y de verdad para los tests (en memoria); se mira su resultado, no las llamadas que recibe |
| **Flujo unidireccional (UDF)** | El estado baja a la pantalla, lo que pasa sube, y un solo sitio decide el estado nuevo |
| **Fuente de verdad (source of truth)** | El único sitio donde un dato es el bueno; los demás son copias |
| **Función pura** | La que, con la misma entrada, da siempre la misma salida y no hace nada más |
| **Inyección de dependencias (DI)** | Recibir lo que necesitas (normalmente por el constructor) en vez de crearlo tú |
| **Intent (MVI)** | Algo que ha pasado en la pantalla, como un dato; nada que ver con el `Intent` de Android |
| **MVI** | Model-View-Intent: una sola entrada de intents, un reducer puro y los efectos aparte |
| **MVVM** | Model-View-ViewModel: el estado vive en un ViewModel que la vista observa |
| **receiveAsFlow** | Convierte un `Channel` en un `Flow` que, al recogerlo, saca elementos del canal |
| **Reducer** | La función pura `(estado, intent) -> estado nuevo (+ efecto)` |
| **Repositorio** | La clase que da acceso a unos datos y decide de dónde salen; la pantalla solo ve su interfaz |
| **runTest** | Ejecuta un test con corrutinas con un reloj virtual: los `delay` no esperan de verdad |
| **sealed interface** | Una interfaz cuyas implementaciones conoce el compilador: el `when` sobre ella puede ser exhaustivo |
| **setMain** | `Dispatchers.setMain(...)` de `kotlinx-coroutines-test`: pone un *dispatcher* de prueba donde va `Dispatchers.Main` |
| **UiState** | Un solo valor inmutable con todo lo que pinta una pantalla |
| **viewModelFactory** | El constructor de *factories* de `lifecycle-viewmodel`: con `initializer { ... }`, cómo crear un ViewModel con argumentos |
