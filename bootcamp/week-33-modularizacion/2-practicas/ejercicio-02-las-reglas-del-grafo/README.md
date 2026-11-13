# Ejercicio 02 — Las reglas del grafo

⏱️ 1 h 30 min · Teoría previa: 01 y 04

## Objetivo

Gradle deja declarar cualquier dependencia entre módulos mientras no haya un ciclo, y ni siquiera los ciclos los ve hasta que construye. Las reglas de la Bitácora (nadie depende de la app, las features no se conocen, `core` no sabe de features, el modelo no depende de nada, las features no tocan la base de datos) son tuyas. Al terminar, un convention plugin las comprueba en todos los módulos, también en los que se añadan después, y para el build con un mensaje que dice qué regla se ha roto.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM cuyos tests (`src/test/kotlin/RulesTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal, añaden una dependencia a un módulo y ejecutan Gradle con TestKit, como en la semana 11.

`sandbox/` es el grafo de la Bitácora, sin código: `:core:model`, `:core:database`, `:core:data`, `:feature:map`, `:feature:notes` y `:app`. Cada módulo aplica `bitacora.module`, de `sandbox/build-logic`. Por ahora, el plugin solo aplica `java-library`, y `moduleRule` (en `ModuleRules.kt`) siempre devuelve `null`. Compila, y al empezar fallan 7 de 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué pasan ya `theGraphOfTheBitacoraIsAllowed`, `aFeatureCanUseTheModel` y `theAppCanSeeTheDatabase`? ¿Qué probaría un test que solo comprueba que el build pasa, si la regla no existe?
2. **Las reglas** (`moduleRule(from, to)`): recibe dos rutas (`:feature:map`, `:core:data`) y devuelve `null` si la dependencia se puede declarar, o un mensaje si no. Los mensajes empiezan como buscan los tests: `":feature:map no puede depender de :feature:notes"`, `":core:model no depende de nada"`. Mira `RulesTest`.
3. **El orden importa**: `:core:model` → `:app` rompe dos reglas. ¿Cuál debería ganar? Ordena el `when` para que el mensaje sea el más útil.
4. **El plugin** (`bitacora.module.gradle.kts`): para cada configuración (`configurations.configureEach`), para cada `ProjectDependency` que se declare (`dependencies.withType<ProjectDependency>().configureEach`), pregunta a `moduleRule(project.path, path)` y, si devuelve un mensaje, lanza una `GradleException` con él (teoría 04, apartado 4).
5. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la regla del modelo y, en una copia de `sandbox/`, añade `implementation(project(":core:database"))` a `core/model/build.gradle.kts`. Ejecuta `gradle help` y después `gradle build` (con el `gradlew` de `starter/` y `-p`). ¿Cuál falla, con qué mensaje, y por qué el otro no? Restáurala.
   - (b) `theTestsFollowTheRulesToo` usa `testImplementation`. ¿Por qué no hace falta nada especial para que la regla la vea? ¿Qué cambiaría si, en vez de `configurations.configureEach`, miraras solo `configurations.named("implementation")`?
   - (c) El mensaje del error no dice en qué archivo ni en qué línea está la dependencia. ¿Por qué? ¿Qué añadirías al mensaje para que se encuentre igual?
   - (d) `theRulesKeepTheConfigurationCache` pasa. ¿Cuándo corren las reglas: al configurar o al ejecutar? ¿Qué pasa con la caché de configuración si cambias el `build.gradle.kts` de un módulo?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| Pasan las reglas en un test tuyo, pero en el sandbox no salta ninguna | El plugin no llama a `moduleRule`: falta el paso 4 |
| `Unresolved reference 'dependencyProject'` | Gradle 9 lo quitó (en blogs viejos sale `dependencyProject.path`): la ruta es `path`, del propio `ProjectDependency`. `project.path` es la del módulo que la declara |
| `theModelDependsOnNothing` falla porque el build pasa | «core depende de core» se puede, salvo desde `:core:model`: falta esa regla |
| `Circular dependency between the following tasks` | Gradle ve el ciclo al construir; la regla del modelo lo para antes, al configurar |
| `aNewModuleFollowsTheRules` falla | El módulo nuevo aplica `bitacora.module`: si la regla no salta, el plugin mira solo algunas configuraciones o algunos módulos |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.
