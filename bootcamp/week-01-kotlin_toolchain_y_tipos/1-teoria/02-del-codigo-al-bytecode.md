# Del código al bytecode

> Antes de usar Gradle conviene saber qué hace por ti: escribir `.kt`, producir `.class`, empaquetar un `.jar` y arrancar la JVM. Si entiendes esas cuatro cosas, los errores dejan de ser misteriosos.

## 🎯 Objetivos

- Nombrar las etapas entre un `.kt` y un programa ejecutándose
- Leer un error y decir si es de **compilación** o de **ejecución**
- Abrir un `.class` con `javap` y reconocer lo que Kotlin genera
- Entender por qué `allWarningsAsErrors` está activo en todos los starters

## 1. El camino completo

| Etapa | Herramienta | Entrada | Salida |
| --- | --- | --- | --- |
| Compilar | `kotlinc` (lo invoca Gradle) | `Main.kt` | `MainKt.class` |
| Empaquetar | tarea `jar` | `.class` + recursos | `hola-toolchain.jar` |
| Cargar y ejecutar | `java` (la JVM) | `.class` / `.jar` | El programa corriendo |

Kotlin convierte las funciones de nivel superior de `Main.kt` en métodos estáticos de una clase llamada `MainKt`. Por eso en `build.gradle.kts` aparece `mainClass.set("MainKt")`.

Comprueba con tus ojos:

```bash
./gradlew build
ls build/classes/kotlin/main        # MainKt.class
javap -c -cp build/classes/kotlin/main MainKt
```

`javap -c` desensambla el bytecode. En un `saludo(nombre: String)` verás primero una llamada a `Intrinsics.checkNotNullParameter`: Kotlin protege el contrato "este parámetro no es nulo" también cuando alguien lo llama desde Java.

## 2. Errores de compilación y de ejecución

| | Compilación | Ejecución |
| --- | --- | --- |
| Cuándo | Al construir (`compileKotlin`) | Al correr el programa o los tests |
| Ejemplo | `Initializer type mismatch: expected 'Int', actual 'Double'.` | `ArithmeticException: / by zero` |
| Quién lo detecta | El compilador | La JVM |
| Cómo se arregla | Cambias el código antes de ejecutar nada | Cambias el código o validas el dato |

La regla práctica: **prefiere que el error sea de compilación**. Para eso sirven los tipos.

## 3. Warnings como errores

Un *warning* es un aviso que no detiene el build. Los avisos que nadie lee se acumulan hasta que uno de ellos era el bug. Por eso cada starter tiene:

```kotlin
kotlin {
    compilerOptions { allWarningsAsErrors.set(true) }
}
```

Con eso, usar algo marcado como `@Deprecated` rompe el build:

```
e: warnings found and -Werror specified
e: src/main/kotlin/Err.kt:7:13 'fun viejo(): Int' is deprecated. x.
```

Observa que el compilador de Kotlin **no** avisa de una variable sin usar: eso lo hace el IDE con una inspección (la ves subrayada en gris). El compilador y el IDE son dos herramientas distintas con listas de avisos distintas.

## 4. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| `Could not find or load main class MainKt` | `mainClass` no coincide con el nombre del archivo | Archivo `Main.kt` → clase `MainKt` |
| `Unresolved reference: x` | Falta un `import` o el nombre está mal escrito | Revisa el nombre y los imports |
| Cambié el código y la salida no cambió | Ejecutaste el `.jar` viejo | `./gradlew build` antes de ejecutar |

## ✅ Qué debes saber al terminar

- Un `.kt` pasa a `.class`, luego a `.jar`, y la JVM lo ejecuta.
- `javap -c` te enseña el bytecode.
- Los warnings se tratan como errores para que no se acumulen.
