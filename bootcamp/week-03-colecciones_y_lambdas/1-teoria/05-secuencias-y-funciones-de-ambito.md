# Secuencias y funciones de ámbito

> Dos herramientas para escribir menos y gastar menos: las **secuencias** evitan crear colecciones intermedias, y las **funciones de ámbito** (`let`, `apply`...) te dejan trabajar con un objeto sin repetir su nombre.

## 🎯 Objetivos

- Explicar la evaluación **perezosa** de una `Sequence`
- Saber cuándo conviene `asSequence()` y cuándo no
- Usar `let`, `apply`, `also` y `run` y distinguir qué devuelven

## 1. Secuencias

Una cadena `list.filter{}.map{}.first()` crea una lista completa tras **cada** paso. Una `Sequence` procesa los elementos **uno a uno** de principio a fin, y solo hasta donde hace falta:

```kotlin
val r = sequenceOf(1, 2, 3, 4)
    .filter { println("filter $it"); it % 2 == 0 }
    .map { println("map $it"); it * 10 }
    .first()
// filter 1
// filter 2
// map 2
// r = 20      (el 3 y el 4 ni se miran)
```

- Las operaciones **intermedias** (`filter`, `map`, `take`...) no hacen nada hasta que hay una operación **terminal** (`first`, `toList`, `sum`, `count`...).
- Se crea con `asSequence()` a partir de una colección, o con `generateSequence`:

```kotlin
generateSequence(1) { it * 2 }.take(5).toList()     // [1, 2, 4, 8, 16]
```

**¿Cuándo?** Con listas pequeñas, las colecciones normales son igual de rápidas y más sencillas. Una secuencia compensa con colecciones grandes y varios pasos, o cuando solo necesitas los primeros resultados (`first`, `take`). Mide antes de cambiar: no es una mejora automática.

## 2. Funciones de ámbito

Cinco funciones que ejecutan un bloque sobre un objeto. Se diferencian en **cómo se llama al objeto dentro** y **qué devuelven**:

| Función | Objeto dentro | Devuelve | Se usa para |
| --- | --- | --- | --- |
| `let` | `it` | el resultado de la lambda | Transformar o ejecutar algo si no es `null` |
| `run` | `this` | el resultado de la lambda | Calcular un resultado con varias llamadas al objeto |
| `with(obj)` | `this` | el resultado de la lambda | Lo mismo que `run`, pasando el objeto como argumento |
| `apply` | `this` | el propio objeto | Configurar un objeto al crearlo |
| `also` | `it` | el propio objeto | Efectos secundarios: registrar, depurar |

```kotlin
// let con ?. : ejecuta solo si no es null
val longitud = texto?.let { it.trim().length } ?: 0

// apply: configura y devuelve el objeto
val lista = mutableListOf<Int>().apply {
    add(1)
    add(2)
}

// also: mira un valor sin alterar la cadena
val n = calcular().also { println("calculado: $it") }
```

Dentro de `apply`, `run` y `with`, el objeto es `this` y se omite al llamar a sus miembros (`add(1)` es `this.add(1)`). Este es el mecanismo que usan los DSL: lo verás en las semanas 8 y 21.

### Cuándo no

Encadenar tres funciones de ámbito anidadas hace el código ilegible porque `it` y `this` cambian de significado. Una regla útil: **una por expresión** y solo cuando ahorre una variable temporal. `?.let { }` para no-nulos y `apply` para configurar cubren el 90 %.

## 3. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| La secuencia "no hace nada" | Falta una operación terminal | `toList()`, `first()`, `sum()` |
| `Sequence` más lenta que la lista | Colección pequeña o pasos triviales | Vuelve a la lista |
| Reutilizar una secuencia | Algunas solo se pueden recorrer una vez | Vuelve a crearla |
| `let` devuelve algo inesperado | Devuelve el resultado, no el objeto | Para devolver el objeto: `also` o `apply` |
| `it` confuso con lambdas anidadas | Dos `it` en juego | Nombra los parámetros |

## ✅ Qué debes saber al terminar

- Las secuencias son perezosas: nada ocurre hasta la operación terminal.
- `let`/`run`/`with` devuelven el resultado; `apply`/`also` devuelven el objeto.
- `?.let { }` y `apply` como casos de uso habituales.
