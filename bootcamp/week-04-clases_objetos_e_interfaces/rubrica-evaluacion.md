# Rúbrica de Evaluación — Semana 04: Clases, objetos e interfaces

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: `Observation` y `Logbook` |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre un parámetro del constructor con `val` y uno sin él? | 10 |
| 2 | ¿Para qué sirve `field` en un `set` y qué pasa si lo omites? | 10 |
| 3 | ¿Cuándo usarías una propiedad calculada en lugar de una que guarda su valor? | 10 |
| 4 | ¿Por qué la propiedad de respaldo `_songs` y `songs = _songs.toList()`? ¿Qué pasaría con `val songs = _songs`? | 10 |
| 5 | ¿Qué es una invariante? Da una de `Observation` y dónde la haces cumplir. | 10 |
| 6 | ¿Qué propiedades entran en el `equals` de una `data class`? ¿Y una definida en el cuerpo? | 10 |
| 7 | ¿Qué diferencia hay entre `==` y `===`? | 10 |
| 8 | ¿Por qué las clases de Kotlin son `final` por defecto y qué palabras permiten heredar y sobrescribir? | 10 |
| 9 | ¿Cuándo eliges interfaz, clase abstracta o composición? | 10 |
| 10 | ¿Qué ventaja tiene una fábrica `Version.parse` en el `companion object` frente a un constructor? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Con `val`/`var` el parámetro **crea una propiedad** de la clase. Sin ellos solo existe como parámetro y puede usarse en `init` y en los inicializadores de propiedades, pero no en los métodos.
2. `field` es el campo real que guarda el valor. Sin él, `set(value) { celsius = value }` se llamaría a sí mismo sin fin (`StackOverflowError`).
3. Cuando el valor **se deriva** de otros datos (`area` de `width` y `height`): no se guarda, así que nunca se desincroniza.
4. `_songs` es la lista mutable privada; `songs` es la vista pública. `toList()` devuelve una **copia**, así que quien la recibe no puede cambiar el estado ni ve cambios posteriores. Con `val songs = _songs` se expone la misma lista (se puede convertir a mutable y se ven cambios a mitad de un recorrido).
5. Una condición que siempre debe cumplirse. En `Observation`: el código no está en blanco. Se cumple en `init` con `require`, de modo que un objeto inválido no llega a existir.
6. Solo las propiedades del **constructor primario**. Lo definido en el cuerpo no cuenta para `equals`, `hashCode`, `toString` ni `copy`.
7. `==` compara por valor (llama a `equals`); `===` compara identidad (si son el mismo objeto en memoria).
8. Porque heredar acopla las clases y debe ser una decisión consciente. `open` permite heredar una clase o sobrescribir un miembro; `abstract` obliga a implementarlo; `override` sustituye lo heredado.
9. Interfaz: distintas clases "saben hacer lo mismo". Clase abstracta: relación "es un" con estado y lógica comunes. Composición (y `by`): reutilizar comportamiento de otro objeto sin atarse a su implementación; es la opción por defecto.
10. Puede devolver `null` (o fallar con un mensaje claro) cuando la entrada no es válida, y su nombre dice lo que hace. Un constructor siempre devuelve un objeto o lanza.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Clases y propiedades (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 tests en verde | 15 |
| Usa `private set`, `field` y propiedades calculadas donde corresponde; no copia valores derivados (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Interfaces y herencia (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 14 |
| `NOTAS.md`: paso 8 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — `Observation` y `Logbook`

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 8 tests propios, calculados a mano; una invariante por test, `assertFailsWith`, `report` exacto | 25 |
| Encapsulación | Lista mutable privada, copia hacia fuera, ninguna forma de dejar el `Logbook` en un estado inválido | 20 |
| Invariantes | Todas las reglas de `Observation` en `init` con mensajes que dicen qué llegó; sin validar fuera del objeto | 15 |
| Adaptación al dominio | Nombres, datos y descripciones con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-04`, pull request con salida de `test` y de `run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Se expone la lista mutable | -15 |
| Validación duplicada fuera del `init` | -5 |
| `!!`, `lateinit` sin necesidad o `catch` vacío | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| `String.format` sin `Locale.ROOT` | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-04` → `main` en el repositorio del aprendiz.
