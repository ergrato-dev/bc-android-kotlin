# Glosario — Semana 24

| Término | Definición |
| --- | --- |
| **App Links** | Enlaces `https://` de un dominio que la app demuestra que es suyo; Android los abre en ella sin preguntar |
| **Back stack** | La lista de claves de los destinos por los que va el usuario; se ve la última |
| **BROWSABLE** | Categoría que añade el navegador al abrir un enlace; sin ella en el `intent-filter`, el enlace no abre la app desde una página |
| **DEFAULT** | Categoría que necesita una actividad para recibir un `Intent` implícito de `startActivity` |
| **DeepLinkRequest** | Lo que Navigation 3 compara con los patrones: la `Uri`, la acción y el tipo de un `Intent`; sin `Uri` ni acción, lanza una excepción |
| **Decorador de entrada** | `NavEntryDecorator`: da a cada entrada algo propio mientras está en el back stack y lo limpia al quitarla |
| **Destino** | Una pantalla de la app identificada por su clave, dentro de la misma actividad |
| **Enlace profundo** | Un enlace que abre una pantalla concreta de la app, no solo la app |
| **entryProvider** | El DSL que dice qué pintar para cada tipo de clave: `entry<T> { key -> ... }` |
| **intent-filter** | La declaración en el manifiesto de qué `Intent` acepta una actividad: acción, categorías y datos |
| **NavDisplay** | El composable de Navigation 3 que pinta la última clave del back stack y recoge atrás |
| **NavKey** | La interfaz de las claves de Navigation 3; con `@Serializable`, la clave se puede guardar |
| **Navigation 3** | La biblioteca de navegación de AndroidX para Compose en la que el back stack es una lista que es tuya |
| **onBack** | El parámetro de `NavDisplay` que se llama con atrás; casi siempre quita la última clave |
| **rememberNavBackStack** | Crea el back stack y lo guarda en el estado de la actividad, serializando cada clave |
| **rememberSaveableStateHolderNavEntryDecorator** | El decorador que da a cada entrada su `rememberSaveable`, que sobrevive mientras está tapada y se borra al quitarla |
| **rememberViewModelStoreNavEntryDecorator** | El decorador de `lifecycle-viewmodel-navigation3` que da a cada entrada su `ViewModel`, borrado al quitarla |
| **UriDeepLinkMatcher** | Un patrón de `Uri` con `{argumentos}` y el serializador de una clave: convierte un enlace que encaja en esa clave |
| **withBackStack** | Dice qué claves quedan debajo de la de un enlace, para que atrás vuelva a la app y no fuera |
