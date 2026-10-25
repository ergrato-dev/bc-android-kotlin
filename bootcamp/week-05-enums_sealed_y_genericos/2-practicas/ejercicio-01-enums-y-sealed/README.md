# Ejercicio 01 — Enums y sealed

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Reemplazar `String`s y banderas por tipos que el compilador verifica, y escribir `when` exhaustivos sin `else`.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`Weekday`**: `isWeekend` y `next()` con `entries` y `ordinal`. `next()` de `SUNDAY` debe dar `MONDAY`.
2. **`Priority.parse`**: busca por nombre ignorando mayúsculas y devuelve `null` si no existe. Comprueba en el test qué haría `valueOf`.
3. **`countByPriority` y `urgency`**: `Priority.entries.associateWith` para tener siempre las tres claves, y un `when` **sin `else`**. Prueba a añadir `CRITICAL(20)` al enum: ¿qué deja de compilar? Quítalo después.
4. **`area`**: un `when` exhaustivo sobre `Shape`. Fíjate en que `Empty` no necesita `is`.
5. **`render`**: un caso por cada `LoadState`. Un `Success` con lista vacía es "Sin datos".
6. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `'when' expression must be exhaustive` | Falta una rama; no pongas `else`, añade la rama |
| `Unresolved reference: radius` | Falta el smart cast: `is Shape.Circle ->` |
| `Priority.parse("HIGH")` da `null` | Compara `it.name.equals(text, ignoreCase = true)` |
| El mapa no tiene todas las claves | Usa `Priority.entries.associateWith`, no `groupingBy` |
| `entries` no se encuentra dentro del enum | Es `Weekday.entries` o `entries` dentro del enum; si falla, revisa la versión de Kotlin (2.4.21) |

## Entrega

Los 6 tests en verde. No hace falta `NOTAS.md`.
