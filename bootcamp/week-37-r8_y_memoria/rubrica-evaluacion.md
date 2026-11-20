# Rúbrica de Evaluación — Semana 37: R8 y memoria

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora reducida |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué cuatro cosas hace R8? ¿Por qué renombrar reduce el tamaño? ¿Cuánto bajó el APK de la Bitácora, y con cuántas reglas propias? | 10 |
| 2 | ¿Por qué R8 va solo en release? ¿Qué pasa si pones `isShrinkResources` sin `isMinifyEnabled`? ¿Qué es el modo completo, y qué pasa si lo apagas? | 10 |
| 3 | ¿Por qué un fallo de R8 aparece al ejecutar y no al compilar? Da tres casos de código que R8 no puede seguir, y uno que sí ve aunque no haya una llamada (lo que nombra el manifiesto). | 10 |
| 4 | ¿Qué diferencia hay entre `-keep`, `-keepclassmembers` y `-keepnames`? ¿Qué regla escribirías para unas clases que se crean por su nombre, y cuál para unos campos leídos por reflexión? ¿Qué tiene de malo `-keep class dev.bitacora.** { *; }`? | 10 |
| 5 | ¿Dónde lleva sus reglas un AAR, y dónde un JAR? ¿Por qué la regla de una biblioteca que usa reflexión va en la biblioteca? ¿Qué es `consumerProguardFiles`, y en qué se diferencia de `proguardFiles`? | 10 |
| 6 | ¿Qué dicen `seeds.txt`, `usage.txt`, `mapping.txt` y `configuration.txt`? ¿Por qué `SyncWorker` conserva su nombre en la Bitácora, quién escribe esa regla, y qué pasaría sin ella? | 10 |
| 7 | En una línea de método del mapping, ¿qué significan los dos rangos? ¿Qué es el `pg_map_id`, y dónde aparece en la traza? ¿Por qué no hizo falta `-keepattributes LineNumberTable`? ¿Qué hay que guardar al publicar, y dónde lo pone AGP para Play? | 10 |
| 8 | ¿Qué diferencia hay entre `file-size` y `download-size`? ¿Qué cuenta `dex references`, y por qué la Bitácora tenía 4 dex sin R8? ¿Qué hace falta para que un recurso que se busca por su nombre llegue a la release? | 10 |
| 9 | ¿Qué es una fuga de una actividad? ¿Cómo la detecta LeakCanary? ¿Qué hace `DetectLeaksAfterTestSuccess`, por qué va por fuera de la regla de la actividad, y cómo se lee su informe? ¿Qué cambió en el manifiesto de la debug al añadir LeakCanary? | 10 |
| 10 | ¿Qué comprueban `ThreadPolicy` y `VmPolicy`? ¿Qué encontró StrictMode en la Bitácora? ¿Por qué `penaltyLog` y no `penaltyDeath`? Con `dumpsys meminfo`, ¿cuánto ocupan la debug, la release sin R8 y la release con R8, y por qué se mide la release? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Quitar lo que no se alcanza desde los puntos de entrada, optimizar (meter métodos en quien los llama, quitar ramas muertas, juntar clases), renombrar y convertir a dex. El dex guarda cada nombre como texto: `fk` ocupa menos que `dev/bitacora/BitacoraViewModel`, y son decenas de miles. De 13 225 377 a 2 254 321 bytes, de 4 dex a 2 y de 199 762 referencias a 30 326, sin ninguna regla propia (medido).
2. En debug haría cada compilación más lenta, y el depurador vería los nombres cambiados. Lo que rompe se ve en release: por eso `benchmarkRelease` pasa por R8. Sin `isMinifyEnabled`, `Removing unused resources requires unused code shrinking to be turned on`. El modo completo solo conserva lo que la regla dice; el de compatibilidad, más (constructores, atributos). Con `android.enableR8.fullMode=false`, AGP avisa de que está obsoleto, y el APK del ejercicio 02 pasó de 765 225 a 863 529 bytes.
3. R8 compila sin quejarse; lo que falta se busca al ejecutar. Casos: `Class.forName` con un nombre en un archivo, campos leídos por reflexión, un nombre guardado (el worker en la base de datos de WorkManager), JNI. Lo que nombran el manifiesto y los layouts lo conserva AGP con reglas que genera (`Generated ProGuard rules` en `configuration.txt`).
4. `-keep` conserva la clase con su nombre y los miembros que diga. `-keepclassmembers`, solo los miembros, si la clase queda, y la clase puede renombrarse. `-keepnames`, el nombre si la clase queda, pero se puede quitar. Clases por su nombre: `-keep class * implements X { <init>(); }`. Campos: `-keepclassmembers class X { <fields>; }`. `**` con `{ *; }` apaga R8 en todo tu código: ni se quita ni se renombra nada (el ejercicio 01 lo comprueba).
5. AAR: `proguard.txt` en su raíz. JAR: `META-INF/proguard/` o `META-INF/com.android.tools/r8*/`. La biblioteca sabe qué usa por reflexión: si va en la app, cada app que la use lo descubre al fallar. `consumerProguardFiles` mete el archivo en el AAR; `proguardFiles` es para reducir la propia biblioteca, que casi nunca se hace.
6. `seeds.txt`: lo que conservan las reglas. `usage.txt`: lo quitado. `mapping.txt`: el nombre de antes y el de después. `configuration.txt`: todas las reglas, con su origen (58 bloques en la Bitácora). WorkManager guarda el nombre de la clase del worker; su regla (`-keepnames class * extends androidx.work.ListenableWorker`) viene en `work-runtime`. Sin ella, tras actualizar la app, el trabajo pendiente no encontraría la clase.
7. `5:13:void onIntent(...):64:64 -> e`: las líneas 5 a 13 de `e` (las de la traza) son la 64 de `onIntent`. El `pg_map_id` identifica el mapping, y la traza lo lleva donde iba el archivo fuente (`r8-map-id-…`). No hizo falta porque la traza lleva las líneas de después, y el mapping las traduce: retrace dio `Session.kt:71`, la del `error` (medido). Hay que guardar el mapping de cada versión publicada; en el AAB va en `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`.
8. `file-size`, lo que ocupa el archivo; `download-size`, lo que baja comprimido (2 254 321 y 2 070 138 bytes). `dex references` cuenta los métodos que cada dex nombra, suyos y ajenos, y cada dex admite 65 536: 199 762 no caben en menos de 4. Un `res/raw/keep.xml` con `tools:keep`.
9. Algo que vive más (un `object`, una lista de nivel superior) apunta a una actividad destruida y el GC no puede liberarla. LeakCanary vigila lo que se destruye, y si a los 5 s sigue, fuerza el GC, vuelca el heap y busca el camino desde una raíz. La regla busca tras cada test que pasa y falla con el informe; por fuera, para mirar con la actividad ya destruida. Se lee de abajo arriba: `Leaking: YES`, quién lo apunta y lo subrayado. La debug pide además `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` y `POST_NOTIFICATIONS` (medido): por eso cambió `onlyTheExpectedPermissionsShip`.
10. `ThreadPolicy`, el hilo principal (disco, red); `VmPolicy`, el proceso (objetos sin cerrar, fugas). 10 avisos de disco en el hilo principal: `getDatabasePath` al crear la base de datos con el `ViewModel`, y el `ServiceLoader` de Ktor al crear el cliente. `penaltyDeath` cerraría la debug por avisos que hoy no se pueden quitar sin rehacer la inyección. PSS: 97,5 MB, 35,9 MB y 27,2 MB (medido). La debug no es lo que corre en los móviles.

**Ejercicio 01, paso 6**: (a) El programa funciona, pero fallan `helperIsRenamed`, `unusedIsGone` y `observationCanBeRenamed`: nada se renombra ni se quita. (b) Una línea vacía: la clase queda, pero sus campos no (nadie los leía), y el constructor se queda sin argumentos. (c) Sí: `-keep` conserva toda clase que implemente `Sensor`, se use o no. (d) R8 recibe tres JAR con manifiesto: el del programa, `kotlin-stdlib-2.4.21.jar` y `annotations-13.0.jar`; se queda con uno y avisa de los otros dos.

**Ejercicio 02, paso 7**: (a) `Removing unused resources requires unused code shrinking to be turned on`. (b) `The option setting 'android.enableR8.fullMode=false' is deprecated`; el APK pasa de 765 225 a 863 529 bytes: el modo de compatibilidad conserva más. (c) La release funciona (las sondas conservan el nombre), pero fallan `theAppDoesNotKnowAboutTheRule` y `theLibraryShipsItsRule`: cada app que use `:sensors` tendría que saberlo. (d) 104 921 bytes en el emulador de la semana 36; el informe pasa por `static Sensors.listeners` y la lambda de `MainActivity` (`f$0`).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Reglas keep (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Dos reglas: una para todos los sensores, por la interfaz, y `-keepclassmembers` para los campos; ninguna con `**` ni `{ *; }`, comentadas; `./gradlew build` en verde (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con lo que escribe el programa en (b) y lo que hay en el mapping en (c) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Una biblioteca y una fuga (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 en verde | 13 |
| R8 y recursos en release; la regla en `consumer-rules.pro` de `:sensors`, comentada; LeakCanary con `debugImplementation`; `DetectLeaksAfterTestSuccess` en `LeakTest`; la fuga arreglada en `MainActivity` (revisión) | 3 |
| `NOTAS.md`: paso 7 explicado, con el aviso y los tamaños de (b); el paso 6 en un dispositivo hecho o dicho que no | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora reducida

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | Los de la semana 36 y `ShrinkTest` en verde, `onlyTheExpectedPermissionsShip` arreglado sin debilitarlo, y mínimo 5 nuevos con el valor esperado escrito a mano (paso 11) | 25 |
| R8 | En la release desde el plugin de convención, con recursos; `proguard-rules.pro` sin reglas que ningún test pida; lo que deja R8 leído (paso 5) y una traza recuperada (paso 6) | 20 |
| Memoria | LeakCanary solo en debug; `DetectLeaksAfterTestSuccess` en `DeviceTest` con el `order` correcto; StrictMode solo en debug, con `penaltyLog` | 20 |
| Adaptación al dominio | README con lo que pide el paso 12: las reglas que llegan solas, tus números, lo que encontraron StrictMode y LeakCanary (o por qué no hay números) | 15 |
| Entrega | Rama `week-37`, pull request con `test` y tus números | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| R8 en debug | -10 |
| Una regla `{ *; }` sobre `dev.bitacora` | -15 |
| `android.enableR8.fullMode=false` | -10 |
| LeakCanary con `implementation` (llega a la release) | -15 |
| StrictMode encendido en release, o con `penaltyDeath` sin haber quitado sus avisos | -10 |
| El `error` del paso 6 en la rama que se entrega | -20 |
| Un test de la semana 36 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-37` → `main` en el repositorio del aprendiz.
