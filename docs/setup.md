# Preparar tu máquina

Para la semana 1 necesitas solo tres cosas. Android Studio y el SDK de Android llegan en la fase 3.

| Qué | Versión | Cómo comprobarlo |
|---|---|---|
| JDK | 21 (LTS). Usamos Temurin 21 | `java -version` debe decir `21.x` |
| Git | cualquiera reciente | `git --version` |
| Un editor con soporte de Kotlin | IntelliJ IDEA Community o Android Studio | abre un `starter/` y no ves errores en rojo |

**No instales Gradle ni Kotlin por separado.** Cada `starter/` trae su `gradlew` (el *wrapper*), que descarga la versión exacta de Gradle (9.8.2) y comprueba su suma SHA-256. Kotlin lo trae el plugin de Gradle.

## JDK 21

Descarga Temurin 21 desde <https://adoptium.net/temurin/releases/> o instálalo con tu gestor de paquetes. Después:

```bash
java -version      # openjdk version "21.x.x"
echo $JAVA_HOME    # opcional; si lo defines, debe apuntar al JDK 21
```

Si tienes varios JDK, `./gradlew` usa el del `PATH`/`JAVA_HOME` para arrancar y el *toolchain* (`jvmToolchain(21)`) para compilar. Si no encuentra un JDK 21, falla con un mensaje que lo dice.

## Primera comprobación

```bash
cd bootcamp/week-01-kotlin_toolchain_y_tipos/2-practicas/ejercicio-01-hola-toolchain/starter
./gradlew --version
```

La primera vez descarga Gradle (unos 130 MB). Debe mostrar `Gradle 9.8.2`, `Kotlin 2.4.21` y tu JVM.
