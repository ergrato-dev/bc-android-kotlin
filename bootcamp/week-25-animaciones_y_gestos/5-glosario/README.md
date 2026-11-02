# Glosario — Semana 25

| Término | Definición |
| --- | --- |
| **anchoredDraggable** | Arrastre en un eje hasta posiciones fijas (anclas); lo usa `SwipeToDismissBox` por dentro |
| **animate\*AsState** | Devuelve un valor que va, fotograma a fotograma, del de antes al nuevo destino: `animateColorAsState`, `animateFloatAsState`... |
| **animateItem** | Modifier de una fila de `LazyColumn` con clave que anima su aparición, su salida y su cambio de sitio |
| **AnimatedContent** | Cambia un contenido por otro con una transición; durante ella, los dos están en el árbol |
| **AnimatedVisibility** | Hace que un contenido entre y salga con transiciones; mientras sale, sigue en el árbol |
| **clearAndSetSemantics** | Borra la semántica de un nodo y sus hijos y pone la que se le dé; con `{}`, el nodo solo se ve |
| **combinedClickable** | Toque, doble toque y pulsación larga, con la semántica de cada uno y sus etiquetas |
| **CustomAccessibilityAction** | Una acción con nombre que TalkBack ofrece en el menú de acciones de un nodo (`customActions`) |
| **drawBehind** | Modifier que dibuja detrás del contenido; lo que lee dentro solo invalida el dibujo, no la composición |
| **Fases de Compose** | Composición, medida y colocación, y dibujo; un estado leído en una fase solo invalida esa y las siguientes |
| **graphicsLayer** | Modifier que aplica opacidad, escala, giro o desplazamiento al dibujar, sin recomponer |
| **mainClock** | El reloj de Compose en un test; con `autoAdvance = false`, solo avanza con `advanceTimeBy` |
| **MonotonicFrameClock** | El reloj de fotogramas que da la hora a las animaciones de Compose |
| **performTouchInput** | Simula el dedo sobre un nodo en un test: `click`, `longClick`, `swipeLeft`... |
| **Snackbar** | Aviso breve abajo de la pantalla, con una acción opcional; `showSnackbar` espera y dice cómo terminó |
| **spring** | Animación como un muelle, sin duración fija, que se puede interrumpir sin saltos; la de por defecto |
| **SwipeToDismissBox** | El componente de Material 3 que deja apartar su contenido deslizando y avisa con `onDismiss` |
| **tween** | Animación de duración fija con una curva de aceleración |
