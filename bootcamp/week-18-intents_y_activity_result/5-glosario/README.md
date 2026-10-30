# Glosario — Semana 18

| Término | Definición |
| --- | --- |
| **ACTION_CHOOSER** | La acción del selector del sistema; `Intent.createChooser` envuelve tu Intent en uno con esta acción |
| **ACTION_SEND** | Acción para mandar algo a otra app: un texto en `EXTRA_TEXT`, un archivo en `EXTRA_STREAM` |
| **ACTION_VIEW** | Acción para mostrar lo que dice `data`: un mapa (`geo:`), una web, un deep link |
| **ActivityNotFoundException** | Fallo de `startActivity` o `launch` cuando ninguna actividad acepta el Intent |
| **ActivityResultContract** | Clase que dice cómo se monta el Intent (`createIntent`) y cómo se lee la respuesta (`parseResult`) |
| **ActivityResultLauncher** | Lo que devuelve `registerForActivityResult`; `launch(input)` abre la otra pantalla |
| **ActivityResultRegistry** | El registro de cada actividad: asigna los `requestCode` y los guarda en el estado |
| **App Link** | Deep link `https://` verificado con `assetlinks.json` en tu dominio; abre tu app sin preguntar |
| **Binder** | Mecanismo con el que se hablan los procesos de Android; los extras viajan por él, con un búfer de 1 MB |
| **BROWSABLE** | Categoría que deja abrir una actividad desde un enlace del navegador o de otra app |
| **checkActivities** | Modo estricto de Robolectric: un Intent sin destino lanza `ActivityNotFoundException` |
| **Componente** | El paquete de la app y el nombre de la clase de un Intent explícito |
| **Contrato** | Ver `ActivityResultContract` |
| **DEFAULT** | Categoría que `startActivity` añade a todo Intent implícito; un filtro sin ella no lo recibe |
| **Deep link** | `Uri` que abre una pantalla concreta de una app, como `bitacora://observation/A-7` |
| **exported** | Atributo del manifiesto: si otras apps pueden abrir la actividad; obligatorio con `intent-filter` |
| **Extra** | Dato de clave y valor que viaja dentro de un Intent, en un `Bundle` |
| **GetContent** | Contrato de AndroidX para elegir un archivo de otra app; devuelve su `Uri` `content://` |
| **Intent** | Mensaje para Android con lo que quieres hacer: qué abrir, o qué acción, y con qué datos |
| **Intent explícito** | El que nombra la clase que se abre |
| **Intent implícito** | El que dice qué hacer (acción, datos, tipo) y deja que Android busque quién lo hace |
| **intent-filter** | Bloque del manifiesto que dice qué Intents implícitos acepta una actividad |
| **IntentCompat** | Clase de `androidx.core` que lee extras con la API de cada versión de Android |
| **MATCH_DEFAULT_ONLY** | Opción de `queryIntentActivities` que busca como `startActivity`: solo filtros con `DEFAULT` |
| **Parcelable** | La forma de Android de copiar un objeto entre procesos |
| **@Parcelize** | Anotación del plugin de Kotlin Parcelize que genera el código de `Parcelable` |
| **queries** | Bloque del manifiesto que declara qué apps o Intents quiere ver la tuya, desde Android 11 |
| **receiveResult** | Método de `ShadowActivity` que entrega un resultado como si la otra pantalla hubiera terminado |
| **registerForActivityResult** | Registra un contrato y un callback; va antes de que la actividad llegue a `STARTED` |
| **RESULT_CANCELED** | Código de resultado `0`: la pantalla se cerró sin `setResult`, por ejemplo al volver atrás |
| **RESULT_OK** | Código de resultado `-1`: la pantalla devolvió lo que se le pidió |
| **requestCode** | Número que une un lanzamiento con su resultado; el registro lo asigna al azar |
| **resolveActivity** | Pregunta al `PackageManager` qué actividad abriría un Intent, o `null` si ninguna |
| **Selector** | La pantalla del sistema para elegir con qué app compartir (*chooser*) |
| **setResult** | Lo que llama la pantalla que devuelve algo, antes de `finish()` |
| **ShadowPackageManager** | Parte de Robolectric para instalar apps y filtros de mentira en los tests |
| **Visibilidad de paquetes** | Regla de Android 11: una app solo ve las otras apps que declara en `<queries>` |
