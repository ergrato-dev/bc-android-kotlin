# Rúbrica de Evaluación — Semana 20: Variantes, flavors y firma

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora en dos sabores, y firmada |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre un tipo de build y un flavor? Pon un ejemplo de cada uno en la Bitácora. | 10 |
| 2 | ¿Qué `applicationId` y qué `versionName` tiene `demoDebug`? ¿En qué orden se suman los sufijos? | 10 |
| 3 | ¿Qué diferencia hay entre `namespace` y `applicationId`? ¿Cuál devuelve `packageName`? | 10 |
| 4 | Añades flavors y `./gradlew :app:lintDebug` deja de funcionar. ¿Por qué? ¿Qué variante mira `./gradlew check`, y cómo eliges otra? | 10 |
| 5 | `app_name` está en `src/main/res/values`, `src/main/res/values-en` y `src/demo/res/values`. ¿Cómo se llama la demo en un móvil en inglés? ¿Por qué? | 10 |
| 6 | ¿Por qué una clase puede estar en `src/demo` y en `src/field`, pero no en `src/main` y en `src/demo`? | 10 |
| 7 | ¿Para qué sirve `beforeVariants`, y por qué no se hace lo mismo con `onVariants`? | 10 |
| 8 | ¿Cómo compruebas con qué clave está firmado un APK, y que esa clave es la tuya? | 10 |
| 9 | ¿Por dónde llegan las contraseñas de la firma al build, en tu máquina y en un servidor de integración continua? ¿Por qué el release sale sin firmar en vez de fallar cuando no están? | 10 |
| 10 | ¿Qué diferencia hay entre la clave de subida y la clave de firma de la app en Google Play? ¿Qué pasa si pierdes cada una? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Un tipo de build cambia cómo se construye la misma app: `debug` se puede depurar, se firma con la clave de debug y lleva `.debug`; `release` es la que se publica. Un flavor cambia qué app se construye: `demo` arranca con observaciones de ejemplo y se llama "Bitácora demo"; `field` es la de verdad.
2. `dev.bitacora.agro.demo.debug` y `1.0.0-demo-debug`. Primero el sufijo del flavor y después el del tipo de build.
3. `namespace` es el paquete de Kotlin de `R` y `BuildConfig` (`dev.bitacora`), y no cambia con la variante. `applicationId` es la identidad de la app en el móvil y en Google Play, y lleva los sufijos. `packageName` devuelve el `applicationId`.
4. Con flavors hay dos variantes de debug, y Gradle no sabe cuál es `lintDebug`: `'lintDebug' is ambiguous ... Candidates are: ... 'lintDemoDebug', ... 'lintFieldDebug'`. `check` y `lint` miran una sola variante: sin decirle nada, AGP eligió `demoDebug`. Con `isDefault = true` en `field`, miran `fieldDebug`.
5. `Field log`, el de `main`. El orden de las carpetas decide entre recursos con los mismos calificadores, y un calificador que coincide con el móvil (`en`) pesa más que la carpeta. La demo necesita también `src/demo/res/values-en`.
6. Las carpetas de `demo` y de `field` no se compilan nunca juntas: cada variante tiene uno solo de los dos flavors. `main` se compila con todas, y el código no se tapa como los recursos: dos clases con el mismo nombre en la misma compilación dan `Redeclaration`.
7. `beforeVariants` actúa antes de crear la variante, cuando aún se puede decidir si existe (`enable = false`), y con ella desaparecen sus tareas. `onVariants` recibe la variante ya creada: sirve para cambiar sus fuentes, assets o campos, no para quitarla.
8. Con `apksigner verify --print-certs -v`, que da el DN y el SHA-256 del certificado. Ese SHA-256 se compara con el de `keytool -list -v` sobre tu almacén: si coinciden, la clave es la tuya. La de debug tiene el DN `C=US, O=Android, CN=Android Debug`.
9. Como propiedades de Gradle: en tu máquina, en `~/.gradle/gradle.properties`, o una vez con `-P`; en un servidor de integración continua, como variables de entorno `ORG_GRADLE_PROJECT_bitacoraStorePassword`, etc., desde sus secretos. Sin ellas, el release sale sin firmar para que el build funcione en cualquier máquina (la de un compañero, un pull request de fuera), que no tiene por qué tener la clave.
10. La clave de subida firma lo que tú subes a Google Play; la de firma de la app la guarda Google y firma lo que llega a los móviles. Si pierdes la de subida, Play Console la cambia y los usuarios no lo notan. Sin la firma de Google Play, perder la clave de la app es no poder actualizarla nunca.

**Ejercicio 01, paso 8**: (a) Falla `inEnglishToo` en `demoDebug`, con `expected:<Field log[ demo]> but was:<Field log[]>`. `demo` tapa a `main` solo entre recursos con los mismos calificadores; en inglés, `main/values-en` coincide con el móvil y gana a `demo/values`. (b) `Redeclaration:` en `main/.../Samples.kt` y en `demo/.../Samples.kt` al compilar `demoDebug`, y en `main` y `field` al compilar `fieldDebug`: en `fieldDebug` se compilan las dos copias. (c) Sin `isDefault`, `lint` ejecuta `lintDemoDebug`: la app de campo, la que se publica, se queda sin lint. (d) `lintDebug`: `is ambiguous in project ':app'`, con los candidatos. `test --tests`: `Unknown command-line option '--tests'`. Solo los de field: `./gradlew :app:testFieldDebugUnitTest`. (e) Dos: `app-demo-debug.apk` (`dev.bitacora.agro.demo.debug`) y `app-field-debug.apk` (`dev.bitacora.agro.debug`). Se pueden instalar las dos a la vez, y la de release (`dev.bitacora.agro`) a su lado: tres `applicationId` distintos.

**Ejercicio 02, paso 5**: (a) Coinciden: el APK está firmado con el certificado de tu almacén. Es la forma de comprobar que un APK es tuyo. (b) Sin `-P`, `fieldRelease` sale con `Config: none`; con ellos, `Config: release` y `Store:` con la ruta de tu almacén. `fieldDebug` no cambia: `debug.keystore` y `AndroidDebugKey`. (c) v1 y v2. v1 es la firma de Android 6 y anteriores, y con `minSdk = 23` la app se puede instalar en Android 6; con 26, AGP no la necesita. (d) v2 y v3, y un `.idsig` al lado: la firma v4, para instalar mientras se descarga. (e) `withoutTheKeyTheReleaseIsUnsigned`: el build de los tests usa `~/.gradle` como carpeta de Gradle y lee su `gradle.properties`, así que encontraría la clave y firmaría. Las de verdad, en un gestor de contraseñas, y en `~/.gradle/gradle.properties` solo en tu máquina, o en los secretos del servidor de integración continua.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Dos sabores (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 12 de 12 en verde | 13 |
| `Samples` solo en las carpetas de los flavors; `main` no pregunta por la variante; solo `app_name` en los recursos de `demo` (revisión del código) | 3 |
| `NOTAS.md`: paso 8 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Firmar sin secretos (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Ni el almacén ni sus contraseñas en ningún archivo del ejercicio; release firmado con tu clave comprobado con `apksigner` (revisión) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora en dos sabores, y firmada

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: la demo, sus textos, su `packageName`, sin `demoRelease`, `check` en `field`, el release firmado | 25 |
| Variantes | Tres variantes; tipos de build y firma en el plugin de convención, flavors en la app; `field` por defecto; los tests de cada flavor en su carpeta | 20 |
| Firma | Las propiedades de fuera del repositorio; sin ellas, el release sin firmar; `.gitignore` con los almacenes | 15 |
| Adaptación al dominio | Ejemplos y textos de la demo en el idioma del dominio y en el segundo idioma; README con lo que pide el paso 12 | 20 |
| Entrega | Rama `week-20`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una clave o una contraseña en el repositorio, aunque se borre después | -30 |
| `if (BuildConfig.FLAVOR == ...)` en `main` en vez de una clase por flavor | -10 |
| `lint` o `check` en `demoDebug` | -10 |
| `core` o `data` dependen de Android | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

Una clave que ha pasado por el repositorio está en su historia: hay que darla por filtrada, crear otra y, con la firma de Google Play, pedir el cambio de la clave de subida.

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-20` → `main` en el repositorio del aprendiz.
