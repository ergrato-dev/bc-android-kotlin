# Glosario — Semana 15

| Término | Definición |
| --- | --- |
| **aapt2** | Compilador de recursos de Android; AGP lo descarga de `google()` cuando compila recursos |
| **Actividad** (*Activity*) | Una pantalla de la app; Android la crea y llama a `onCreate` |
| **addGeneratedSourceDirectory** | Método de la Variant API que conecta la salida de una tarea a las fuentes de una variante (assets, `res`, `java`...) |
| **AGP** (*Android Gradle Plugin*) | Plugin de Gradle que construye apps y bibliotecas Android: `com.android.application`, `com.android.library` |
| **ANDROID_HOME** | Variable de entorno con la carpeta del SDK de Android; AGP la usa para encontrarlo |
| **androidComponents { }** | Extensión de AGP con la Variant API: `onVariants`, `beforeVariants`, `selector()` |
| **APK** | El paquete de la app: un zip con `classes*.dex`, manifiesto, recursos, assets y firma |
| **apkanalyzer** | Herramienta de `cmdline-tools` que inspecciona un APK desde la terminal: manifiesto, dex, archivos |
| **applicationId** | Identidad de la app en el móvil y en Google Play; no se cambia después de publicar |
| **assets** | Archivos que viajan tal cual en el APK y se leen con `assets.open(...)` |
| **buildEnvironment** | Tarea que imprime el classpath de los plugins del build, como `dependencies` para el código |
| **compileSdk** | Versión de la API de Android contra la que se compila |
| **d8** | Herramienta que convierte bytecode de la JVM en dex |
| **dex** | Formato del código que ejecuta Android (`classes.dex`) |
| **exported** (`android:exported`) | Atributo del manifiesto que dice si otras apps pueden abrir un componente; obligatorio con `intent-filter` desde Android 12 |
| **google()** | El repositorio Maven de Google: AGP, AndroidX y `aapt2` |
| **intent-filter** | Declaración de los *intents* a los que responde un componente; `MAIN` + `LAUNCHER` lo ponen en el lanzador |
| **lint** | Análisis estático de Android que se ejecuta dentro de `build`; los avisos no paran el build, los errores sí |
| **Manifest merger** | Paso de AGP que fusiona tu manifiesto con los de las bibliotecas y los valores del build |
| **minSdk** | Versión de Android más vieja en la que se puede instalar la app |
| **namespace** | Paquete donde AGP genera `R` y contra el que se resuelven los nombres cortos del manifiesto |
| **onVariants** | Punto de la Variant API que se ejecuta una vez por variante, con la variante ya configurada |
| **resources.arsc** | Tabla de recursos compilados dentro del APK |
| **sdkmanager** | Herramienta de `cmdline-tools` que instala y lista paquetes del SDK |
| **targetSdk** | Versión de Android con la que la app declara estar probada; activa los comportamientos nuevos |
| **Tipo de build** (*build type*) | Cómo se construye una variante: `debug` o `release` de serie |
| **Variante** | Combinación de tipo de build y *flavors*; cada una tiene sus tareas y su APK |
| **Variant API** | API de AGP para leer y cambiar las variantes desde el build sin depender de sus tareas internas |
| **versionCode** | Entero que solo sube; Android lo usa para saber si una instalación es una actualización |
| **versionName** | Versión que ve el usuario (`1.0.0`) |
