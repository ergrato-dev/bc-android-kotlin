# Semana 23 — Material 3 y tema

> En la semana 22 la pantalla de la Bitácora aprendió a repartir el espacio, pero seguía pintando con el morado de ejemplo de Material, con una barra de acción de la plataforma que repetía el título, y de noche seguía siendo blanca. Esta semana la Bitácora tiene tema: los roles de color de Material 3 y un esquema entero de tu marca, el modo oscuro en Compose y en la ventana, el color dinámico de Android 12 con su comprobación de versión, la escala de tipos y las formas. Y el formulario avisa antes de guardar: un campo que marca el error, un teclado con su acción, una elección que se ve como una sola elección y botones que no se pueden pulsar mientras falte algo. Todo comprobado en la JVM, también los colores, píxel a píxel.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** los roles de color de Material 3 y por qué un esquema se define entero
- **Aplicar** el modo oscuro en el esquema, en la raíz de la pantalla y en la ventana, y dar a los colores propios su versión de noche
- **Usar** el color dinámico desde Android 12 sin romper la API 26, y decidir qué colores no deben seguirlo
- **Elegir** el estilo de cada texto por su papel, y hacer pantallas que aguanten la letra grande
- **Construir** un formulario con `OutlinedTextField`, error, teclado y botones segmentados, con la regla en un solo sitio
- **Probar** en la JVM colores con `GraphicsMode.NATIVE`, estilos con `GetTextLayoutResult` y el formulario por su semántica

---

## 📋 Prerrequisitos

- [Semana 22](../week-22-layouts_y_listas/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los calificadores de recursos de la [semana 17](../week-17-recursos_y_configuraciones/README.md) y las reglas de test de Compose de la semana 21.

---

## 🗂️ Estructura de la Semana

```
week-23-material_3_y_tema/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el tema, y el formulario de una observación
├── 3-proyecto/                  # la capa 22 de tu Bitácora: el tema y el formulario
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · MaterialTheme y los roles de color](1-teoria/01-materialtheme-y-los-roles-de-color.md) | Los roles por parejas, cómo leen el tema los componentes y un `Text`, el esquema entero y cómo probar colores | 45 min |
| [02 · Modo oscuro](1-teoria/02-modo-oscuro.md) | `isSystemInDarkTheme`, el `Surface`, el tema de la ventana y los colores propios con `values-night` | 45 min |
| [03 · Color dinámico](1-teoria/03-color-dinamico.md) | El esquema del fondo de pantalla, el error `NewApi`, qué da Robolectric y qué colores no deben cambiar | 40 min |
| [04 · Tipografía y formas](1-teoria/04-tipografia-y-formas.md) | La escala de tipos, la letra grande del sistema, comprobar un estilo en un test y las formas | 45 min |
| [05 · El formulario](1-teoria/05-el-formulario.md) | `OutlinedTextField`, el teclado, botones segmentados o chips, botones apagados y dónde va la regla | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El tema de la Bitácora](2-practicas/ejercicio-01-el-tema-de-la-bitacora/README.md) | La marca o el color dinámico, claro u oscuro, el `Surface`, la ventana y los niveles de noche, comprobados píxel a píxel | 2 h |
| [02 · El formulario de una observación](2-practicas/ejercicio-02-el-formulario-de-una-observacion/README.md) | Un campo con cuenta y error, el teclado con "Hecho", el nivel con botones segmentados y "Guardar" solo cuando se puede | 2 h |

### Proyecto

La Bitácora con el tema de tu marca, el color dinámico, el modo oscuro en la pantalla y en la ventana (sin la barra de acción de la plataforma), los niveles con sus colores de día y de noche, la escala de tipos y un formulario que no deja añadir una descripción vacía o demasiado larga.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 40 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 50 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Pintar de verdad en los tests | `@GraphicsMode(GraphicsMode.Mode.NATIVE)` en la clase, y `captureToImage().toPixelMap()[x, y]` |
| Probar de noche y en español | `@Config(qualifiers = "es-night")` |
| Probar con la letra grande | `@Config(fontScale = 2f)` |
| Ver el estilo de un texto | `performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(list) }` y `layoutInput.style` |
| Ver la semántica de un campo (error, acción del teclado) | `rule.onRoot().printToLog("arbol")` |
| Generar el esquema de tu marca | [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/), exportado a Jetpack Compose |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-23` con el tema de tu marca, el color dinámico, la ventana de día y de noche, los niveles en recursos, el formulario que avisa, los tests de la semana 22 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-23` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests empiezan en rojo, salvo en el proyecto los de la semana 22 y dos de los nuevos, que empiezan en verde. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 22](../week-22-layouts_y_listas/README.md) | **Semana 23** | Semana 24 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
