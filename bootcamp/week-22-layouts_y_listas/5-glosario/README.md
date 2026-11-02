# Glosario — Semana 22

| Término | Definición |
| --- | --- |
| **Alignment** | Cómo se colocan los hijos en la dirección que no es la principal: en una `Row`, en vertical; en un `Box`, en las dos |
| **Arrangement** | Cómo se reparte lo que sobra en la dirección principal: `SpaceBetween`, `spacedBy(12.dp)`... |
| **Box** | Layout que pone a sus hijos uno encima de otro; mide lo que el más grande |
| **Clave** (*key*) | Lo que identifica a un elemento de una lista: su estado va con ella aunque cambie de posición. Única, estable y que quepa en un `Bundle` |
| **Column** | Layout que pone a sus hijos uno debajo de otro |
| **contentType** | Dice a un `LazyColumn` qué elementos tienen la misma forma, para reutilizar lo compuesto entre ellos al desplazarse |
| **derivedStateOf** | Calcula un estado a partir de otros y solo avisa cuando cambia el resultado; va dentro de `remember` |
| **FrequentlyChangingValue** | Aviso de lint: leer en la composición un valor que cambia muy a menudo, como `firstVisibleItemIndex` |
| **hasScrollAction** | Buscador de los tests que encuentra el nodo que se desplaza: la lista |
| **item / items** | En el DSL de un `LazyColumn`, un elemento suelto, o uno por cada dato de una lista |
| **key()** | Función de `androidx.compose.runtime` que da clave a lo que se compone dentro de un bucle, fuera de una lista perezosa |
| **LazyColumn** | Lista vertical que solo compone los elementos que caben, y va componiendo y soltando al desplazarse |
| **LazyListState** | El estado de un `LazyColumn`: el primer elemento visible y cuánto se ha desplazado dentro de él |
| **minimumInteractiveComponentSize** | Modifier que reserva 48 dp para lo que se pulsa, aunque se vea más pequeño |
| **Modifier** | Envoltorio que dice cómo se coloca, cuánto mide, cómo se pinta por fuera y qué hace al tocarlo un composable; se lee de fuera adentro |
| **ModifierParameter** | Aviso de lint: el parámetro `Modifier` de un composable tiene que llamarse `modifier` |
| **performScrollToKey** | Acción de los tests que desplaza una lista perezosa hasta el elemento con esa clave |
| **rememberCoroutineScope** | Da un `CoroutineScope` que se cancela cuando el composable sale de la composición, para lanzar corrutinas desde un evento |
| **rememberLazyListState** | Crea y guarda (con `rememberSaveable`) el estado de un `LazyColumn`; sobrevive al giro y a la muerte del proceso |
| **requiredSize** | Pide un tamaño aunque se salga de las restricciones del padre |
| **Restricciones** (*constraints*) | El ancho y el alto mínimos y máximos que un layout da a cada hijo al medirlo |
| **Row** | Layout que pone a sus hijos uno al lado de otro |
| **size** | Pide un tamaño dentro de lo que dejan las restricciones del padre |
| **stickyHeader** | Elemento de un `LazyColumn` que se queda pegado arriba mientras su grupo se ve |
| **testTag** | Etiqueta para encontrar en un test un nodo que no tiene texto; se pasa por el parámetro `modifier` |
| **verticalScroll** | Modifier que hace que una `Column` se desplace; compone todos sus hijos, se vean o no |
| **weight** | Dentro de una `Row` o una `Column`, el hijo se mide el último con lo que dejan los demás, repartido en proporción |
