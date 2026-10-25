# Proyecto — Análisis de la Bitácora

⏱️ 4 h · Capa 2 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

El módulo que **responde preguntas** sobre las observaciones: ¿cuántas hay en cada nivel?, ¿cuáles son las más altas?, ¿cuánto suma cada código?, ¿cuáles mencionan "plaga"? Todo con funciones de colección, sin `var` acumuladores.

## Dónde trabajas

En **tu repositorio**, rama `week-03`. El `starter/` trae una base mínima (`Base.kt`: `Observation`, `header` y `level`) para que compile solo; **usa tus versiones de las semanas 1 y 2** y tus nombres del dominio. Copia `Analysis.kt`, `AnalysisTest.kt` y `Main.kt` y adáptalos.

## Pasos

1. **`byLevel`**: `groupBy` con `level(it.measure)`.
2. **`countByLevel`**: `associateWith` sobre `listOf("LOW", "MEDIUM", "HIGH")` para que **siempre** estén las tres claves, aunque valgan `0`.
3. **`top`**: `sortedWith(compareByDescending<Observation> { it.measure }.thenBy { it.code })` y `take(n)`. `n` negativo: `require`.
4. **`quantityByCode`**: `groupBy` + `mapValues { ... sumOf ... }`.
5. **`search`**: `contains(text, ignoreCase = true)`. Un texto en blanco devuelve lista vacía (no "todo").
6. **`statistics`**: devuelve `null` con la lista vacía; si no, `Stats(count, min, max, average)` con `minOf`, `maxOf` y `sumOf` (aquí `minOf`/`maxOf` son seguros porque ya comprobaste que no está vacía).
7. **`distributionReport`**: cabecera, una línea por nivel en orden `LOW`, `MEDIUM`, `HIGH`, la línea `Top: ...` y la línea de media y máximo con `Locale.ROOT`. Revisa el formato exacto en el comentario de la función.
8. **Escribe al menos 7 tests** en `AnalysisTest.kt`, calculados **a mano** antes de ejecutar. Obligatorios: `countByLevel` con una lista vacía, un empate en `top`, `quantityByCode` con un código repetido, `search` sin mayúsculas y con texto en blanco, `statistics` con la lista vacía y con datos, y el texto exacto de `distributionReport` en los dos casos.
9. **Cambia `Main.kt`** por datos de tu dominio (con algún código repetido) y ejecuta `./gradlew run`.
10. **Abre el pull request** `week-03` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, sin `first()`/`maxOf`/`!!` sobre listas que pueden estar vacías, tests propios con valores calculados a mano, ninguna función muta su entrada.

## Pistas

- Los comparadores se componen: `compareByDescending<Observation> { it.measure }.thenBy { it.code }`.
- `list.sumOf { it.measure }` suma `Double`s; `sumOf { it.quantity }`, `Int`s: la lambda decide el tipo.
- Para unir los códigos: `.joinToString(", ") { it.code }`.
- Si el informe sale con coma decimal, falta `Locale.ROOT`.
