# Rúbrica de Evaluación — Semana 08: Serialization y DSLs

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: Bitácora serializable y DSL |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué hace el plugin `kotlin("plugin.serialization")` y qué versión debe llevar? | 10 |
| 2 | ¿Por qué un valor por defecto no aparece en el JSON y cómo se cambia? | 10 |
| 3 | ¿Qué diferencia hay entre `MissingFieldException` y el error de un `init { require }` al leer? | 10 |
| 4 | ¿Por qué se crea una sola instancia de `Json`? | 10 |
| 5 | ¿Cómo se escribe una `value class` en JSON y qué pasa con su `init` al leer? | 10 |
| 6 | ¿Qué es el discriminador de una jerarquía `sealed` y por qué hay que escribir con el tipo base? | 10 |
| 7 | ¿Por qué se decodifica cada observación por separado en una importación tolerante? | 10 |
| 8 | Nombra tres reglas para que un formato de archivo pueda crecer sin romper. | 10 |
| 9 | ¿Qué es una lambda con receptor y qué hace `@DslMarker`? | 10 |
| 10 | ¿Por qué el esperado de un test *golden* se escribe a mano y no se copia de la salida? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Genera en compilación los serializadores de las clases `@Serializable`. Lleva la **misma versión que el compilador de Kotlin** (la biblioteca `kotlinx-serialization` tiene su versión propia, fijada con su BOM).
2. Porque `encodeDefaults` vale `false` por defecto: si el valor es igual al de por defecto no se escribe, y el JSON queda más corto. Con `encodeDefaults = true` siempre aparece (útil para `version`).
3. `MissingFieldException` es un error de **formato**: falta una propiedad obligatoria. El `require` del `init` es de **validación**: la biblioteca lo envuelve en un `SerializationException` y deja el original como `cause`, de donde sacas tu mensaje.
4. Crearla en cada llamada es lento (el compilador lo avisa con `Redundant creation of Json format`). Se configura una vez y se reutiliza.
5. Como su valor suelto (`"riego"`), no como un objeto. Su `init` sigue ejecutándose al leer: un valor inválido falla con tu mensaje como causa.
6. Un campo (`type`) con el nombre del subtipo (`@SerialName`). Si el tipo estático es el subtipo, el compilador ya lo conoce y no lo escribe; con el tipo base (`encodeToString<Event>(e)`), sí.
7. Para que una observación rota no tire todo el archivo: lo bueno entra y lo malo se anota con su posición y su motivo, así el usuario puede corregirlo.
8. Cualquiera de: incluir una `version` desde el principio; dar valor por defecto a cada campo nuevo; `ignoreUnknownKeys = true` al leer; no renombrar sin `@SerialName`; subir la versión ante un cambio incompatible y rechazar una versión mayor.
9. Una lambda de tipo `T.() -> Unit` en la que `this` es un objeto de tipo `T`, de modo que sus miembros se llaman sin prefijo. `@DslMarker` impide que desde un bloque interior se llame, sin decirlo, a los miembros del constructor exterior (un fallo silencioso).
10. Porque si lo copias de la salida, el test solo comprueba que el código es igual a sí mismo: no detecta un formato mal diseñado ni un cambio involuntario. Escrito a mano, fija el contrato.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — JSON con serialization (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 15 |
| Una sola instancia de `Json`, sin `catch` vacío ni `catch (e: Exception)` (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — DSL con lambdas con receptor (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 14 |
| `NOTAS.md`: paso 7 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Bitácora serializable y DSL

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 8 tests propios, esperados escritos a mano; texto exacto, ida y vuelta, tolerancia, versión futura, observaciones rotas con posición y motivo, DSL | 25 |
| Serialización | `Code` como texto suelto, `Level` en minúsculas, una instancia de `Json`, `version` siempre escrita, formato exacto | 25 |
| Importación y errores | `importFile` no lanza; versión comprobada antes de decodificar; motivos exactos; orden de lo válido y lo inválido | 10 |
| Adaptación al dominio | Datos, motivos y mensajes con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-08`, pull request con salida de `test` y de `run`, y una importación con motivos distintos | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `importFile` lanza una excepción hacia fuera | -10 |
| `Json { }` creado dentro de una función | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| El motivo de un rechazo es el `e.message` de la biblioteca | -5 cada uno |
| `@DslMarker` ausente en un constructor | -5 |
| `jsonObject`/`jsonArray`/`jsonPrimitive` sobre una entrada de fuera sin comprobar el tipo | -5 cada uno |
| `!!`, `catch` vacío o `catch (e: Exception)` | -5 cada uno |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-08` → `main` en el repositorio del aprendiz.
