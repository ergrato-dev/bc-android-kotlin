# Semana 05 — Enums, sealed y genéricos

> Hasta aquí, un nivel era un `String`, un código era un `String` y una lectura fallida era un `null` sin explicación. Esta semana conviertes cada uno en un **tipo**: el compilador deja de ser un espectador y empieza a revisar tu modelo. Es la semana que cierra el primer bloque del bootcamp.

Todavía no hay coroutines ni `Flow` (semanas 6 y 7). Sí aparecen `inline` y `reified`, porque sin ellos los genéricos se quedan a medias.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Declarar** un `enum` con propiedades, un `companion object` y un `when` exhaustivo
- **Modelar** resultados y estados con `sealed interface`, `data class` y `data object`
- **Decidir** entre `enum`, `sealed` y herencia abierta
- **Crear** una `value class` con validación y explicar qué garantiza
- **Escribir** clases y funciones genéricas con restricciones (`T : Comparable<T>`)
- **Explicar** `out` e `in` y por qué `MutableList` es invariante
- **Usar** `inline` y `reified` para preguntar por el tipo en ejecución
- **Refactorizar** `String`s, banderas y `null`s hacia tipos, apoyándote en el compilador y en los tests

---

## 📋 Prerrequisitos

- [Semana 04](../week-04-clases_objetos_e_interfaces/README.md) completada: clases, `init`, interfaces, `companion object`, `Comparable`.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 4 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-05-enums_sealed_y_genericos/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 4 de tu Bitácora: modelo tipado
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Enums](1-teoria/01-enums.md) | `entries`, `valueOf`, propiedades, `when` exhaustivo | 35 min |
| [02 · Sealed](1-teoria/02-sealed.md) | Jerarquías cerradas, `data object`, estados y resultados | 45 min |
| [03 · Value classes](1-teoria/03-value-classes.md) | Tipos que no se confunden, `@JvmInline`, `typealias` | 40 min |
| [04 · Genéricos](1-teoria/04-genericos.md) | Parámetros de tipo, cotas, `out`/`in`, `reified` | 50 min |
| [05 · Modelar con tipos](1-teoria/05-modelar-con-tipos.md) | Estados irrepresentables, método de refactor | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Enums y sealed](2-practicas/ejercicio-01-enums-y-sealed/README.md) | `Weekday`, `Priority`, `Shape` y `LoadState` con `when` exhaustivo | 2 h |
| [02 · Value classes y genéricos](2-practicas/ejercicio-02-value-classes-y-genericos/README.md) | `Meters`, `Email`, `Stack<T>`, `clamp`, `Outcome<T>`, `reified` | 2 h |

### Proyecto

La Bitácora se vuelve un modelo tipado: `Level` es un `enum`, `Code` es una `value class`, `ParseResult` explica por qué se descarta cada línea y `topBy` es una función genérica.
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
| Añadir las ramas que faltan a un `when` | Alt+Enter sobre el `when` → "Add remaining branches" |
| Ver qué casos tiene una jerarquía sellada | Ctrl+H sobre el nombre |
| Renombrar `String` por un tipo en todo el proyecto | Cambia el tipo y sigue los errores del compilador (F2 salta al siguiente) |
| Ver el tipo inferido de una expresión genérica | Ctrl+Shift+P sobre la expresión |
| Ver el bytecode de una `value class` | Tools → Kotlin → Show Kotlin Bytecode |
| Ejecutar un solo test | `./gradlew test --tests "LogbookTest.reasons"` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 6 de 6 en verde
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con la respuesta del paso 7
3. ✅ Proyecto: rama `week-05` con `Code.kt`, `Level.kt`, `ParseResult.kt`, `Logbook.kt` y al menos 9 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-05` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters empiezan en rojo (`NotImplementedError`): es lo esperado.

---

## 🏁 Cierre del primer bloque (semanas 1 a 5)

Has terminado la base del lenguaje. Antes de seguir, comprueba que dominas esto **sin mirar**:

- [ ] Compilar y probar con `./gradlew build` y leer un `build.gradle.kts` mínimo (S1)
- [ ] Escribir una función con `when`, rangos, `require` y `null` seguro (S2)
- [ ] Transformar y agrupar una colección con `map`, `filter` y `groupBy` (S3)
- [ ] Diseñar una clase que no puede quedar en un estado inválido (S4)
- [ ] Reemplazar un `String` o un `null` ambiguo por un `enum`, una `sealed` o una `value class` (S5)

Tu repositorio debe tener ya el `core` de la Bitácora con modelo tipado, lectura con motivos de descarte, análisis y un informe, todo con tests propios. Las semanas 6 a 8 lo harán **asíncrono**, **reactivo** y **serializable**.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 04](../week-04-clases_objetos_e_interfaces/README.md) | **Semana 05** | Semana 06 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
