# Rúbrica de Evaluación — Semana 13: Caché de build local y remota

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora con caché de build |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre `UP-TO-DATE` y `FROM-CACHE`, y por qué después de `clean` solo puede darse el segundo? | 10 |
| 2 | ¿Qué diferencia hay entre la caché de build y la caché de configuración? | 10 |
| 3 | ¿De qué se calcula la clave de caché de una tarea? | 10 |
| 4 | ¿Por qué una tarea propia no es cacheable por defecto y qué hace falta para que lo sea? | 10 |
| 5 | ¿Qué es una tarea reubicable y qué `PathSensitivity` la rompe? | 10 |
| 6 | ¿Qué pasa en la caché con una tarea que escribe la hora en su salida? | 10 |
| 7 | ¿Por qué `:test` del raíz quedaba `UP-TO-DATE` al cambiar `core/` y cómo se arregla? | 10 |
| 8 | ¿Por qué solo el CI sube resultados a la caché remota y cómo se configura? | 10 |
| 9 | ¿Dónde van la URL y las credenciales de la caché remota, y por qué no en `settings.gradle.kts`? | 10 |
| 10 | ¿Qué hace Gradle si la caché remota falla, y cómo encuentras por qué una tarea no sale de la caché? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. `UP-TO-DATE` significa que las salidas que hay en `build/` corresponden a las entradas de ahora, y no se hace nada. `FROM-CACHE` significa que las salidas se copian de la caché, guardadas con la clave de esas entradas. `clean` borra `build/`, así que no queda nada que pueda estar al día; la caché vive fuera del proyecto.
2. La de configuración guarda el grafo de tareas ya configurado y se salta la fase de configuración. La de build guarda las salidas de las tareas y se salta su ejecución. Son independientes y se usan juntas.
3. De la implementación de la tarea (clase y classpath), de sus valores `@Input`, de la huella de sus archivos de entrada (contenido y, según la normalización, la ruta) y de los nombres de sus salidas.
4. Porque guardar cuesta: comprimir, escribir y, con caché remota, subir. Para tareas que tardan menos que eso no compensa, como `jar`. Hace falta `@CacheableTask` y una normalización (`@PathSensitive`, `@Classpath`...) en cada archivo de entrada; sin normalización, Gradle falla con `missing a normalization strategy`.
5. Una tarea cuya clave no depende de la carpeta del proyecto: dos copias en sitios distintos aciertan en la misma entrada. `PathSensitivity.ABSOLUTE` la rompe, porque mete la ruta completa en la clave. También una ruta absoluta guardada como `@Input` de texto.
6. La hora no es una entrada, así que la tarea sigue acertando. Pero después de `clean` el archivo trae la hora de la primera ejecución: la caché copia, no ejecuta. Además, cualquier tarea que use ese archivo como entrada cambia de clave en cada ejecución real y nunca acierta.
7. Los tests de TestKit leen el proyecto entero, pero las entradas de `:test` eran solo las clases de test y su classpath. Gradle no veía `core/`. Con la caché era peor: `clean` ya no lo arreglaba, porque `:test` salía `FROM-CACHE`. Se arregla con `inputs.files(fileTree(...))` con `withPathSensitivity(RELATIVE)`, excluyendo `build/` y las cachés.
8. Porque lo que hay en el servidor lo descarga todo el equipo: tiene que salir de un build limpio de `main`, no de un portátil con cambios sin commit. Una entrada mala compartida (*cache poisoning*) es un riesgo de seguridad. Se configura con `isPush = providers.environmentVariable("CI").isPresent`.
9. La URL, en una propiedad de Gradle (`-P` o `~/.gradle/gradle.properties`); las credenciales, en variables de entorno, que en el CI vienen de su almacén de secretos. `settings.gradle.kts` está en el repositorio y lo lee cualquiera que lo clone.
10. Avisa, desactiva la caché remota durante ese build y sigue: `The remote build cache was disabled during the build due to errors.` Para encontrar por qué una tarea no acierta, se ejecuta con `-Dorg.gradle.caching.debug=true` en las dos situaciones y se comparan las líneas `Appending ...`: la distinta es la entrada que cambia.

**Ejercicio 01, paso 5**: (a) `property 'source' is annotated with @InputFile but missing a normalization strategy`. A una tarea que no es cacheable no le hace falta: su resultado nunca se reutiliza fuera de esa carpeta. (b) Fallan `aRelocatedCopyReusesTheResults` (la copia ejecuta la tarea: `SUCCESS`) y `theTaskDeclaresARelativePath`. La línea de `source` contiene `ABSOLUTE_PATH{...}` con la ruta de tu carpeta. (c) `SUCCESS`, `UP-TO-DATE`, `FROM-CACHE`. La segunda encuentra las salidas en `build/`; la tercera ya no, pero la caché sí. (d) La hora de la primera ejecución: la caché copia la salida guardada. Una tarea que usara ese informe como entrada cambiaría de clave en cada ejecución real.

**Ejercicio 02, paso 5**: (a) `Using insecure protocols with remote build cache, without explicit opt-in, is unsupported.` Gradle solo acepta `http` hacia `127.0.0.1`, la dirección de *loopback*, que no sale de la máquina; `localhost` es un nombre y no lo da por seguro. (b) Falla `aDeveloperOnlyPulls`: el desarrollador sube 1 entrada. Al servidor llegaría lo que haya en cualquier portátil: cambios sin commit, otro JDK, un archivo generado a mano. (c) Sí: la caché es una optimización, y una optimización no debería impedir construir. Te enterarías por el tiempo del CI, o buscando la línea `The remote build cache was disabled` en su salida. (d) La URL, en `~/.gradle/gradle.properties` (`bitacora.cacheUrl=...`); la contraseña, en variables de entorno o en el gestor de secretos de tu sistema, nunca en un archivo del repositorio.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Tareas cacheables y reubicables (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| `@CacheableTask` en la clase, `RELATIVE` en `source`, la caché activada en `gradle.properties` y no con un flag (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Caché remota con push desde el CI (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Sin URL no hay caché remota; ninguna credencial ni `isAllowInsecureProtocol` en el archivo (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora con caché de build

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: copia en otra carpeta, cambio de versión, `run` tras `clean`, CI sube y otra máquina baja, servidor caído, contraseña mala | 25 |
| Caché | Activada en `gradle.properties`; remota con la URL en una propiedad, push solo con `CI`, credenciales de variables de entorno | 25 |
| Entradas de `:test` | Declaradas con `inputs.files`, rutas relativas, sin `build/` ni cachés; cambiar `core/` vuelve a ejecutar `:test` | 10 |
| Adaptación al dominio | Tu dominio en la salida de `app`; README que explica el fallo de `:test` y por qué la caché lo empeoraba | 20 |
| Entrega | Rama `week-13`, pull request con la salida de `test` y de dos `clean build` seguidos | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una contraseña o un usuario escritos en un archivo del repositorio | -20 |
| `isPush = true` sin condición | -10 |
| `PathSensitivity.ABSOLUTE` o una ruta absoluta como `@Input` | -5 cada una |
| Una salida con la hora o un valor aleatorio | -5 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-13` → `main` en el repositorio del aprendiz.
