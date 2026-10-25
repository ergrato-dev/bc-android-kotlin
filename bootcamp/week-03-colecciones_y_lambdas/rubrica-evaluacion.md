# Rúbrica de Evaluación — Semana 03: Colecciones y lambdas

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: análisis de la Bitácora |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué garantiza cada una de `List`, `Set` y `Map`? Da un caso de uso de cada una. | 10 |
| 2 | ¿Qué significa "solo lectura" frente a "mutable", y por qué se devuelve la primera aunque por dentro uses la segunda? | 10 |
| 3 | `lista[10]`, `lista.getOrNull(10)` y `mapa["x"]` sobre colecciones sin ese elemento: ¿qué da cada uno? | 10 |
| 4 | ¿Qué tipo es `(Int, Int) -> Boolean` y cómo escribes una lambda de ese tipo? | 10 |
| 5 | ¿Qué es `it` y cuándo es mejor nombrar el parámetro? | 10 |
| 6 | ¿Qué hace una lambda con las variables de su alrededor ("closure")? Da un ejemplo. | 10 |
| 7 | ¿Qué diferencia hay entre `maxOf` y `maxByOrNull` con una lista vacía? | 10 |
| 8 | ¿Cuál es el resultado de `listOf(1,2,3,4).filter { it > 1 }.map { it * 2 }.sum()`? Desglósalo. | 10 |
| 9 | ¿Qué devuelve `groupBy`? ¿Por qué usarías `associateWith` si quieres que aparezcan claves sin datos? | 10 |
| 10 | ¿Cuándo es útil una `Sequence` y cuándo no? ¿Qué falta para que "haga algo"? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. `List`: orden y posición, admite repetidos (observaciones por orden de llegada). `Set`: sin repetidos (códigos ya vistos). `Map`: clave única que apunta a un valor (cantidad por código).
2. De solo lectura no tiene `add`/`remove`: quien la recibe no puede romperla. Se acumula con una mutable y se devuelve como `List<T>`, para que el contrato exponga lo mínimo.
3. `lista[10]` lanza `IndexOutOfBoundsException`; `getOrNull(10)` da `null`; `mapa["x"]` da `null` (su tipo es `V?`).
4. Una función de dos `Int` que devuelve `Boolean`. Por ejemplo `val mayor: (Int, Int) -> Boolean = { a, b -> a > b }`.
5. `it` es el nombre implícito del único parámetro de una lambda. Para lambdas largas o anidadas es mejor darle un nombre, porque `it` de una lambda interior tapa al de la exterior.
6. La lambda puede leer y modificar las variables locales de la función que la creó, incluso después de que esa función termine. Ejemplo: `fun crearSumador(n: Int): (Int) -> Int = { it + n }` recuerda `n`.
7. `maxOf { }` lanza `NoSuchElementException` con una lista vacía; `maxByOrNull { }` devuelve `null`.
8. `filter` → `[2, 3, 4]`; `map` → `[4, 6, 8]`; `sum` → `18`.
9. Un `Map<K, List<T>>` con solo las claves que tienen elementos. `associateWith` parte de una lista fija de claves y calcula el valor de cada una, así que aparecen también con valor `0` o lista vacía.
10. Útil con colecciones grandes y varias operaciones, o cuando solo necesitas los primeros resultados: procesa elemento a elemento sin colecciones intermedias. No ayuda con listas pequeñas. Hace falta una operación terminal (`toList`, `first`, `sum`).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Colecciones básicas (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 tests en verde | 15 |
| Devuelve tipos de solo lectura y no muta sus entradas (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Lambdas y transformaciones (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 en verde | 14 |
| `NOTAS.md`: paso 9 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Análisis de la Bitácora

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 7 tests propios, calculados a mano; incluyen vacío, empates de `top` y el texto exacto del informe | 25 |
| Colecciones | Usa `groupBy`, `associateWith`, `sortedWith` + `thenBy` y funciones `...OrNull` donde la lista puede estar vacía | 25 |
| Pureza | Ninguna función muta su entrada; devuelven `List`/`Map` de solo lectura | 10 |
| Adaptación al dominio | Datos y búsquedas con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-03`, pull request con salida de `test` y de `run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `first()`, `maxOf`, `reduce` o `!!` sobre una lista que puede estar vacía | -5 cada uno |
| Un `for` con `var` acumulador donde bastaba una función de colección, repetido | -5 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| `String.format` sin `Locale.ROOT` | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-03` → `main` en el repositorio del aprendiz.
