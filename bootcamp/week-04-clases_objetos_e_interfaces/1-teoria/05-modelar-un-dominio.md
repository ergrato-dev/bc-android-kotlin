# Modelar un dominio

> Programar es decidir **qué cosas existen y qué reglas cumplen**. Si el modelo está bien, el código se escribe casi solo; si está mal, cada función lucha contra él.

## 🎯 Objetivos

- Pasar de una descripción en lenguaje natural a clases con responsabilidades
- Decidir qué es un dato (`data class`) y qué es un servicio con estado (`class`)
- Escribir las reglas del dominio como invariantes y probarlas
- Organizar archivos y visibilidad

## 1. De la descripción a las clases

Una descripción del dominio de la Bitácora:

> "Un inspector registra **observaciones**. Cada una tiene un código, una descripción, una cantidad y una medida. La **bitácora** reúne las observaciones de un dominio, evita duplicados y sabe contar cuántas hay en cada nivel y escribir un informe."

Subraya los sustantivos y los verbos:

| Sustantivo | ¿Qué es? | Tipo |
| --- | --- | --- |
| observación | Un dato con reglas propias | `data class Observation` (valida en `init`) |
| bitácora | Algo que **guarda** observaciones y cambia | `class Logbook` (estado privado) |
| nivel | Una etiqueta derivada de la medida | Propiedad de extensión (`level`) |
| informe | Un texto que se genera | Método `report()` de una interfaz `Reportable` |

| Verbo | Dónde vive |
| --- | --- |
| registrar, quitar, contar | Métodos de `Logbook` |
| validar | `init` de `Observation` |
| leer una línea | Fábrica `Observation.parse` en el companion |

## 2. ¿Dato o servicio?

| Pregunta | Si sí |
| --- | --- |
| ¿Se identifica por sus valores y no cambia? | `data class` con `val` |
| ¿Tiene una vida: se crea, cambia, se consulta? | `class` con estado privado y métodos |
| ¿Solo agrupa funciones sin estado? | Funciones de nivel superior, o un `object` |

El `Observation` es un valor: dos con los mismos datos son la misma observación. El `Logbook` tiene identidad y cambia: dos bitácoras con las mismas observaciones siguen siendo bitácoras distintas.

## 3. Reglas = invariantes

Para cada clase, completa: **"Un X es válido si ..."**.

- Una `Observation` es válida si su código y descripción no están en blanco, su cantidad no es negativa y su medida es un número no negativo.
- Una `Logbook` es válida si no contiene dos observaciones iguales.

La primera va en `init`. La segunda, en `add`. Después, **un test por regla**, intentando romperla:

```kotlin
@Test
fun aBlankCodeIsRejected() {
    assertFailsWith<IllegalArgumentException> { Observation("", "x", 1, 1.0) }
}
```

Con las reglas dentro del objeto, el resto del programa **no necesita volver a validar**: si tienes una `Observation`, es válida. Eso simplifica todo lo que viene después.

## 4. Organizar el código

```
src/main/kotlin/
  Observation.kt     # data class + companion + extensión level
  Logbook.kt         # la clase con estado
  Reportable.kt      # la interfaz
  Base.kt            # funciones sueltas: level, header, format2
  Main.kt            # el punto de entrada
```

- Un archivo por concepto, con el nombre de la clase principal. Varias declaraciones pequeñas relacionadas pueden compartir archivo.
- `private` a nivel de archivo sirve para helpers que solo usa ese archivo.
- `internal` se usará cuando haya varios módulos (fase 2).

## 5. Probar clases

| Qué probar | Cómo |
| --- | --- |
| Cada invariante | `assertFailsWith` con el dato que la rompe |
| Cada operación | Estado antes, operación, estado después |
| Que el estado interno no se escapa | Pide una lista, cambia el objeto y comprueba que la lista no cambió |
| Igualdad y orden | `assertEquals` entre dos objetos construidos por separado; `sorted()` |
| Fábricas | Entrada válida y cada tipo de entrada inválida |

Un test no debería depender de otro: cada uno construye su propio objeto.

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| Validar en la función que usa el objeto | La regla pertenece al objeto | Muévela a `init` |
| Una clase que lo hace todo | Sin separar datos y estado | Divide por responsabilidades |
| `data class` con lógica de negocio pesada | Mezcla de dato y servicio | Servicio aparte |
| Exponer la lista mutable | Comodidad | Propiedad de respaldo + copia |
| Tests que comprueban la implementación | Atados a campos privados | Prueba el comportamiento visible |

## ✅ Qué debes saber al terminar

- Sustantivos → clases; verbos → métodos.
- Dato (`data class`) frente a servicio con estado (`class`).
- "Un X es válido si ..." → `init` y un test por regla.
- Probar comportamiento visible, no detalles internos.
