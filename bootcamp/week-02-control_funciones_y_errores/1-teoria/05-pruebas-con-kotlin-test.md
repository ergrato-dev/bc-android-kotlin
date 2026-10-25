# Pruebas con `kotlin.test`

> Un test es una pregunta con respuesta conocida. Si calculas la respuesta antes de ejecutar, el test te protege. Si la copias de la salida del programa, solo repite lo que el programa ya hace, bien o mal.

## 🎯 Objetivos

- Escribir tests con `@Test` y las aserciones de `kotlin.test`
- Comprobar que una función **lanza** una excepción con `assertFailsWith`
- Elegir casos: normal, límites y error
- Calcular el valor esperado **a mano** antes de ejecutar

## 1. Un test

```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesTest {
    @Test
    fun levelIsMediumAtTheThreshold() = assertEquals("MEDIUM", level(10.0))
}
```

Un archivo `src/test/kotlin/RulesTest.kt` con una clase y funciones marcadas `@Test`. Gradle las encuentra y las ejecuta con `./gradlew test`. Las clases se estudian en la semana 4; por ahora, copia la forma.

El nombre del test debe decir **qué se comprueba**: `levelIsMediumAtTheThreshold` se entiende sin abrir el cuerpo. También puedes usar nombres con espacios entre comillas invertidas: ``fun `level is medium at the threshold`()``.

## 2. Las aserciones

| Aserción | Comprueba |
| --- | --- |
| `assertEquals(esperado, real)` | Igualdad. El esperado va **primero** |
| `assertTrue(c)`, `assertFalse(c)` | Un booleano |
| `assertNull(x)`, `assertNotNull(x)` | Nulidad |
| `assertFailsWith<T> { ... }` | El bloque lanza una excepción del tipo `T` |
| `assertEquals(esperado, real, mensaje)` | Con un mensaje que verás si falla |

```kotlin
@Test
fun dividingByZeroIsRejected() {
    val e = assertFailsWith<IllegalArgumentException> { dividir(1.0, 0.0) }
    assertEquals("b no puede ser 0", e.message)
}
```

`assertFailsWith` devuelve la excepción para que compruebes su mensaje. Además de que **falle**, interesa que falle **por la razón correcta**.

## 3. Qué casos probar

| Tipo | Ejemplo con `level` |
| --- | --- |
| Normal | `level(12.0)` → `"MEDIUM"` |
| Justo en el límite | `level(10.0)` → `"MEDIUM"` y `level(9.99)` → `"LOW"` |
| Vacío o cero | Lista vacía, texto vacío |
| Error | `level(-1.0)` lanza `IllegalArgumentException` |

La mayoría de los bugs viven **en los límites** (`>=` frente a `>`, `until` frente a `..`). Un test justo en el límite y otro justo al lado valen más que diez en el medio.

## 4. Calcular a mano

Antes de escribir el `assertEquals`, resuelve en papel:

```
Observaciones: (2 unidades, medida 1.5) y (1 unidad, medida 2.0)
total   = 2 * 1.5 + 1 * 2.0 = 5.0
media   = (1.5 + 2.0) / 2   = 1.75
```

Después escribes `assertEquals(5.0, total(lista))`. Si el test falla, el bug está en el código **o** en tu cuenta; descubrir cuál es parte del trabajo.

## 5. Ritmo de trabajo

1. Escribe el test (falla, en rojo).
2. Escribe el código mínimo para que pase (verde).
3. Mejora el código sin romper el test.

Es el ritmo de los ejercicios: los tests ya vienen escritos y tú pones el código. En el proyecto, tú escribes los dos.

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| Esperado y real al revés | El mensaje de fallo engaña | `assertEquals(esperado, real)` |
| Comparar `Double` con `assertEquals` y falla por poco | Error de redondeo | `assertEquals(esperado, real, absoluteTolerance = 1e-9)` |
| Un test depende de otro | Estado compartido | Cada test se basta solo |
| El test pasa siempre | Valor copiado de la salida | Calcula a mano antes de ejecutar |

## ✅ Qué debes saber al terminar

- `@Test`, `assertEquals`, `assertFailsWith`.
- Probar el caso normal, los límites y el error.
- Calcular el valor esperado a mano.
