# Ejercicio 02 — Interfaces y herencia

⏱️ 2 h · Teoría previa: 03 y 04

## Objetivo

Usar polimorfismo con interfaces y clases abstractas, y añadir funcionalidad desde fuera con extensiones y un `companion object`.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`Double.format2()`**: función de extensión con `String.format(Locale.ROOT, "%.2f", this)` (añade `import java.util.Locale`).
2. **`String.isCode`**: propiedad de extensión con `Regex("[A-Z]{3}-[0-9]{3}").matches(this)`.
3. **`Shape`, `Circle`, `Square`**: implementa el método por defecto `describe()` en la interfaz y `area()` en cada clase (`import kotlin.math.PI`). Comprueba que `Circle(5.0).describe()` no repite código.
4. **`totalArea` y `largest`**: funciones sobre `List<Shape>` que no preguntan por el tipo concreto.
5. **`Employee`, `Manager`, `Developer`**: implementa `salary()` y `title()`. En `Developer`, `level` debe estar entre 1 y 5 (`require` en `init`).
6. **`payroll`**: suma de `salary()`. Añade luego un `class Intern : Employee(...)` en `main` y comprueba que `payroll` **no hace falta tocarla**.
7. **`Version`**: `compareTo` con `compareValuesBy`, `toString` como `1.2.3` y `parse` en el `companion object` (`null` si no son exactamente 3 enteros no negativos).
8. **Responde en `NOTAS.md`**:
   - ¿Por qué `describe()` está en la interfaz y `salary()` es abstracta en una clase? ¿Podrías haber hecho `Employee` una interfaz? ¿Qué cambia?
   - ¿Por qué `Version(1, 10, 0)` debe ir **después** de `Version(1, 2, 0)` al ordenar, y qué pasaría si fueran `String`?
   - Una extensión no puede ver lo `private` de la clase: ¿por qué es una ventaja?
9. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Class 'Circle' is not abstract and does not implement...` | Falta implementar `area()` o `name` |
| `'area' hides member of supertype and needs 'override'` | Añade `override` |
| `This type is final, so it cannot be inherited from` | `Employee` debe ser `abstract` |
| `Version.parse("1.2.x")` lanza | Usa `toIntOrNull()` y devuelve `null` cuando falle |
| Los salarios no coinciden por decimales | Usa la fórmula pedida: `base + base * level / 10` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con el paso 8.
