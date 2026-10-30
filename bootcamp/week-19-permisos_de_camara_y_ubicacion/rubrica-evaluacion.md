# Rúbrica de Evaluación — Semana 19: Permisos de cámara y ubicación

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora sabe dónde y lo ve |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre un permiso normal, uno peligroso y uno de firma? Pon un ejemplo de cada uno que esté en un APK de esta semana. | 10 |
| 2 | ¿Qué devuelve `shouldShowRequestPermissionRationale` antes de pedir, después de un "no" y después de dos? ¿Cómo distingue la app "no esta vez" de "no para siempre"? | 10 |
| 3 | ¿Qué hace la app cuando el usuario ha dicho "no" para siempre? | 10 |
| 4 | ¿Por qué se piden a la vez la ubicación aproximada y la precisa? ¿Qué hace la Bitácora si solo le dan la aproximada? | 10 |
| 5 | ¿Por qué lint no acepta una comprobación de permiso hecha en otra función, y qué pasaría en un móvil sin ninguna comprobación? | 10 |
| 6 | ¿Por qué la Bitácora no declara `CAMERA`? ¿Qué pasaría si lo declarase y abriese la cámara sin pedirlo? | 10 |
| 7 | ¿Qué hace `FileProvider`, y por qué no vale una `Uri` `file://`? | 10 |
| 8 | Android mata la Bitácora mientras el usuario hace la foto. ¿Qué tiene que estar guardado, y dónde, para no perderla? | 10 |
| 9 | Tu APK lleva `READ_CONTACTS` y no lo usas. ¿Cómo encuentras quién lo trae y cómo lo quitas? | 10 |
| 10 | ¿Qué son las funciones implícitas de `aapt2 dump badging`, y por qué no basta con `android.hardware.camera.any` como no obligatoria? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Normal: Android lo concede al instalar (`INTERNET`, que trae el SDK del ejercicio 02). Peligroso: lo concede el usuario en ejecución, con un diálogo (`ACCESS_FINE_LOCATION`). De firma: solo lo tienen las apps firmadas con la misma clave que la que lo define (`dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, de `androidx.core`, con `protectionLevel` `0x2`).
2. Antes de pedir, `false`; después de un "no", `true`; después de dos (Android 11 o superior), `false` otra vez. Por eso se mira en el callback, después de una denegación: `true` es "no esta vez", y `false`, "no para siempre".
3. Ya no puede preguntar: `launch` responde que no sin enseñar el diálogo. Explica que el permiso está apagado y ofrece abrir los ajustes de la app (`ACTION_APPLICATION_DETAILS_SETTINGS` con `package:dev.bitacora.agro`). La app sigue funcionando sin ubicación.
4. Porque desde Android 12 el usuario elige entre precisa y aproximada en el mismo diálogo, y Android 12 ignora una petición de la precisa sola. Con solo la aproximada, la Bitácora usa el proveedor de red, con una precisión de unos 3 km².
5. Lint busca la comprobación (`checkSelfPermission`) en el mismo método que la llamada protegida; no sigue las llamadas a funciones tuyas. En un móvil, la llamada sin permiso lanzaría `SecurityException` y cerraría la app. En Robolectric, `getCurrentLocation` sin permiso no llama nunca al callback.
6. Porque la foto la hace la app de la cámara, que ya tiene su permiso; con `TakePicture`, la Bitácora no lo necesita. Si lo declarase y el usuario no lo hubiera concedido, abrir `ACTION_IMAGE_CAPTURE` lanzaría `SecurityException`, y habría que pedirlo antes, sin necesitarlo.
7. Convierte un archivo de la app en una `Uri` `content://` (`content://dev.bitacora.agro.files/photos/A-1.jpg`) y da permiso solo a la app que recibe el Intent, solo para ese archivo. Una `Uri` `file://` daría la ruta del archivo, que otra app no puede abrir, y desde Android 7 sacarla de tu app lanza `FileUriExposedException`.
8. La observación viaja en el Intent, y el `requestCode` del lanzador lo guarda el registro (semana 18). Lo que la pantalla añade antes de volver, el nombre de la foto, va en `onSaveInstanceState` y se recupera en `onCreate`. El resultado de `TakePicture` llega al callback de la pantalla recreada.
9. En el informe de la fusión (`manifest-merger-blame-debug-report.txt`), la línea del permiso dice qué manifiesto la trajo (`[:sdk]`). Se quita desde el tuyo con `<uses-permission android:name="android.permission.READ_CONTACTS" tools:node="remove" />`.
10. Son las funciones de hardware que Google Play deduce de los permisos: `CAMERA` implica `android.hardware.camera`, y los de ubicación, `android.hardware.location`, obligatorias. Una app que las tiene obligatorias no aparece en los móviles que no las tienen. Solo un `uses-feature` con el mismo nombre (`android.hardware.camera`) y `required="false"` quita la implícita; con `camera.any`, `aapt2` sigue mostrando `uses-feature: name='android.hardware.camera'`.

**Ejercicio 01, paso 7**: (a) `Error: Call requires permission which may be rejected by user ... [MissingPermission]`. Lint no sigue la llamada a `granted()`: busca `checkSelfPermission` en el mismo método. (b) Lint no dice nada. Android 12 ignoraría la petición: hay que pedir las dos. (c) Falla `whenTheUserNeedsAReasonItExplainsFirst` con `expected:<[android.permission.ACCESS_COARSE_LOCATION, android.permission.ACCESS_FINE_LOCATION]> but was:<null>`: la app explica cada vez y nunca llega a pedir. El usuario quedaría atrapado en la explicación. (d) Siguen en verde, y lint también: Robolectric no comprueba el manifiesto. En un móvil, el permiso que no está declarado se deniega sin diálogo. (e) `No location yet`: en Robolectric el proveedor de red empieza apagado, y `getCurrentLocation` responde enseguida con `null`.

**Ejercicio 02, paso 7**: (a) Vuelve `android.permission.READ_CONTACTS`. El informe de la fusión lo atribuye a `[:sdk]` y a su `AndroidManifest.xml`. (b) `aapt2` muestra `uses-feature-not-required: name='android.hardware.camera.any'`, pero también `uses-feature: name='android.hardware.camera'` con `uses-implied-feature ... reason='requested android.permission.CAMERA permission'`. Las tablets sin cámara trasera no la verían. (c) `android.hardware.camera`, por `CAMERA`, y `android.hardware.location`, por los dos permisos de ubicación; además, `android.hardware.faketouch`, que es de todas las apps. (d) `INTERNET` es un permiso normal, que Android concede al instalar; `CAMERA` es peligroso. `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` lo declara `androidx.core`, con `protectionLevel` de firma: solo lo tiene la Bitácora. (e) `SecurityException` al lanzar `ACTION_IMAGE_CAPTURE`. Sin `CAMERA` en el manifiesto, `TakePicture` funciona sin pedir nada.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Pedir permiso (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Lanzador como propiedad; comprobación en el mismo método; `lintDebug` en verde sin `@SuppressLint` (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Permisos en el APK (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| `:sdk` sin tocar; `file_paths.xml` con una sola carpeta (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora sabe dónde y lo ve

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: ubicar con y sin permiso, denegado, la foto, cancelada, muerte del proceso, `data`, el APK | 25 |
| Permisos | Solo los de ubicación; pedidos al pulsar, los dos a la vez; la app funciona sin ellos; ubicación no obligatoria en `badging()` | 20 |
| Foto | `TakePicture` con `FileProvider` sin exportar y una sola carpeta; sin `CAMERA`; la foto sobrevive a la muerte del proceso | 15 |
| Adaptación al dominio | Textos en el idioma del dominio y en el segundo idioma; README con lo que pide el paso 12 | 20 |
| Entrega | Rama `week-19`, pull request con `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `@SuppressLint("MissingPermission")` o una llamada sin comprobar | -10 |
| Permisos pedidos al abrir la app | -10 |
| `CAMERA` declarado sin usarlo | -10 |
| `core` o `data` dependen de Android | -10 |
| Un sha256 añadido o cambiado a mano, o `lenient` | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-19` → `main` en el repositorio del aprendiz.
