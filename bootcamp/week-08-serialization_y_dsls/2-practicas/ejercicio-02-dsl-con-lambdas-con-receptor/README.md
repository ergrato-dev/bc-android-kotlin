# Ejercicio 02 — DSL con lambdas con receptor

⏱️ 2 h · Teoría previa: 03 (sección del árbol) y 04

## Objetivo

Construir DSLs pequeños: un *builder* con validación, un constructor anidado, un filtro, un informe con `+"texto"`, operadores sobre una clase y un JSON con `buildJsonObject`.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt` (`Model.kt` ya está hecho).

1. **`observation`**: un `ObservationBuilder` con valores por defecto y `description` obligatoria (`check` en `build()`).
2. **`logbook`**: un constructor anidado que conserva el orden y rechaza los códigos repetidos. Fíjate en la nota sobre el miembro que se llama a sí mismo.
3. **`filter`**: condiciones combinadas con Y; sin condiciones, pasan todas.
4. **`report`**: `title`, `section` y `+"texto"` con `operator fun String.unaryPlus()`. El formato exacto está en el comentario.
5. **Operadores**: `get`, `contains` y `plus` sobre `Logbook`.
6. **`summaryJson`**: `buildJsonObject`, `putJsonObject` y `putJsonArray`. El orden y el formato son exactos.
7. **`NOTAS.md`**: (a) Quita `@LogbookDsl` de las dos clases y escribe, dentro de un bloque `observation("A") { ... }`, una llamada a `observation("B") { description = "x" }`. ¿Compila? ¿Dónde acaba `B`? (b) Vuelve a poner `@LogbookDsl` y repite: ¿qué dice el compilador? (c) ¿Cómo se llamaría al exterior si de verdad lo quisieras? Después deja el código sin esa llamada.
8. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `description obligatoria` no aparece | `build()` no llama a `check` con el mensaje exacto (`description obligatoria en <code>`) |
| `val cannot be reassigned` o `Type mismatch` en `LogbookBuilder.observation` | Dentro del miembro, `observation(...)` se llama a sí mismo; usa `ObservationBuilder(code).apply(block).build()` |
| `Unresolved reference: description` | La lambda debe ser `ObservationBuilder.() -> Unit` |
| El informe tiene una línea en blanco al principio | La línea en blanco va **antes de cada sección**, no antes del título |
| `+"uno"` no compila | `unaryPlus` tiene que ser **miembro** de `SectionBuilder` (dentro de la clase) |
| `summaryJson` sale con otro orden | `buildJsonObject` respeta el orden de inserción; revisa el orden de los `put` |
| `levels` no tiene los tres niveles | Recorre `Level.entries`, no los niveles que aparecen |

## Entrega

Los 8 tests en verde y `NOTAS.md` con la respuesta del paso 7.
