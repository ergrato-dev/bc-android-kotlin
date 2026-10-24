# Glosario — Semana 01

| Término | Definición |
| --- | --- |
| **allWarningsAsErrors** | Opción del compilador que convierte cada aviso en error de compilación |
| **Bytecode** | Instrucciones intermedias de la JVM, guardadas en archivos `.class` |
| **BOM** | Lista de versiones compatibles de librerías; se importa con `platform(...)` |
| **const val** | Constante conocida al compilar, declarada fuera de una función |
| **Daemon (Gradle)** | Proceso JVM que se queda en memoria para acelerar las siguientes ejecuciones |
| **data class** | Clase para datos; genera `toString`, `equals`, `hashCode` y `copy` |
| **Desbordamiento (overflow)** | Resultado fuera del rango del tipo; en `Int` da la vuelta |
| **Elvis (`?:`)** | Operador que da un valor alternativo si la izquierda es `null` |
| **Gradle** | Herramienta que compila, prueba y empaqueta el proyecto |
| **Inferencia de tipos** | El compilador deduce el tipo del valor, sin que lo escribas |
| **JDK** | Kit de desarrollo de Java: compilador, herramientas y JVM |
| **JIT** | Compilación a código máquina mientras el programa corre |
| **JVM** | Máquina virtual que ejecuta el bytecode |
| **Kotlin DSL** | Los archivos `.gradle.kts`: configuración de Gradle escrita en Kotlin |
| **Llamada segura (`?.`)** | Ejecuta la llamada solo si el valor no es `null` |
| **Locale** | Configuración regional; afecta a decimales, fechas y mayúsculas |
| **Tarea (task)** | Unidad de trabajo de Gradle (`compileKotlin`, `test`…) |
| **Toolchain** | JDK concreto con el que Gradle compila el proyecto |
| **val / var** | Variable que no se reasigna / que sí se reasigna |
| **Wrapper (`gradlew`)** | Script que descarga y usa la versión fijada de Gradle |
