# Buscar, actuar, comprobar: la API de tests de Compose

> Un test de UI hace tres cosas: encuentra un nodo, hace algo con él y comprueba cómo queda la pantalla. Esta teoría repasa las tres partes de la API de Compose (la mayoría ya las has usado desde la semana 21), cómo se combinan los matchers y cómo se escribe un **robot**, la clase que esconde el «cómo» para que los tests digan solo el «qué».

## 🎯 Objetivos

- Combinar matchers para encontrar el nodo exacto
- Elegir la acción: clic, texto, gesto, desplazamiento o acción semántica
- Distinguir `assertExists` de `assertIsDisplayed`
- Escribir un robot para la pantalla de la Bitácora

## 1. La regla

| Regla | Qué pinta | Cuándo |
| --- | --- | --- |
| `createComposeRule()` | Lo que le des con `setContent`, en una actividad vacía (`ui-test-manifest`) | Una pieza o una pantalla sin estado: `ScreenshotTest`, `BitacoraScreenTest` |
| `createAndroidComposeRule<MainActivity>()` | Lo que pinte esa actividad | La app de verdad, en un dispositivo (`DeviceTest`) |
| `createEmptyComposeRule()` | Nada: encuentra lo que pinten las actividades que abra el test | Con `Robolectric.buildActivity`, como `launch()` de la Bitácora (semana 30) |

Las tres son de `androidx.compose.ui.test.junit4.v2`. Las de `junit4` sin `v2` están deprecadas y rompen `-Werror` (semana 21).

## 2. Buscar

Un finder recibe un `SemanticsMatcher`. Los `onNodeWithText` y compañía son atajos.

```kotlin
rule.onNode(hasText("Alta") and hasClickAction())              // el botón, no el texto del resumen
rule.onNode(hasText("riego") and hasAnyAncestor(hasTestTag("observations")))
rule.onAllNodes(hasClickAction()).filter(hasText("A-", substring = true))
```

| Matcher | Encuentra |
| --- | --- |
| `hasText`, `hasContentDescription`, `hasStateDescription` | Por lo que se lee |
| `hasClickAction`, `hasScrollAction`, `hasSetTextAction`, `isToggleable` | Por lo que se puede hacer |
| `isEnabled`, `isSelected`, `isFocused`, `isOn` | Por el estado |
| `hasParent`, `hasAnyAncestor`, `hasAnyChild`, `hasAnyDescendant` | Por la posición en el árbol |
| `and`, `or`, `!` | Combinaciones |

`onNode` espera **exactamente uno**. Si hay dos, falla con `Expected exactly '1' node but found '2' nodes`, y te los lista. `hasText` sin `substring = true` compara el texto entero: el botón «Alta» no se confunde con el resumen («… · Alta: 1») ni con una fila («A-1 · Alta · riego»). El problema aparece con el mismo texto en dos sitios, por ejemplo un título y un botón que dicen lo mismo.

## 3. Actuar

| Acción | Para |
| --- | --- |
| `performClick()` | Pulsar. Va al centro del nodo |
| `performTextInput("riego")`, `performTextReplacement`, `performTextClearance` | Campos de texto. `Input` añade al final |
| `performTouchInput { swipeLeft() }`, `{ click(Offset(140f, 20f)) }`, `{ longClick() }` | Gestos con coordenadas: deslizar para borrar (semana 25), tocar el cuarto círculo |
| `performScrollTo()`, `performScrollToNode(matcher)`, `performScrollToKey("A-40")` | Llevar la lista hasta algo. `ToKey` usa la clave del `items` (semana 22) |
| `performSemanticsAction(SetProgress) { it(4f) }` | Lo que haría TalkBack. Solo funciona si el nodo tiene esa acción |
| `performImeAction()` | La tecla de acción del teclado (Hecho, Buscar) |

Las acciones semánticas no dependen de dónde esté nada. Los gestos sí: `click(Offset(140f, 20f))` cuenta con la densidad 1 de Robolectric (semana 16), y en un dispositivo con densidad 2,75 tocaría otro círculo. Para un test que corra en los dos sitios, calcula con `width` y `height` dentro de `performTouchInput`, que están en píxeles del nodo.

## 4. Comprobar

| Aserción | Comprueba |
| --- | --- |
| `assertExists()` / `assertDoesNotExist()` | Que el nodo está (o no) en el árbol |
| `assertIsDisplayed()` / `assertIsNotDisplayed()` | Que además se ve: dentro de la pantalla y sin recortar del todo |
| `assertTextEquals`, `assertTextContains` | El texto, entero o en parte |
| `assertIsEnabled`, `assertIsOn`, `assertIsSelected` | El estado |
| `assert(hasStateDescription("3 de 5"))` | Cualquier matcher |
| `assertCountEquals(2)` | Sobre `onAllNodes`: cuántos hay |

`assertExists` y `assertIsDisplayed` no son lo mismo. Una fila que el `LazyColumn` no ha compuesto no existe. Una que está compuesta pero queda debajo de los botones fijos existe y no se ve. La pregunta de un test de UI casi siempre es la segunda: ¿lo ve la persona?

## 5. El robot

Los tests de la Bitácora repiten lo mismo: escribir en «Descripción», pulsar un nivel, buscar la fila. Si cambia el texto del campo, hay que cambiar todos. Un **robot** (también llamado *page object*) guarda el «cómo» en una clase, y el test se queda con el «qué»:

```kotlin
class BitacoraRobot(private val rule: ComposeTestRule) {
    fun add(description: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    fun assertRow(text: String) {
        rule.onNodeWithTag("observations").performScrollToNode(hasText(text))
        rule.onNodeWithText(text).assertIsDisplayed()
    }
}

fun ComposeTestRule.bitacora(steps: BitacoraRobot.() -> Unit) = BitacoraRobot(this).steps()
```

```kotlin
rule.bitacora {
    add("riego", "Alta")
    add("helada", "Baja")
    assertRows(2)
    assertRow("A-2 · Baja · helada")
}
```

El robot no comprueba nada por su cuenta ni guarda estado: solo traduce. Las aserciones van en métodos `assert…` para que se lea qué comprueba el test. Un robot por pantalla, en `src/test`, junto a los tests que lo usan.

¿Cuándo no compensa? Con uno o dos tests por pantalla, el robot es una capa más que leer. En la Bitácora, varias clases de tests escriben observaciones y buscan filas, y ahí sí.

## ⚠️ Errores comunes

| Error | Qué pasa | Solución |
| --- | --- | --- |
| `onNode` con un matcher que coincide con dos nodos | `Expected exactly '1' node but found '2'` | Afina con `and hasClickAction()` o `hasAnyAncestor(...)` |
| `assertExists` para «se ve» | Pasa con un nodo tapado o fuera de la pantalla | `assertIsDisplayed` |
| Coordenadas fijas en `performTouchInput` | En un dispositivo, con otra densidad, el toque cae en otro sitio | Calcula con `width`, `height` o `center` del nodo |
| `performTextInput` dos veces esperando reemplazar | El texto se añade: «riegoriego» | `performTextReplacement` |
| Un robot con aserciones escondidas en las acciones | El test no dice qué comprueba | Acciones y `assert…` separados |

## 📝 Resumen

- `createComposeRule` para piezas, `createAndroidComposeRule` para una actividad, `createEmptyComposeRule` cuando la abre el test.
- Los matchers se combinan con `and`, `or` y los de posición en el árbol. `onNode` quiere exactamente uno.
- Las acciones semánticas no dependen de coordenadas; los gestos sí.
- `assertIsDisplayed` es lo que ve la persona; `assertExists`, lo que hay en el árbol.
- Un robot traduce el «qué» del test al «cómo» de la pantalla.

## 🔗 Recursos

- [Chuleta de la API de tests de Compose](https://developer.android.com/develop/ui/compose/testing/testing-cheatsheet)
- [Finders, acciones y aserciones](https://developer.android.com/develop/ui/compose/testing/apis)
- [Robot pattern (Jake Wharton)](https://jakewharton.com/testing-robots/)
