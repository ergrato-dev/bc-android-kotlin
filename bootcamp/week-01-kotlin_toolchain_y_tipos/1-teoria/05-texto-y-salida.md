# Texto y salida

> Un programa que calcula bien pero imprime mal no sirve. Aquí aprendes plantillas de texto, formato numérico, y la diferencia entre lo que el programa dice y lo que el programa avisa.

## 🎯 Objetivos

- Construir texto con plantillas `$` y `${}`
- Formatear números con ancho y decimales usando `String.format`
- Evitar el error de la coma decimal con `Locale.ROOT`
- Separar salida normal (`println`) de avisos (`System.err`)
- Escribir una `data class` y usar su `toString` y su igualdad

## 1. Plantillas de texto

```kotlin
val nombre = "Ada"
val edad = 36
println("Hola, $nombre. El año que viene tendrás ${edad + 1}.")
```

`$nombre` inserta una variable; `${expresión}` inserta cualquier expresión. Las comillas triples `"""` abren un texto de varias líneas sin escapar nada.

## 2. Formato numérico

```kotlin
import java.util.Locale

String.format(Locale.ROOT, "%-8s|%5d|%10.2f", "OBS-001", 3, 12.5)
// "OBS-001 |    3|     12.50"
```

| Especificador | Significa |
| --- | --- |
| `%s` | Texto |
| `%-8s` | Texto alineado a la izquierda en 8 columnas |
| `%5d` | Entero alineado a la derecha en 5 columnas |
| `%10.2f` | Decimal en 10 columnas con 2 decimales |

### El error de la coma decimal

`String.format("%.2f", 12.5)` usa el *locale* de tu sistema. En un sistema configurado en español puede escribir `12,50` y en uno en inglés `12.50`. Los tests pasarían en una máquina y fallarían en otra. **Siempre pasa `Locale.ROOT`** cuando el formato es parte del contrato (tests, archivos, protocolos).

## 3. Salida normal y avisos

```kotlin
println("Total: 52.75")        // salida estándar (stdout)
System.err.println("Aviso: lista vacía")  // error estándar (stderr)
```

Son dos canales distintos. Si ejecutas `./gradlew run > salida.txt`, el primero va al archivo y el segundo sigue saliendo por pantalla. Guarda en stdout el **resultado** y en stderr los **avisos**: así otro programa puede leer lo primero sin ensuciarse con lo segundo.

## 4. `data class`

```kotlin
data class Observation(
    val code: String,
    val description: String,
    val quantity: Int,
    val measure: Double,
)

val a = Observation("OBS-001", "Lote norte", 3, 12.5)
val b = Observation("OBS-001", "Lote norte", 3, 12.5)
println(a)         // Observation(code=OBS-001, description=Lote norte, quantity=3, measure=12.5)
println(a == b)    // true: compara por valor
val c = a.copy(quantity = 4)
```

Una `data class` genera `toString`, `equals`, `hashCode` y `copy` a partir de las propiedades del constructor. Es la forma estándar de representar datos en Kotlin, y la pieza central de tu Bitácora.

## 5. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| Tu test pasa en tu máquina y falla en otra | `String.format` sin `Locale` | `Locale.ROOT` |
| `MissingFormatArgumentException` | Hay más `%` que argumentos | Cuenta los especificadores |
| `IllegalFormatConversionException: d != java.lang.Double` | `%d` con un `Double` | `%f` para decimales |
| Dos objetos iguales dan `false` | Es una `class` normal, no `data class` | Declárala `data class` |

## ✅ Qué debes saber al terminar

- Plantillas con `$` y `${}`.
- `String.format(Locale.ROOT, ...)` con ancho y decimales.
- Resultado por `println`, avisos por `System.err`.
- Qué te regala una `data class`.
