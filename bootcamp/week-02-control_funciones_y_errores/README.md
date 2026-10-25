# Semana 02 — Control de flujo, funciones y errores

> Con tipos y `val`/`var` ya sabes guardar datos. Esta semana aprendes a **decidir** (`if`, `when`), a **repetir** (`for`, `while`, rangos), a **empaquetar** (funciones) y a **fallar bien** (`require`, `try`, `null`, `Result`). Es la semana en que tu Bitácora deja de imprimir y empieza a validar.

Todavía no hay clases propias, ni lambdas, ni coroutines. Las listas se usan solo para recorrerlas y acumular; las colecciones de verdad llegan en la semana 3.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Escribir** `if` y `when` como expresiones y **ordenar** sus ramas correctamente
- **Recorrer** rangos con `..`, `until`, `downTo` y `step`, y **elegir** entre `for` y `while`
- **Declarar** funciones con valores por defecto, argumentos con nombre y `vararg`
- **Elegir** entre devolver `null`, lanzar una excepción o devolver un `Result`
- **Validar** argumentos con `require` y **capturar** excepciones con `try`/`catch`
- **Escribir** tests con `assertFailsWith`, probando casos normales, límites y errores
- **Leer** líneas de texto de una fuente externa y **descartar** las inválidas sin romper el programa

---

## 📋 Prerrequisitos

- [Semana 01](../week-01-kotlin_toolchain_y_tipos/README.md) completada: `./gradlew test` funciona en tu máquina y tu repositorio `bc-android-kotlin-<tu-dominio>` ya existe.
- Conoces `val`/`var`, los tipos básicos, `String?` y `String.format(Locale.ROOT, ...)`.

---

## 🗂️ Estructura de la Semana

```
week-02-control_funciones_y_errores/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 1 de tu Bitácora: reglas y validación
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · `if` y `when`](1-teoria/01-if-y-when.md) | Expresión frente a sentencia, `when` con rangos, `is` y sin argumento | 40 min |
| [02 · Bucles y rangos](1-teoria/02-bucles-y-rangos.md) | `for`, `while`, `..`/`until`/`downTo`/`step`, `break`, `Unit`, `Nothing` | 40 min |
| [03 · Funciones](1-teoria/03-funciones.md) | Firma, valores por defecto, argumentos con nombre, `vararg` | 45 min |
| [04 · Errores y excepciones](1-teoria/04-errores-y-excepciones.md) | `throw`, `try`, `require`/`check`, `runCatching`, excepción o `null` | 45 min |
| [05 · Pruebas con `kotlin.test`](1-teoria/05-pruebas-con-kotlin-test.md) | `@Test`, aserciones, `assertFailsWith`, casos límite | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Control de flujo](2-practicas/ejercicio-01-control-de-flujo/README.md) | `when`, rangos, `while`, `FizzBuzz`, primos, `return` dentro de un bucle | 2 h |
| [02 · Funciones y errores](2-practicas/ejercicio-02-funciones-y-errores/README.md) | `require`, `toIntOrNull`, `vararg`, argumentos con nombre, `try`, `runCatching` | 2 h |

### Proyecto

Añades a tu `core` las reglas del dominio: niveles con `when`, validación con `require`, lectura de líneas de texto que pueden venir mal formadas y un resumen que cuenta las descartadas.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h |
| Recursos y glosario | 1 h 30 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Probar una expresión sin proyecto | <https://play.kotlinlang.org> |
| Ver cómo sube un error por las llamadas | Lee la traza de abajo hacia arriba: la última línea de tu código es el origen |
| Ejecutar un solo test | `./gradlew test --tests "MainTest.primos"` |
| Ejecutar los tests al guardar | `./gradlew test --continuous` |
| Ver los resultados en HTML | Abre `build/reports/tests/test/index.html` |
| Convertir un `if` encadenado en `when` | El IDE lo ofrece con Alt+Enter ("Replace 'if' with 'when'") |
| Comprobar un rango rápido | `println((1..10 step 3).toList())` en el Playground |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 6 de 6 en verde
2. ✅ Ejercicio 02: 6 de 6 en verde y `NOTAS.md` con la respuesta del paso 8
3. ✅ Proyecto: rama `week-02` de tu repositorio con `Rules.kt` y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-02` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Recuerda que los starters empiezan en rojo (`NotImplementedError`): es lo esperado.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 01](../week-01-kotlin_toolchain_y_tipos/README.md) | **Semana 02** | [Semana 03](../week-03-colecciones_y_lambdas/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
