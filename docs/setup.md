# Preparar tu máquina

Para la semana 1 necesitas solo tres cosas. Android Studio y el SDK de Android llegan en la fase 3 ([más abajo](#fase-3-el-sdk-de-android)).

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

## Fase 3: el SDK de Android

Desde la semana 15 los builds necesitan el SDK de Android. Android Studio lo instala solo, pero los starters se construyen y se prueban desde la terminal, y así lo hace también el CI. Versiones en [stack-versions.md](stack-versions.md).

1. Descarga las *Command line tools only* de <https://developer.android.com/studio#command-line-tools-only> y descomprímelas en `cmdline-tools/latest` dentro de la carpeta del SDK:

   ```bash
   export ANDROID_HOME="$HOME/.local/android-sdk"
   mkdir -p "$ANDROID_HOME/cmdline-tools"
   unzip commandlinetools-*.zip -d "$ANDROID_HOME/cmdline-tools"
   mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
   ```

2. Añade las variables a tu shell (`~/.zshrc` o `~/.bashrc`), para que `./gradlew` las herede:

   ```bash
   export ANDROID_HOME="$HOME/.local/android-sdk"
   export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
   ```

3. Acepta las licencias e instala los paquetes de la semana 15:

   ```bash
   sdkmanager --licenses
   sdkmanager "platforms;android-37.0" "build-tools;36.0.0" "platform-tools"
   sdkmanager --list_installed
   ```

Si ya tienes Android Studio, su SDK sirve: apunta `ANDROID_HOME` a la carpeta que muestra en *Settings > Languages & Frameworks > Android SDK*. No uses `local.properties` para esto: tiene una ruta de tu máquina y no va al repositorio.

Si `./gradlew` dice `SDK location not found` después de definir la variable, el daemon de Gradle arrancó sin ella: `./gradlew --stop` y vuelve a ejecutar.

El emulador necesita virtualización por hardware (KVM en Linux, `/dev/kvm`). Si tu máquina no la tiene (algunas máquinas virtuales y WSL2 sin configurar), usa un móvil con la depuración USB activada y `adb`; nada de lo que se evalúa necesita emulador.
