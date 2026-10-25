# Glosario — Semana 09

| Término | Definición |
| --- | --- |
| **@CacheableTask** | Anotación que declara que el resultado de una tarea depende solo de sus entradas, y puede restaurarse de la caché |
| **@Input / @InputFile** | Anotaciones que declaran un valor o un archivo como entrada de una tarea |
| **@OutputFile / @OutputDirectory** | Anotaciones que declaran lo que produce una tarea |
| **@TaskAction** | Marca el método de una clase de tarea que hace el trabajo |
| **Archivo reproducible** | Un `jar` o `zip` que, con las mismas entradas, da siempre los mismos bytes; es el comportamiento por defecto desde Gradle 9 |
| **Build cache** | Caché que guarda la salida de las tareas por la huella de sus entradas y la restaura (`FROM-CACHE`) |
| **Configuration cache** | Caché que guarda el grafo de tareas ya armado y evita la fase de configuración |
| **Configuración (fase)** | Fase en que se ejecutan los `build.gradle.kts` y se registran las tareas |
| **dependsOn** | Relación: la otra tarea debe ejecutarse antes |
| **Dependencia implícita** | La que Gradle deduce al usar la salida de una tarea como entrada de otra (`from(stage)`) |
| **Ejecución (fase)** | Fase en que corren las tareas pedidas y sus dependencias |
| **finalizedBy** | Relación: la otra tarea se ejecuta justo después |
| **Grafo de tareas** | El conjunto de tareas y sus relaciones, que Gradle ordena |
| **gradle.properties** | Archivo de valores de configuración del build |
| **GradleRunner** | Clase de TestKit que ejecuta un build desde un test |
| **Huella (*hash*)** | Resumen del contenido de las entradas; sirve para saber si algo cambió |
| **Inicialización (fase)** | Fase en que se lee `settings.gradle.kts` y se decide qué proyectos hay |
| **onlyIf** | Condición que salta una tarea (`SKIPPED`) |
| **Plugin** | Extensión que añade tareas y convenciones a un proyecto |
| **Property / Provider** | Valor perezoso (y asignable, en el caso de `Property`), cuyo origen conoce Gradle |
| **register** | Forma perezosa de definir una tarea: el bloque corre solo si se necesita |
| **Tarea de ciclo de vida** | Tarea sin acciones que agrupa otras (`build`, `check`) |
| **TestKit** | Biblioteca de Gradle para probar builds |
| **UP-TO-DATE** | Resultado de una tarea cuyas entradas y salidas no cambiaron desde la última vez |
| **Wrapper** | `gradlew` y su carpeta: descarga y fija la versión exacta de Gradle |
