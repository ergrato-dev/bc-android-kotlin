# Glosario — Semana 33

| Término | Definición |
| --- | --- |
| **AAR** | El archivo que produce una biblioteca de Android: sus clases, sus recursos y su manifiesto. Va dentro de una app; no se instala solo |
| **ABI** (*Application Binary Interface*) | Lo que un módulo enseña a quien lo usa: sus clases, funciones y propiedades públicas, con sus tipos. Un cambio que no la toca no recompila los módulos de arriba |
| **`api`** | Configuración de Gradle para una dependencia que sale en las firmas públicas del módulo: quien depende del módulo también la ve al compilar |
| **Biblioteca de Android** (`com.android.library`) | Módulo con código, recursos y manifiesto de Android que no es una app: sin `applicationId`, sin `versionCode`, sin firma |
| **`CommonExtension`** | La parte común del `android { }` de una app y de una biblioteca en AGP. Un convention plugin que la configura sirve para las dos |
| **Compile avoidance** | Que Gradle y Kotlin no recompilen un módulo cuando la ABI de sus dependencias no ha cambiado, aunque sí haya cambiado su código |
| **`configureEach`** | Configura cada elemento de una colección de Gradle, los que hay y los que se añadan después, sin forzar a crearlos antes |
| **Core** (módulo) | Lo que comparten varias features: el dominio, la base de datos, la red, el repositorio. No sabe de pantallas |
| **Feature** (módulo) | Una pantalla o un flujo, con sus textos y sus dependencias propias. Solo la usa la app; no conoce otras features |
| **Grafo de módulos** | Quién depende de quién. Gradle prohíbe los ciclos; las demás reglas (capas, features que no se conocen) son del proyecto |
| **`implementation`** | Configuración de Gradle para una dependencia que se usa dentro del módulo: va en el APK, pero quien depende del módulo no la ve al compilar |
| **`internal`** | Visibilidad de Kotlin: solo dentro del mismo módulo, tests del módulo incluidos |
| **Manifiesto final** (*merged manifest*) | El de la app junto con los de todas sus bibliotecas. La app manda, y quita lo que no quiere con `tools:node="remove"` |
| **Namespace** | El paquete de la `R` y de `BuildConfig` de un módulo de Android, y el que completa los nombres relativos de su manifiesto. Uno distinto por módulo |
| **Overlay de recursos** | Cuando la app define un recurso con el mismo nombre que una biblioteca y el suyo sustituye al de la biblioteca, también dentro del código de la biblioteca |
| **`ProjectDependency`** | Una dependencia hacia otro módulo del mismo build (`project(":core:data")`). Su `path` es la ruta del módulo al que apunta |
| **`R` no transitiva** | Cada módulo tiene su clase `R` con sus recursos y nada más. Por defecto desde AGP 8. Un recurso de una biblioteca se usa con la `R` de la biblioteca |
| **`resourcePrefix`** | Prefijo que AGP pide a los recursos de una biblioteca, con un aviso de lint, para que no choquen con los de otras |
| **Smart cast** | Cuando Kotlin trata un valor como de un tipo más concreto tras comprobarlo (`!= null`, `is`). No funciona con propiedades públicas de otro módulo |
| **`testOptions.targetSdk`** | El `targetSdk` de los tests de una biblioteca, que no tiene uno propio. Robolectric lo usa para elegir qué Android simula |
