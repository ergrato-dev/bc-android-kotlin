# Glosario — Semana 12

| Término | Definición |
| --- | --- |
| **afterEvaluate** | Bloque que se ejecuta al terminar de configurar un proyecto; con providers no hace falta y se evita |
| **apply** | La función de `Plugin<Project>` que configura el proyecto; Gradle la llama al aplicarse el plugin |
| **convention** | Valor por defecto de una `Property`: se usa si nadie asigna otro |
| **Coordenadas** | `group:artefacto:versión` de algo publicado: `dev.bitacora:build-info-plugin:1.0.0` |
| **Extensión** | Objeto con nombre que un plugin añade al proyecto para que el usuario lo configure: `buildInfo { }` |
| **extensions.create** | Crea una extensión a partir de una clase abstracta y le da nombre |
| **gradlePlugin { }** | Bloque de `java-gradle-plugin` que asocia ids con clases de plugin |
| **implementationClass** | La clase que implementa un id en `gradlePlugin { }` |
| **java-gradle-plugin** | Plugin para builds que producen plugins: `gradlePlugin { }`, `validatePlugins`, `pluginUnderTestMetadata`. Lo aplica `kotlin-dsl` |
| **Marcador** (*plugin marker*) | POM sin código, `<id>:<id>.gradle.plugin:<versión>`, que lleva del id de un plugin a su artefacto |
| **maven-publish** | Plugin que publica artefactos en repositorios Maven (`publish`, `publishToMavenLocal`) |
| **mavenLocal** | Repositorio de la máquina (`~/.m2/repository`), compartido por todos los builds que lo declaran |
| **Plugin binario** | Plugin escrito como clase que implementa `Plugin<Project>` |
| **pluginManager.withPlugin** | Ejecuta un bloque cuando se aplica otro plugin, antes o después del tuyo; nunca si no se aplica |
| **pluginUnderTestMetadata** | Tarea de `java-gradle-plugin` que permite a TestKit usar el plugin sin publicarlo |
| **ProjectBuilder** | Crea un `Project` en memoria para pruebas unitarias de un plugin; no ejecuta tareas |
| **Property&lt;T&gt;** | Valor configurable y perezoso; se conecta con `set(provider)` y se lee con `get()` al ejecutar |
| **provider { }** | Valor perezoso: la lambda se ejecuta cuando alguien pide el valor |
| **TaskProvider** | Referencia perezosa a una tarea registrada; `task.map { it.outputDir }` lleva su salida y su dependencia |
| **validatePlugins** | Tarea que comprueba las anotaciones de las clases de tareas de un plugin; forma parte de `check` |
| **withPluginClasspath** | Opción de TestKit que pone el plugin que se prueba a disposición del build de prueba |
