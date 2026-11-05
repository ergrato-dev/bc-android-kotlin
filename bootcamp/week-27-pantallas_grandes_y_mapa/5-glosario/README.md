# Glosario — Semana 27

| Término | Definición |
| --- | --- |
| **API key (clave de API)** | El identificador de tu proyecto de Google Cloud que pide Maps; va dentro del APK, así que la protege su restricción por paquete y SHA-1, no el secreto |
| **BackNavigationBehavior** | Qué quita atrás en un list-detail: `PopLatest`, `PopUntilScaffoldValueChange` (por defecto), `PopUntilCurrentDestinationChange` o `PopUntilContentChange` |
| **Bisagra (hinge)** | La zona donde se pliega un plegable; en Compose, un `HingeInfo` de la `Posture`, con sus `bounds` en píxeles de la ventana |
| **CameraPositionState** | El estado de la cámara de un `GoogleMap`: dónde mira y con qué zoom; se mueve con `move` o `animate` y un `CameraUpdate` |
| **Clase de tamaño (WindowSizeClass)** | El tramo en el que cae el ancho (y el alto) de la ventana: compact, medium, expanded, large o extra-large |
| **Compact / medium / expanded** | Menos de 600 dp, de 600 a 840, y de 840 a 1200 de ancho |
| **detailPane / listPane** | Los metadatos de una entrada de Navigation 3 que dicen a `ListDetailSceneStrategy` en qué panel va |
| **Escena (Scene)** | Lo que `NavDisplay` pinta de una vez: una o varias entradas del back stack |
| **Estrategia de escenas (SceneStrategy)** | Lo que decide, mirando el back stack, si varias entradas van juntas en una escena |
| **FoldingFeature** | El pliegue de la pantalla en Jetpack WindowManager: `state`, `orientation`, `isSeparating` y `bounds` |
| **GoogleMap** | El composable de Maps Compose: un `MapView` dentro de Compose, con su propia composición para los marcadores |
| **Hueco (detailPlaceholder)** | Lo que va en el panel de detalle cuando el back stack solo tiene la lista |
| **LatLng / LatLngBounds** | Un punto (latitud, longitud) y el rectángulo más pequeño que contiene varios; clases de datos que funcionan en la JVM |
| **List-detail** | La forma de pantalla con una lista y el detalle de lo elegido: una pila en el móvil, dos paneles con sitio |
| **manifestPlaceholders** | Valores que AGP pone en los `${...}` del manifiesto al fusionarlo; aquí, la clave de Maps desde una propiedad de Gradle |
| **Marker** | Un marcador del mapa, con su posición (`MarkerState`), su título, su texto y su globo |
| **Postura de libro (book)** | Medio cerrado con la bisagra en vertical: dos páginas, una a cada lado |
| **Postura de mesa (tabletop)** | Medio cerrado con la bisagra en horizontal: arriba lo que se lee, abajo lo que se toca; `Posture.isTabletop` |
| **Posture** | La postura de la ventana en `material3-adaptive`: si es mesa y qué bisagras tiene |
| **window-testing** | La librería de tests de Jetpack WindowManager que pone pliegues de mentira (`WindowLayoutInfoPublisherRule`) |
| **WindowAdaptiveInfo** | Lo que da `currentWindowAdaptiveInfoV2()`: la clase de tamaño y la postura de la ventana, como estado de Compose |
