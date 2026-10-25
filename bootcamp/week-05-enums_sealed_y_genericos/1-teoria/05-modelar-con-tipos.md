# Modelar con tipos

> La mejor validación es la que no hace falta: si un estado imposible **no se puede escribir**, no hay que comprobarlo, ni probarlo, ni depurarlo.

## 🎯 Objetivos

- Aplicar "haz irrepresentables los estados ilegales"
- Reemplazar `String`s, banderas y `null`s ambiguos por tipos
- Combinar `enum`, `sealed`, `value class` y genéricos en un mismo modelo
- Refactorizar sin romper los tests

## 1. De lo suelto a lo tipado

| Antes | Problema | Después |
| --- | --- | --- |
| `level: String` | `"HIHG"` compila | `enum class Level` |
| `code: String` | Se confunde con otros `String` | `value class Code` |
| `Observation?` tras leer | No sabes por qué falló | `sealed interface ParseResult` |
| `ok: Boolean` + `error: String?` | `ok = true` con error a la vez | `Valid` / `Invalid` |
| `Map<String, Int>` | Claves libres | `Map<Level, Int>` |
| `Pair<List, Int>` | `first` y `second` no significan nada | `data class ImportResult(...)` |

## 2. El método

1. **Escribe en una frase** qué puede ser cada dato: "un nivel es uno de LOW, MEDIUM o HIGH".
2. **Elige el tipo** que solo admite eso: conjunto fijo → `enum`; casos con datos → `sealed`; un valor con reglas → `value class`.
3. **Mueve la validación al tipo**: `init` en la `value class`, `Level.of` en el companion.
4. **Deja que el compilador te guíe**: cambia el tipo y arregla cada error de compilación. Cada uno es un sitio donde había una suposición escondida.
5. **Actualiza los tests**: ahora `assertEquals(Level.HIGH, ...)` en lugar de `"HIGH"`.

## 3. Un refactor concreto

Antes (semanas 1 a 4):

```kotlin
fun level(measure: Double): String = ...            // "LOW", "MEDIUM", "HIGH"
fun parseObservation(line: String): Observation?    // null si falla; ¿por qué?
```

Después:

```kotlin
enum class Level { LOW, MEDIUM, HIGH; companion object { fun of(measure: Double): Level = ... } }
fun parseLine(line: String): ParseResult            // Valid o Invalid(line, reason)
```

`Logbook.fromLines` ya no tira lo inválido: devuelve un `ImportResult` con la bitácora **y** la lista de descartadas con su motivo. El usuario puede **corregirlas** en vez de adivinar.

## 4. Cuándo no sobre-modelar

| Señal | Qué hacer |
| --- | --- |
| Un tipo nuevo para un valor que vive en una sola función | No: una variable local basta |
| Una `sealed` con un solo caso | Una `data class` |
| Genéricos con tres parámetros de tipo para dos usos | Escribe las dos versiones concretas |
| Una jerarquía con más niveles de herencia que casos reales | Aplánala |

Los tipos están para **reducir** el número de estados posibles, no para aumentar el número de archivos.

## 5. Probar un modelo tipado

| Qué probar | Cómo |
| --- | --- |
| Que el tipo rechaza lo inválido | `assertFailsWith` en el constructor o la fábrica |
| Cada rama de un `sealed` | Un caso de entrada por rama; comprueba el tipo y los datos |
| Los motivos de rechazo | `assertEquals("cantidad no numérica: 'dos'", invalid.reason)` |
| Que un `enum` conserva todos los casos | `Level.entries` y las claves de un mapa |
| Genéricos con dos tipos distintos | Mismo test con `Int` y con `String` |

El compilador ya garantiza lo que no puede compilar; los tests cubren lo que **sí** compila pero puede estar mal.

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| Un `enum` con 15 constantes que son realmente datos | Conjunto abierto | `data class` o una tabla |
| `else` en cada `when` | Se pierde la ayuda del compilador | Quítalo en `enum`/`sealed` |
| Value class por todas partes | Exceso | Solo con reglas o riesgo de confusión |
| Refactor sin tests | Cambias tipos y no sabes si algo se rompió | Tests antes, como red |
| Mensajes de error inventados cada vez | Texto disperso | Una función que los centraliza |

## ✅ Qué debes saber al terminar

- Convertir `String`s, banderas y `null`s en tipos.
- Elegir `enum`, `sealed` o `value class` según el caso.
- Dejar que los errores de compilación guíen un refactor.
- No sobre-modelar.
