# El build de la Bitácora, completo

> Esta semana cierra la fase 2. En seis semanas, la Bitácora ha pasado de un módulo con un `build.gradle.kts` copiado a un build multi-módulo con plugins propios, caché local y remota, dependencias verificadas y tests de su propio build. Este es el repaso, y lo que cambia cuando llegue Android.

## 🎯 Objetivos

- Repasar las decisiones del build de la Bitácora y de qué semana viene cada una
- Revisar tu build con una lista de comprobación
- Saber qué parte del build cambia con el plugin de Android en la fase 3

## 1. Qué tienes y por qué

| Pieza | Semana | Por qué |
| --- | --- | --- |
| Tareas con entradas y salidas declaradas | 9 | `UP-TO-DATE` y caché correctas |
| Módulos `core`, `data`, `app` con `api`/`implementation` | 10 | Un cambio interno no recompila a los demás |
| Catálogo de versiones | 10 | Cada versión, una vez |
| `build-logic` con convention plugins | 11 | Cada módulo dice en una línea qué es |
| `bitacora.build-info`, plugin binario con tests | 12 | La versión y el dominio, en el programa |
| Caché de build local y remota, push desde el CI | 13 | `clean` no obliga a recompilar; el equipo comparte trabajo |
| `:test` con sus entradas declaradas | 13 | Los tests del build se ejecutan cuando cambia el código |
| Verificación de dependencias | 14 | Lo que llega es lo que se publicó |
| Caché de configuración y paralelo | 14 | La configuración no se repite; los módulos se construyen a la vez |

## 2. Lista de comprobación

- [ ] `./gradlew build` sin warnings (`allWarningsAsErrors`).
- [ ] Ninguna versión fuera del catálogo; todas completas, ninguna `latest` ni `+`.
- [ ] Ningún módulo configura a otro (`subprojects { }`, `allprojects { }`).
- [ ] Toda tarea propia declara sus entradas y salidas; las cacheables, con `@PathSensitive(RELATIVE)`.
- [ ] Ninguna salida lleva la hora ni rutas absolutas.
- [ ] `gradle/verification-metadata.xml` en el repositorio, con `sha256`, sin `lenient`.
- [ ] Ninguna credencial en un archivo del repositorio.
- [ ] `org.gradle.caching`, `org.gradle.configuration-cache` y `org.gradle.parallel` en `gradle.properties`.
- [ ] Los tests del build comprueban el efecto de cada decisión, con valores calculados a mano.

## 3. Qué cambia con Android

En la fase 3, `app` pasa a ser una aplicación Android. El plugin de Android (AGP 9.4.1, en `docs/stack-versions.md`) añade mucho al build: variantes, recursos, manifiesto, firma. La estructura no cambia:

- **Un convention plugin más**: `bitacora.android-application`, en el mismo `build-logic`, con la configuración de Android que repetirían todos los módulos de app.
- **`core` y `data` siguen siendo Kotlin puro.** Se prueban en milisegundos, sin emulador. Lo que no depende de Android no debería depender de Android.
- **El catálogo crece**: AGP, Compose, AndroidX. Cada versión entra con su verificación, y el `verification-metadata.xml` se regenera y se revisa en el pull request.
- **La caché y la configuración importan más**: un build Android tiene muchas más tareas que el de ahora, y la diferencia entre un build bien declarado y uno mal declarado se mide en minutos.

## ✅ Comprueba

- [ ] Elige tres filas de la tabla de la sección 1 y explica qué pasaría sin ellas.
- [ ] ¿Qué parte de tu `build-logic` reutilizarás para Android y qué parte será nueva?
- [ ] ¿Por qué `core` no debería depender de Android?
