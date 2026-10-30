# Glosario — Semana 20

| Término | Definición |
| --- | --- |
| **androidComponents** | La API de variantes de AGP, para cambiar las variantes desde el build |
| **apksigner** | Herramienta de `build-tools` que firma un APK y verifica su firma |
| **applicationId** | La identidad de la app en el móvil y en Google Play; es lo que devuelve `packageName` |
| **applicationIdSuffix** | Lo que un tipo de build o un flavor añade al `applicationId` |
| **beforeVariants** | Bloque de `androidComponents` que actúa antes de crear la variante; con `enable = false` la quita |
| **BuildConfig** | Clase que AGP genera con constantes de la variante; en AGP 9, apagada por defecto |
| **buildConfigField** | Añade una constante a `BuildConfig`; su valor se escribe como código Java |
| **Clave de debug** | La de `~/.android/debug.keystore`, que firma los APK de debug de tu máquina |
| **Clave de firma de la app** | Con la firma de Google Play, la que guarda Google y firma lo que llega a los móviles |
| **Clave de subida** | La tuya: firma lo que subes a Google Play |
| **debuggable** | Si se puede conectar un depurador a la app; sí en `debug`, no en `release` |
| **Dimensión** | Grupo de flavors; cada variante tiene un flavor de cada dimensión |
| **Esquema de firma** | Versión del formato de firma: v1 (JAR), v2, v3 y v4 |
| **FLAVOR** | Campo de `BuildConfig` con el nombre del flavor; solo existe si hay flavors |
| **flavorDimensions** | La lista de dimensiones de los flavors del módulo |
| **isDefault** | Marca el flavor o el tipo de build que eligen `lint`, `check` y Android Studio |
| **keytool** | Herramienta del JDK que crea almacenes de claves y muestra sus certificados |
| **Almacén de claves** | Archivo (`.jks`, PKCS12) con claves privadas y sus certificados, protegido por contraseña |
| **namespace** | El paquete de Kotlin de `R` y `BuildConfig`; no cambia con la variante |
| **onVariants** | Bloque de `androidComponents` que recibe cada variante ya creada |
| **ORG_GRADLE_PROJECT_** | Prefijo de las variables de entorno que Gradle lee como propiedades del proyecto |
| **Play App Signing** | La firma de Google Play: Google guarda la clave de la app y tú firmas con la de subida |
| **productFlavors** | Los flavors del módulo: versiones distintas de la app desde el mismo código |
| **selector()** | Elige en `androidComponents` las variantes por flavor, tipo de build o nombre |
| **signingConfig** | La configuración de firma que usa un tipo de build |
| **signingConfigs** | Las configuraciones de firma del módulo: almacén, alias y contraseñas |
| **signingReport** | Tarea que muestra con qué almacén y alias se firma cada variante |
| **Source set** | Carpeta de `src/` con el código, los recursos y el manifiesto de una variante, un flavor o un tipo de build |
| **testDemo / testField** | Source sets de tests de la JVM que solo se ejecutan en un flavor |
| **Tipo de build** | Forma de construir la app: `debug`, `release` o los que declares |
| **Variante** | Una combinación de flavor y tipo de build, con sus tareas: `fieldDebug`, `fieldRelease` |
| **versionNameSuffix** | Lo que un tipo de build o un flavor añade al `versionName` |
| **zipalign** | Herramienta de `build-tools` que alinea un APK; AGP lo deja alineado antes de firmar |
