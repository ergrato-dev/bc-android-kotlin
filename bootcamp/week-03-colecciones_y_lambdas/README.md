# Semana 03 — Colecciones y lambdas

> Con funciones y control de flujo ya puedes procesar **un** dato. Esta semana procesas **miles**: listas, conjuntos y mapas, y el vocabulario (`map`, `filter`, `groupBy`...) que convierte un bucle de diez líneas en una frase. Tu Bitácora deja de mirar observaciones sueltas y empieza a **analizarlas**.

Todavía no hay clases propias con comportamiento, ni coroutines. Sí aparecen las lambdas, porque sin ellas las colecciones no se entienden; son también la base de Jetpack Compose.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Elegir** entre `List`, `Set` y `Map` y **distinguir** solo lectura de mutable
- **Acceder** a elementos sin excepciones (`getOrNull`, `?: valor`)
- **Escribir** lambdas, tipos de función y funciones que reciben funciones
- **Transformar** datos con `map`, `filter`, `fold`, `sortedWith`, `any`, `find`...
- **Agrupar y contar** con `groupBy`, `groupingBy` y `associateWith`
- **Saber** qué función lanza excepción en una colección vacía y cuál devuelve `null`
- **Explicar** qué es una `Sequence` y cuándo merece la pena
- **Usar** `?.let { }` y `apply` con criterio

---

## 📋 Prerrequisitos

- [Semana 02](../week-02-control_funciones_y_errores/README.md) completada: `when`, bucles, funciones con valores por defecto, `require` y tests con `assertFailsWith`.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las ramas `week-01` y `week-02` ya integradas en `main`.

---

## 🗂️ Estructura de la Semana

```
week-03-colecciones_y_lambdas/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 2 de tu Bitácora: análisis
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Listas, conjuntos y mapas](1-teoria/01-listas-conjuntos-y-mapas.md) | Las tres colecciones, solo lectura frente a mutable, acceso seguro | 40 min |
| [02 · Lambdas y orden superior](1-teoria/02-lambdas-y-orden-superior.md) | Tipos de función, `it`, lambda final, closures, `::` | 45 min |
| [03 · `map`, `filter`, `fold`](1-teoria/03-map-filter-fold.md) | Vocabulario de transformación, orden y comparadores | 45 min |
| [04 · Agrupar y combinar](1-teoria/04-agrupar-y-combinar.md) | `groupBy`, `associateWith`, `partition`, `Pair` | 40 min |
| [05 · Secuencias y funciones de ámbito](1-teoria/05-secuencias-y-funciones-de-ambito.md) | `Sequence`, `let`, `apply`, `also`, `run` | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Colecciones básicas](2-practicas/ejercicio-01-colecciones-basicas/README.md) | Listas, conjuntos y mapas; acceso seguro; inventario inmutable | 2 h |
| [02 · Lambdas y transformaciones](2-practicas/ejercicio-02-lambdas-y-transformaciones/README.md) | Funciones de orden superior propias, cadenas de `filter`/`map`, `groupBy` | 2 h |

### Proyecto

Añades a tu `core` el módulo de **análisis**: observaciones por nivel, ranking, totales por código, búsqueda, estadísticas y un informe de distribución.
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
| Ver el tipo que devuelve una cadena | En el IDE, Alt+Enter → "Add explicit type", o Ctrl+Shift+P sobre la expresión |
| Probar una cadena de `map`/`filter` | <https://play.kotlinlang.org> con una lista de ejemplo |
| Ver cada paso de un pipeline | Inserta `.also { println(it) }` entre pasos |
| Convertir un `for` en una función de colección | Alt+Enter sobre el `for` ("Replace with...") |
| Un `Map` con orden de inserción | `mapOf` y `mutableMapOf` lo conservan (`LinkedHashMap`) |
| Ejecutar un solo test | `./gradlew test --tests "AnalysisTest.counts"` |
| Ver cuánto tarda cada test | `build/reports/tests/test/index.html` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 6 de 6 en verde
2. ✅ Ejercicio 02: 6 de 6 en verde y `NOTAS.md` con la respuesta del paso 9
3. ✅ Proyecto: rama `week-03` con `Analysis.kt` y al menos 7 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-03` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters empiezan en rojo (`NotImplementedError`): es lo esperado.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 02](../week-02-control_funciones_y_errores/README.md) | **Semana 03** | Semana 04 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
