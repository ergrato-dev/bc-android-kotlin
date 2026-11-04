# Glosario — Semana 26

| Término | Definición |
| --- | --- |
| **AccessibilityNodeInfo** | Lo que Android da a TalkBack de cada parada: texto, papel, estado y acciones; sale del árbol de semántica unido |
| **Árbol de semántica** | Los nodos con significado que salen de los composables: lo que leen los tests y TalkBack |
| **Archivo de estabilidad** | Un `.conf` (`stabilityConfigurationFiles`) que declara estables clases que no pasan por el compilador de Compose |
| **ATF (Accessibility Test Framework)** | La herramienta de Google que revisa una pantalla entera; en Compose, `enableAccessibilityChecks()`. En Robolectric no comprueba nada |
| **clearAndSetSemantics** | Borra la semántica de un nodo y de sus hijos y pone la que se le da; con `{}`, el nodo solo se ve |
| **contentDescription** | El nombre de lo que no tiene texto (un icono, un dibujo); `null` si solo decora |
| **derivedStateOf** | Un estado calculado a partir de otros que solo avisa cuando cambia su resultado |
| **Encabezado (heading)** | Un nodo marcado con `heading()`; TalkBack tiene un gesto para saltar de uno a otro |
| **Estable** | Un tipo que Compose puede comparar con `equals`: primitivos, `String`, `enum`, funciones y clases de `val` estables |
| **@Immutable / @Stable** | Promesas al compilador de Compose: nada cambia, o lo que cambia avisa. No se comprueban |
| **Inestable** | Un tipo que puede cambiar sin que Compose se entere; con *strong skipping* se compara con `===` |
| **Informes del compilador** | `*-composables.txt`, `*-classes.txt` y métricas en JSON, con la estabilidad de cada parámetro y clase (`reportsDestination`) |
| **liveRegion** | Un nodo que TalkBack lee solo cuando cambia: `Polite` espera, `Assertive` interrumpe |
| **mergeDescendants** | Un nodo que une a sus hijos en una sola parada; lo hacen `clickable`, `toggleable` y los botones de Material |
| **minimumInteractiveComponentSize** | Modifier que reserva 48 x 48 dp para lo que se pulsa; va antes del `size` |
| **Role** | El papel que lee TalkBack: `Button`, `Checkbox`, `Switch`, `RadioButton`, `Tab`, `Image`, `DropdownList` |
| **skippable / restartable** | Una función que Compose se puede saltar si sus parámetros no cambian / que puede volver a ejecutar sola |
| **Strong skipping** | Activado por defecto desde Kotlin 2.0.20: toda función es `skippable` y las lambdas se recuerdan solas |
| **toggleable** | Modifier de lo que se activa y desactiva, con su papel y su estado (`ToggleableState`) |
| **touchBoundsInRoot** | El área de toque de un nodo; Compose la agranda a 48 dp aunque el nodo sea más pequeño |
