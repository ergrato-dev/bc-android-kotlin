# Diseñar y probar un build de varios módulos

> Un build con módulos se rompe de formas nuevas: un ciclo, una dependencia que se filtra, una versión duplicada. Todo eso se puede **comprobar con tests** que ejecutan Gradle (TestKit), igual que la semana pasada.

## 🎯 Objetivos

- Probar la estructura y las dependencias de un build con TestKit
- Leer un informe de dependencias para comprobar `api` e `implementation`
- Aplicar una lista de comprobación a un build de varios módulos

## 1. Lo que se prueba

| Quieres saber | Cómo lo compruebas |
| --- | --- |
| Los módulos existen | `projects` y buscas `Project ':core'` |
| El programa funciona entero | `:app:run -q` y comparas la salida con el texto **escrito a mano** |
| El orden de construcción | `result.tasks.map { it.path }`: `:core:compileKotlin` antes que `:data:compileKotlin` |
| Un módulo no arrastra a los demás | `:data:test` ejecuta tareas de `core` y ninguna de `:app:` |
| `core` no depende de nadie | `:core:dependencies --configuration runtimeClasspath` no contiene `project '` |
| El detalle no se filtra | En `:app:dependencies --configuration compileClasspath` **no** está `kotlinx-serialization`; en `runtimeClasspath` **sí** |
| Un cambio interno no recompila | Cambias un cuerpo en `core` y `data` queda `UP_TO_DATE` |
| Una versión, una vez | Lees `libs.versions.toml` y cuentas cada versión |
| Los repositorios están centralizados | Añades `repositories { }` a un módulo: `buildAndFail()` y el mensaje |
| Los ciclos se rechazan | Añades la dependencia inversa y compruebas `Circular dependency` |

## 2. Informe de dependencias

```bash
./gradlew :app:dependencies --configuration runtimeClasspath
```

```
runtimeClasspath - Runtime classpath of 'main'.
\--- project ':data'
     +--- project ':core'
     +--- org.jetbrains.kotlinx:kotlinx-serialization-bom:1.12.0
     |    +--- org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0 (c)
     ...
     \--- org.jetbrains.kotlinx:kotlinx-serialization-json -> 1.12.0
```

- `project ':core'`: una dependencia a otro módulo.
- `a -> b`: la versión pedida se resolvió a otra (aquí, sin versión, a la del BOM).
- `(c)`: una restricción (*constraint*) del BOM, no una dependencia nueva.

Si necesitas saber **por qué** una biblioteca está, `./gradlew :app:dependencyInsight --dependency <nombre> --configuration runtimeClasspath` muestra el camino.

## 3. Lista de comprobación

- [ ] `settings.gradle.kts` incluye todos los módulos y centraliza los repositorios
- [ ] Ninguna versión escrita a mano en un `build.gradle.kts`; cada una, una vez, en el catálogo
- [ ] Plugins declarados en el raíz (`apply false` si no los necesita)
- [ ] `core` no depende de nadie; las dependencias van hacia `core`
- [ ] `api` solo donde un tipo sale en la API pública
- [ ] Ningún ciclo
- [ ] `./gradlew build` pasa y, repetido, casi todo es `UP-TO-DATE`
- [ ] Tests por módulo, y tests del build (estructura, dependencias)

## 4. Errores frecuentes

| Síntoma | Causa |
| --- | --- |
| `Cannot access class 'X'` en un módulo que no declara nada raro | La dependencia está como `implementation` en el módulo del medio; el tipo sale en su API: `api` |
| `Circular dependency between the following tasks` | Dos módulos se necesitan; mueve lo común a un tercero |
| `Unresolved reference` en `libs.…` | El nombre del catálogo tiene un guion donde el acceso lleva punto, o está mal escrito |
| `The Kotlin Gradle plugin was loaded multiple times` | Falta declarar el plugin en el raíz (`apply false`) |
| `prefer settings repositories over project repositories` | Un módulo declara su `repositories { }`; bórralo, ya están en `settings` |
| Se recompila todo con un cambio mínimo | El cambio altera la interfaz pública de `core` (una función nueva, un tipo cambiado) |

## ✅ Comprueba

- [ ] ¿Cómo compruebas con un test que `kotlinx-serialization` no se filtra al módulo `app`?
- [ ] ¿Qué significa `(c)` en un informe de dependencias?
- [ ] ¿Qué ejecuta `./gradlew :data:test` y qué no?
