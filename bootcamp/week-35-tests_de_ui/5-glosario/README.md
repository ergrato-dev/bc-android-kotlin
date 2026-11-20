# Glosario — Semana 35

| Término | Definición |
| --- | --- |
| **`advanceTimeByFrame`** | Avanza el reloj de Compose un fotograma (16 ms): recompone y lanza los efectos pendientes. Con el reloj parado, es lo que hace que una acción del test tenga efecto en la pantalla |
| **Árbol semántico** | Los nodos que describen la pantalla para la accesibilidad y los tests: texto, papel, estado y acciones. No tiene píxeles ni funciones |
| **Árbol sin unir** | El árbol semántico con cada pieza aparte, sin juntar los descendientes de lo que es `clickable`. Se pide con `useUnmergedTree = true` |
| **ATD** (*Automated Test Device*) | Imagen de emulador sin las apps que un test no necesita: más ligera y rápida de arrancar. `aosp-atd` o `google-atd` |
| **`captureRoboImage`** | La función de Roborazzi que captura un nodo de Compose o una vista. Graba, compara o verifica según la tarea o la propiedad |
| **Captura de referencia** (*golden*) | La imagen guardada en el repositorio con la que se comparan las capturas de cada ejecución |
| **`clearPackageData`** | Argumento del orquestador que borra los datos de la app entre un test instrumentado y el siguiente |
| **Espresso** | La biblioteca de tests de vistas de AndroidX: `onView`, `perform`, `check` |
| **Espresso-Intents** | La parte de Espresso que graba los `Intent` que salen de la app (`intended`) y contesta los que esperan un resultado (`intending`) |
| **GMD** (*Gradle Managed Device*) | Un emulador descrito en el build, que Gradle crea, arranca, usa y apaga para ejecutar los tests instrumentados |
| **`IntentsRule`** | Regla de JUnit que empieza y termina la grabación de Espresso-Intents en cada test |
| **`mainClock`** | El reloj de Compose en un test. Con `autoAdvance = false`, solo avanza cuando el test lo mueve |
| **Orquestador** | Herramienta de AndroidX Test que ejecuta cada test instrumentado en su propio proceso |
| **Robot** (*page object*) | Clase de test que traduce las acciones de una persona («añadir», «compartir») a finders y acciones sobre la pantalla |
| **Roborazzi** | Biblioteca de capturas de pantalla para la JVM, sobre Robolectric |
| **`roborazzi.test.verify`** | Propiedad de Gradle que hace que los tests de la JVM verifiquen las capturas sin llamar a la tarea `verify` |
| **Runner de instrumentación** | La clase que arranca los tests en el dispositivo y crea la `Application`. `HiltTestRunner` crea `HiltTestApplication` |
| **`src/androidTest`** | Las fuentes de los tests instrumentados: se compilan en un APK de test que se instala junto a la app |
| **`testTag`** | Etiqueta de un nodo que solo ven los tests (y UiAutomator, con `testTagsAsResourceId`). Para lo que no tiene texto ni papel |
| **`testTagsAsResourceId`** | Propiedad semántica que expone los `testTag` de los descendientes como `resource-id` de Android |
| **Test de captura** | Test que compara una imagen de la pantalla con una de referencia, píxel a píxel |
| **Test instrumentado** | Test que se ejecuta en un dispositivo o un emulador, con la app instalada |
| **`waitForIdle`** | Espera a que Compose no tenga nada pendiente: recomposiciones, fotogramas y animaciones. No espera a otros hilos |
| **`waitUntil`** | Espera a que se cumpla una condición, repitiendo `waitForIdle`, con un límite de tiempo real |
