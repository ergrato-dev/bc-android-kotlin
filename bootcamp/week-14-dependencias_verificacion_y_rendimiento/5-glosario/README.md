# Glosario — Semana 14

| Término | Definición |
| --- | --- |
| **because** | Motivo de una restricción o dependencia; aparece en `dependencyInsight` como `By constraint: <motivo>` |
| **BOM** (*platform*) | Publicación que solo aporta restricciones de versión para una familia de bibliotecas: `platform(...)` |
| **Build Scan** | Informe detallado de un build (`--scan`) que se sube a un servidor externo |
| **Cadena de suministro** | Todo el código de otros que entra en tu build: bibliotecas, plugins y sus dependencias |
| **Conflicto de versiones** | Dos o más peticiones de versiones distintas del mismo módulo; Gradle elige la más alta |
| **constraints { }** | Bloque de restricciones: fija la versión de un módulo si alguien lo trae, sin añadirlo |
| **dependencies** (tarea) | Imprime el árbol de dependencias de una configuración |
| **dependencyInsight** | Explica, para un módulo y una configuración, qué versión ganó, por qué y quién la pidió |
| **--dependency-verification lenient** | Convierte los fallos de verificación en avisos; solo para actualizar, nunca en el repositorio |
| **exclude** | Quita una transitiva de una configuración: `configurations.runtimeClasspath { exclude(...) }` |
| **failOnVersionConflict** | Estrategia de resolución que convierte cualquier conflicto en un error |
| **org.gradle.configuration-cache** | Propiedad de `gradle.properties` que activa la caché de configuración en todas las ejecuciones |
| **org.gradle.parallel** | Propiedad que ejecuta a la vez tareas de proyectos distintos que no dependen entre sí |
| **--profile** | Genera en `build/reports/profile` un informe local con el tiempo de cada fase y tarea |
| **Restricción** | Ver `constraints { }`; en el árbol de `dependencies` aparece con `(c)` |
| **sha256** | Suma criptográfica de un archivo; la que se usa en la verificación (no `sha1` ni `md5`) |
| **Transitiva** | Dependencia que no declaras tú, sino una de tus dependencias |
| **verification-metadata.xml** | Archivo en `gradle/` con el sha256 de cada artefacto que usa el build; con él, la verificación está activa |
| **verify-metadata** | Opción del archivo de verificación: verifica también `.pom` y `.module`, no solo los jars |
| **verify-signatures** | Opción que comprueba además la firma PGP de quien publica; desactivada por defecto |
| **--write-verification-metadata** | Genera o actualiza `verification-metadata.xml` con el algoritmo indicado (`sha256`) |
