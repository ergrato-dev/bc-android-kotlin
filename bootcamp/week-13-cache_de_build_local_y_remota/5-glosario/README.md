# Glosario — Semana 13

| Término | Definición |
| --- | --- |
| **@CacheableTask** | Anotación que declara cacheable una clase de tarea; sin ella, sus salidas no se guardan en la caché de build |
| **Caché de build** | Almacén de salidas de tareas indexado por su clave; se activa con `org.gradle.caching=true` |
| **Caché local** | La caché de build de la máquina, por defecto en `~/.gradle/caches/build-cache-1`; borra lo que no se usa en 7 días |
| **Cache poisoning** | Una entrada mala en una caché compartida, que descargan todos; por eso solo el CI sube |
| **Caché remota** | Caché de build en un servidor, compartida por el equipo; se consulta después de la local |
| **Clave de caché** | Hash de la implementación, las entradas y los nombres de las salidas de una tarea |
| **Credenciales** | Usuario y contraseña de la caché remota; salen de variables de entorno, nunca del repositorio |
| **FROM-CACHE** | Resultado de una tarea cuyas salidas se copian de la caché, sin ejecutarla |
| **HttpBuildCache** | Tipo de caché remota que habla HTTP: `GET` para bajar y `PUT` para subir |
| **inputs.files** | API de ejecución para declarar archivos de entrada de una tarea que no son propiedades anotadas |
| **isAllowInsecureProtocol** | Permite `http` en la caché remota; Gradle solo lo acepta sin ella para `127.0.0.1` |
| **isPush** | Si una caché remota recibe resultados de este build; `true` solo en el CI |
| **Normalización** | Cómo se compara un archivo de entrada: solo contenido, nombre, ruta relativa o absoluta |
| **org.gradle.caching** | Propiedad de `gradle.properties` que activa la caché de build en todas las ejecuciones |
| **@PathSensitive** | Anotación que elige la normalización de un archivo de entrada; obligatoria en tareas cacheables |
| **PathSensitivity.RELATIVE** | Normalización por contenido y ruta relativa al proyecto; la que hace reubicable una tarea |
| **Reproducible** | Tarea cuyas mismas entradas dan las mismas salidas, byte a byte |
| **--rerun-tasks** | Ejecuta todas las tareas sin mirar `UP-TO-DATE` ni leer la caché, pero guardando en ella |
| **Reubicable** | Tarea cuya clave no depende de la carpeta del proyecto |
| **withPathSensitivity** | El `@PathSensitive` de la API de ejecución (`inputs.files(...).withPathSensitivity(...)`) |
