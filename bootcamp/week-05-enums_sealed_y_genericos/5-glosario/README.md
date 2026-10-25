# Glosario — Semana 05

| Término | Definición |
| --- | --- |
| **Borrado de tipos (erasure)** | Los parámetros de tipo no existen en ejecución en la JVM |
| **Cota superior** | Restricción de un parámetro de tipo: `T : Comparable<T>` |
| **Covarianza (`out`)** | `Box<String>` es un `Box<Any>`: solo produce `T` |
| **Contravarianza (`in`)** | `Sink<Any>` es un `Sink<String>`: solo consume `T` |
| **data object** | `object` con `toString`, `equals` y `hashCode` legibles; para casos sin datos |
| **entries** | Lista de las constantes de un enum, en orden de declaración |
| **enum class** | Conjunto cerrado de constantes únicas |
| **Exhaustivo** | `when` que trata todos los casos; sin `else` en `enum` y `sealed` |
| **Genérico** | Clase o función que funciona con cualquier tipo, indicado con `<T>` |
| **inline** | El compilador copia el cuerpo de la función en cada llamada |
| **Invariante (tipo)** | Sin relación de subtipo entre `X<A>` y `X<B>` aunque `A` sea subtipo de `B` |
| **Nothing** | Subtipo de todo; el tipo de lo que nunca devuelve |
| **Obsesión por los primitivos** | Usar `String` o `Int` donde corresponde un tipo con significado |
| **ordinal** | Posición de una constante de enum; no es estable para persistir |
| **Proyección estrella (`*`)** | `List<*>`: lista de algo desconocido |
| **reified** | Conserva el tipo `T` en ejecución; solo con `inline` |
| **sealed** | Jerarquía cerrada: todos los subtipos se conocen al compilar |
| **typealias** | Otro nombre para un tipo; no crea un tipo nuevo |
| **value class** | Tipo nuevo que envuelve un único valor, con casi ningún coste |
| **valueOf** | Convierte un nombre en constante de enum; lanza si no existe |
