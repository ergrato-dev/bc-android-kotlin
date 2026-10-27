# Tareas del plugin y otros plugins

> Un plugin registra sus tareas igual que un `build.gradle.kts`, y además tiene que convivir con plugins que puede haber o no: si el proyecto aplica `java`, su archivo va a los recursos; si no, el plugin funciona igual.

## 🎯 Objetivos

- Registrar una tarea propia desde un plugin, con grupo y descripción
- Reaccionar a otro plugin con `pluginManager.withPlugin`
- Mantener el plugin compatible con la caché de configuración

## 1. La tarea

La clase `GenerateBuildInfo` es la de la semana 9, sin cambios: entradas con `@Input`, salida con `@OutputDirectory`, `@CacheableTask`. Lo nuevo es que la registra el plugin:

```kotlin
val task = project.tasks.register("generateBuildInfo", GenerateBuildInfo::class.java) {
    group = "bitacora"
    description = "Escribe build-info.properties con la versión y el dominio."
    appVersion.set(extension.version)
    domain.set(extension.domain)
    outputDir.set(project.layout.buildDirectory.dir("generated/build-info"))
}
```

- **`register`, nunca `create`**: la tarea solo se configura si alguien la necesita (semana 9).
- **Grupo y descripción**: es lo que ve el usuario en `./gradlew tasks`. Un plugin sin ellos esconde sus tareas.
- `register` devuelve un `TaskProvider`: una referencia **perezosa** a la tarea, que se usa en la sección 2.

## 2. Si hay `java`

```kotlin
project.pluginManager.withPlugin("java") {
    project.extensions.getByType(SourceSetContainer::class.java).named("main") {
        resources.srcDir(task.map { it.outputDir })
    }
}
```

- **`withPlugin("java")`** ejecuta el bloque cuando el plugin `java` se aplica, **antes o después** del tuyo. Si nunca se aplica, no hace nada.
- `java` también lo aplican `java-library` y `application`: el bloque funciona con los dos.
- **`task.map { it.outputDir }`**: los recursos dependen de la salida de la tarea, y Gradle deduce que `processResources` necesita `generateBuildInfo`. Sin `dependsOn`.

Sin `withPlugin`, el plugin rompe en proyectos sin `java`:

```
Extension of type 'SourceSetContainer' does not exist. Currently registered extension types: [ExtraPropertiesExtension, VersionCatalogsExtension, BuildInfoExtension]
```

> 🔬 Medido en Gradle 9.8.2: es el error al aplicar `bitacora.build-info` en un build sin `java` si el bloque no va dentro de `withPlugin`.

## 3. La caché de configuración

Las reglas son las de la semana 9, y el plugin las cumple si:

- la acción de la tarea solo lee sus propiedades, nunca `project`;
- los valores viajan como providers (`set(extension.domain)`), no como lambdas que capturan el proyecto en la acción.

`project.provider { project.version.toString() }` usa `project`, pero **al configurar**: Gradle evalúa el provider y guarda el resultado en la caché. Lo prohibido es tocar `project` al **ejecutar**.

> 🔬 Medido: con `--configuration-cache`, la segunda ejecución de `generateBuildInfo` dice `Configuration cache entry reused`.

## 4. Qué expone un plugin

| Expone | No expone |
| --- | --- |
| Su id y su extensión (`buildInfo { }`): es su API | Cómo se llama la carpeta de salida, si nadie la necesita |
| Sus tareas, con grupo y descripción | Valores leídos al configurar |
| Valores por defecto razonables (`version` del proyecto) | Configuración que el usuario no puede cambiar sin tocar el plugin |

La extensión es la API pública del plugin: cambiarla es romper a quien la usa, como cambiar una función pública de `core`.

## ✅ Comprueba

- [ ] ¿Qué pasa si `withPlugin("java")` se ejecuta en un proyecto que aplica `java` después de tu plugin?
- [ ] ¿Por qué `resources.srcDir(task.map { it.outputDir })` no necesita `dependsOn`?
- [ ] ¿Por qué `project.provider { project.version }` no rompe la caché de configuración?
