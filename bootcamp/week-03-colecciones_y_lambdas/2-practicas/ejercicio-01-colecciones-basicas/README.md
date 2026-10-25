# Ejercicio 01 — Colecciones básicas

⏱️ 2 h · Teoría previa: 01 y 03

## Objetivo

Elegir la colección correcta, acceder sin excepciones y no mutar lo que te pasan.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`sinDuplicados`**: una sola función de colección. ¿Conserva el orden?
2. **`frecuencias`**: pasa a minúsculas, deja solo las letras y cuenta con `groupingBy { it }.eachCount()`.
3. **`masLarga`**: `maxByOrNull { it.length }`. Antes de ejecutar, predice qué devuelve con empate y con lista vacía.
4. **`interseccion` y `soloEnPrimero`**: operadores de conjuntos (`intersect`, `-`).
5. **`ultimos`**: `require(n >= 0)` y `takeLast`. Comprueba qué pasa si `n` es mayor que el tamaño.
6. **`cantidadDe` y `agregar`**: `getOrElse` y el operador `+` con un `Pair`. El mapa original **no** debe cambiar (el test lo comprueba).
7. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference: intersect` | Es una función infija: `a intersect b`; también vale `a.intersect(b)` |
| `frecuencias` cuenta espacios | Falta `filter { it.isLetter() }` |
| `agregar` modifica el original | Estás usando un `MutableMap`; devuelve uno nuevo con `+` |
| `Type mismatch: Int? but Int` | `mapa[clave]` es `Int?`; usa `?:` o `getOrElse` |

## Entrega

Los 6 tests en verde. No hace falta `NOTAS.md`.
