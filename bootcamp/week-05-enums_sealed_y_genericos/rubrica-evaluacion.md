# Rúbrica de Evaluación — Semana 05: Enums, sealed y genéricos

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: modelo tipado de la Bitácora |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué gana un `enum` frente a un `String` para representar un nivel? | 10 |
| 2 | ¿Qué hace `Level.valueOf("X")` si no existe y cómo buscas sin que lance? | 10 |
| 3 | ¿Por qué no se debe guardar el `ordinal` de un enum? | 10 |
| 4 | ¿Qué significa que un `when` sea exhaustivo y qué se pierde si le pones `else`? | 10 |
| 5 | ¿Cuándo eliges `sealed` en lugar de `enum`? Da un ejemplo. | 10 |
| 6 | ¿Qué es un `data object` y cuándo lo usas? | 10 |
| 7 | ¿Qué problema resuelve una `value class` y qué restricciones tiene? | 10 |
| 8 | ¿Por qué `typealias Code = String` no evita mezclar códigos y textos? | 10 |
| 9 | ¿Por qué `List<String>` es una `List<Any>` pero `MutableList<String>` no es una `MutableList<Any>`? | 10 |
| 10 | ¿Para qué sirve `reified` y qué necesita? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Que el compilador sabe cuáles son los valores posibles: no se puede escribir `"HIHG"`, el autocompletado funciona y un `when` puede comprobar que se tratan todos los casos.
2. Lanza `IllegalArgumentException`. Se busca con `Level.entries.firstOrNull { it.name.equals(text, ignoreCase = true) }`, que devuelve `null`.
3. Porque cambia si se reordena o se inserta una constante, y los datos guardados quedarían apuntando a otra cosa. Se guarda el `name` o un código explícito.
4. Que el compilador comprueba que se tratan **todos** los casos. Con `else`, si se añade un caso nuevo ya no se avisa y cae en el comportamiento por defecto sin que nadie lo decida.
5. Cuando cada caso necesita **datos distintos**. Ejemplo: `ParseResult` con `Valid(observation)` e `Invalid(line, reason)`. Un `enum` serviría si todos los casos tuvieran la misma forma.
6. Un `object` (singleton) dentro de una jerarquía, con `toString`, `equals` y `hashCode` legibles. Se usa para casos sin datos: `data object Loading`.
7. La "obsesión por los primitivos": dos `String` se confunden. Una `value class` crea un tipo distinto con validación y casi sin coste. Tiene una sola propiedad `val` en el constructor primario, necesita `@JvmInline` y no puede heredar ni ser heredada.
8. Porque un `typealias` es solo otro **nombre** para el mismo tipo: `Code` y `String` siguen siendo intercambiables.
9. `List` solo devuelve elementos (productor, `out E`), así que es seguro que una lista de textos se use como lista de `Any`. `MutableList` también los recibe: si fuera `MutableList<Any>` podrías meter un `Int` en una lista de textos.
10. Para conservar el tipo `T` en ejecución (por ejemplo `it is T`), que normalmente se borra. Necesita una función `inline`.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Enums y sealed (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 tests en verde | 15 |
| Todos los `when` sobre enum/sealed sin `else` (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Value classes y genéricos (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 14 |
| `NOTAS.md`: paso 7 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Modelo tipado

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 9 tests propios, calculados a mano; cada motivo de rechazo, `countByLevel` con ceros, `topBy` con dos claves | 25 |
| Tipos | `Level` enum, `Code` value class, `ParseResult` sealed; ningún `String` hace de nivel o de código | 25 |
| Genéricos | `topBy` con `T : Comparable<T>` y comportamiento de desempate documentado en un test | 10 |
| Adaptación al dominio | Datos, motivos y mensajes con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-05`, pull request con salida de `test` y de `run`, y la lista de descartadas con su motivo | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `else` en un `when` sobre `enum` o `sealed` | -5 cada uno |
| Se guarda o compara `ordinal` | -5 |
| Vuelve a usar `String` como nivel o código en alguna API pública | -10 |
| `!!`, `catch` vacío o `catch (e: Exception)` | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-05` → `main` en el repositorio del aprendiz.
