# Diagnosticar la caché

> Cuando una tarea que esperabas `FROM-CACHE` sale `SUCCESS`, hay una diferencia en su clave. Encontrarla es comparar, entrada por entrada, lo que Gradle metió en la clave en cada build.

## 🎯 Objetivos

- Ver la clave de caché de una tarea y de qué se calcula
- Comparar las claves de dos builds para encontrar la diferencia
- Saber qué opciones de la línea de comandos cambian el uso de la caché

## 1. Ver la clave

Con `--info`, Gradle dice qué clave calcula para cada tarea y si la encuentra:

```
Build cache key for task ':countObservations' is 478ba71db7ab1fd3374efa421f47a4f1
Stored cache entry for task ':countObservations' with cache key 478ba71db7ab1fd3374efa421f47a4f1
```

Con `-Dorg.gradle.caching.debug=true`, además, de qué se calcula, línea a línea:

```
Appending implementation to build cache key: Build_gradle$CountObservations_Decorated@b985dd8b...
Appending input value fingerprint for 'skipHeader' to build cache key: f6d7ed39...
Appending input file fingerprints for 'source' to build cache key: 1cedabae... - ABSOLUTE_PATH{/home/tú/a2/data/observations.csv=...}
Appending output property name to build cache key: report
Build cache key for task ':countObservations' is 478ba71db7ab1fd3374efa421f47a4f1
```

> 🔬 Medido en Gradle 9.8.2. La tercera línea es la del capítulo anterior: la ruta absoluta en la clave.

## 2. Comparar dos builds

El método es mecánico:

1. Ejecuta la tarea con `-Dorg.gradle.caching.debug=true` en las dos situaciones (las dos carpetas, las dos máquinas, antes y después del cambio) y guarda la salida de cada una.
2. Quédate con las líneas `Appending ...` de esa tarea y compáralas con `diff`.
3. La línea distinta es la entrada que cambia. Su nombre te dice qué propiedad es.

| La línea distinta es | Suele ser |
| --- | --- |
| `implementation` | Otra versión del plugin que trae la tarea, o has cambiado su código |
| `input value fingerprint` | Un `@Input` que cambia en cada build: una fecha, una ruta absoluta en un `String`, una variable de entorno |
| `input file fingerprints` con `ABSOLUTE_PATH` | La normalización (capítulo 03) |
| `input file fingerprints` con el mismo `RELATIVE_PATH` | El contenido del archivo es distinto: finales de línea, un archivo generado no reproducible |

Un *Build Scan* (`./gradlew build --scan`) hace esta comparación con una interfaz web, pero sube los datos del build a un servidor de Gradle. En un proyecto de empresa, pregunta antes de usarlo.

## 3. Opciones de la línea de comandos

| Opción | Qué hace |
| --- | --- |
| `--build-cache` / `--no-build-cache` | Activa o desactiva la caché en esa ejecución |
| `--rerun-tasks` | Ejecuta todas las tareas: no usa `UP-TO-DATE` ni lee la caché, pero sí guarda en ella |
| `--offline` | No usa la red, y tampoco la caché remota: `Remote build cache is disabled when running with --offline.` |
| `-Dorg.gradle.caching.debug=true` | Imprime de qué se calcula cada clave |

> 🔬 Medido en Gradle 9.8.2: con `--rerun-tasks`, la tarea se ejecuta y `--info` dice `Stored cache entry`. Sirve para regenerar una entrada que sospechas que está mal. El mensaje de `--offline` también es medido.

## 4. Lista de comprobación

Antes de dar una tarea propia por cacheable:

- [ ] Tiene `@CacheableTask`.
- [ ] Cada archivo de entrada tiene `@PathSensitive` (`RELATIVE` salvo motivo).
- [ ] Ninguna entrada es una ruta absoluta, una fecha o un valor aleatorio.
- [ ] Sus salidas no llevan la hora ni rutas absolutas.
- [ ] Una copia del proyecto en otra carpeta la recupera `FROM-CACHE` (es un test).
- [ ] Si lee archivos que no son sus entradas (como los tests de TestKit), están declarados con `inputs.files`.

## ✅ Comprueba

- [ ] ¿Qué opción enseña de qué se calcula la clave de una tarea?
- [ ] Dos máquinas calculan claves distintas para la misma tarea. ¿Cómo encuentras la causa?
- [ ] ¿Para qué sirve `--rerun-tasks` con la caché activada?
