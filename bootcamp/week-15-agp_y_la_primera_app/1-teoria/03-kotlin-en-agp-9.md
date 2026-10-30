# Kotlin en AGP 9

> Hasta AGP 8, un módulo Android con Kotlin aplicaba dos plugins: el de Android y `org.jetbrains.kotlin.android`. AGP 9 compila Kotlin él solo. Eso cambia cómo se elige la versión de Kotlin del build, y deja más clara una decisión que la Bitácora tomó en la semana 1: lo que no depende de Android se queda en Kotlin puro.

## 🎯 Objetivos

- Explicar qué versión de Kotlin compila una app con AGP 9 y cómo se cambia
- Reconocer el error de aplicar `org.jetbrains.kotlin.android` en AGP 9
- Separar módulos Android y módulos de Kotlin puro, y saber qué gana cada uno

## 1. Kotlin viene dentro

Con AGP 9, un módulo con `com.android.application` ya compila Kotlin: `src/main/kotlin` y `kotlin { jvmToolchain(21) }` funcionan sin más plugins. Si aplicas el de antes, AGP lo rechaza:

```text
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > ⛔ Failed to apply plugin 'org.jetbrains.kotlin.android'
     The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.
     Solution: Remove the 'org.jetbrains.kotlin.android' plugin from this project's build file: app/build.gradle.kts.
```

> 🔬 Medido en AGP 9.4.1 añadiendo `id("org.jetbrains.kotlin.android")` a `:app` (ejercicio 01, paso 5b). Con versión (`alias(...)` desde el catálogo) falla antes y con otro mensaje: el plugin ya está en el classpath por el raíz.

Muchos tutoriales y proyectos de antes de 2026 tienen esa línea. Al migrarlos, se quita.

## 2. Qué versión de Kotlin

AGP depende de una versión del plugin de Kotlin (KGP). Con AGP 9.4.1 es la 2.2.10. Si el build no dice nada más, esa es la que compila tu código.

Para usar la del curso, 2.4.21, basta con que el plugin de Kotlin esté en el classpath del build. El raíz lo declara con `apply false`, junto a AGP:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
```

Es la resolución de conflictos de la semana 14, ahora en el classpath de los plugins: dos versiones de `kotlin-gradle-plugin`, gana la más alta.

```text
\--- com.android.tools.build:gradle:9.4.1
     +--- org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10 -> 2.4.21
```

> 🔬 Medido con `./gradlew buildEnvironment` en el ejercicio 01. Sin la línea `kotlin.jvm apply false`, el árbol dice `kotlin-gradle-plugin:2.2.10` sin flecha y el APK se construye igual, con Kotlin 2.2.10: nada falla, y por eso hay que mirarlo. En la Bitácora el plugin de Kotlin 2.4.21 ya llega al classpath por `build-logic`.

`buildEnvironment` es a los plugins lo que `dependencies` a tu código: el árbol del classpath del build. Úsalo cada vez que subas AGP, para ver qué Kotlin trae.

## 3. Kotlin puro donde se pueda

| | Módulo Android (`com.android.application`) | Módulo Kotlin puro (`kotlin.jvm`) |
| --- | --- | --- |
| Puede usar | Las clases de Android (`Activity`, `Context`, recursos) | Solo Kotlin y bibliotecas JVM |
| Sus tests | En la JVM con `android.jar` de mentira, o en un dispositivo | En la JVM, como en las fases 1 y 2 |
| Tareas de `test` | Las de AGP: manifiesto, recursos, variantes... | Compilar y ejecutar |
| Necesita el SDK | Sí | No |

`core` y `data` de la Bitácora siguen siendo Kotlin puro: el modelo, la validación y la serialización no necesitan Android. `app` depende de ellos con `implementation(project(...))`, como antes, y AGP los mete en el dex.

> 🔬 Medido: `./gradlew :core:test` en la Bitácora no ejecuta ninguna tarea de `:app` (el test `coreStaysOnTheJvm` del proyecto lo comprueba). `:app:tasks --all` lista 286 tareas.

Una cosa cambia: desde una actividad en el paquete `dev.bitacora` no se puede importar una clase del paquete por defecto (sin `package`). Las clases de `core` y `data` pasan a `dev.bitacora.core` y `dev.bitacora.data`. Es un cambio de nombres, no de diseño.

## ✅ Comprueba

- [ ] Quitas `alias(libs.plugins.kotlin.jvm) apply false` del raíz del ejercicio 01. ¿Compila? ¿Con qué versión de Kotlin? ¿Cómo lo compruebas?
- [ ] Un compañero propone pasar `core` a `com.android.library` "por si algún día necesita un `Context`". ¿Qué le contestas?
- [ ] ¿Qué comando usarías para saber si una subida de AGP cambia la versión de Kotlin del build?
