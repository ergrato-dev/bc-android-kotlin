# Enums

> Cuando un dato solo puede valer una de unas pocas opciones conocidas, un `String` o un `Int` es una invitación al error. Un `enum` le dice al compilador cuáles son.

## 🎯 Objetivos

- Declarar un `enum` con propiedades, métodos y un `companion object`
- Usar `entries`, `name`, `ordinal` y `valueOf`
- Escribir un `when` exhaustivo sobre un enum
- Elegir entre enum y otras opciones

## 1. Declarar un enum

```kotlin
enum class Level {
    LOW, MEDIUM, HIGH
}

val l = Level.HIGH
l.name        // "HIGH"
l.ordinal     // 2: posición, empezando en 0
```

Cada constante es **un objeto único**: no se crean más. Comparar con `==` (o con `when`) es seguro y rápido.

## 2. Propiedades y métodos

```kotlin
enum class Priority(val weight: Int) {
    LOW(1), MEDIUM(5), HIGH(10);            // el ; separa las constantes de los miembros

    val isUrgent: Boolean get() = this == HIGH

    companion object {
        fun parse(text: String): Priority? = entries.firstOrNull { it.name.equals(text, ignoreCase = true) }
    }
}
```

- Los argumentos del constructor van **en cada constante**.
- Un enum puede implementar interfaces y declarar métodos; cada constante puede incluso sobrescribirlos con su propio cuerpo.
- `companion object` es el sitio de las fábricas (`parse`, `of`).

## 3. Recorrer y convertir

| Expresión | Resultado |
| --- | --- |
| `Level.entries` | Lista de solo lectura con todas las constantes, en orden de declaración |
| `Level.valueOf("HIGH")` | La constante; **lanza** `IllegalArgumentException` si el nombre no existe |
| `Level.entries.firstOrNull { ... }` | Búsqueda segura: `null` si no hay |
| `level.name` / `level.ordinal` | Texto y posición |

`entries` sustituye al antiguo `values()`: no crea un array nuevo en cada llamada.

**No guardes el `ordinal`** (en un archivo, una base de datos): cambia si reordenas o insertas constantes. Guarda el `name`, o un valor explícito (`val code: Int`).

## 4. `when` exhaustivo

```kotlin
fun urgency(p: Priority): String = when (p) {
    Priority.LOW -> "puede esperar"
    Priority.MEDIUM -> "esta semana"
    Priority.HIGH -> "hoy"
}
```

Sin `else`. Si algún día añades `CRITICAL`, **este código deja de compilar** y el compilador te señala todos los `when` que hay que revisar. Con un `else` esa ayuda se pierde: úsalo solo cuando de verdad quieres un comportamiento por defecto.

Para tener siempre todas las claves, incluso con valor cero:

```kotlin
Level.entries.associateWith { l -> items.count { it.level == l } }
```

## 5. ¿Enum o `String`?

| Cuestión | `String` | `enum` |
| --- | --- | --- |
| Errores de escritura (`"HIHG"`) | Compila y falla en ejecución | No compila |
| Casos olvidados en un `when` | Necesita `else` | El compilador los detecta |
| Autocompletado | No | Sí |
| Un caso con datos propios | No aplica | Usa `sealed` (archivo 02) |

Si cada opción necesita **datos distintos**, un enum se queda corto: ahí entra `sealed`.

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| `IllegalArgumentException: No enum constant` | `valueOf` con un nombre que no existe | `entries.firstOrNull { ... }` |
| `when` no compila tras añadir una constante | Falta la rama | Añádela; es el compilador ayudando |
| Datos corruptos tras reordenar | Se guardó `ordinal` | Guarda `name` o un código explícito |
| Olvidé el `;` | Hay miembros tras las constantes | `LOW, MEDIUM, HIGH;` |

## ✅ Qué debes saber al terminar

- Un enum es un conjunto cerrado de objetos únicos.
- `entries`, `name`, `valueOf` (lanza) y la búsqueda segura.
- `when` exhaustivo sin `else`.
- No persistir el `ordinal`.
