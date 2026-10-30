# Semana 20 — Variantes, flavors y firma

> Una app no se construye de una sola forma. La de debug se tiene que poder instalar al lado de la publicada; la demo que enseñas en una feria no es la que usa un agricultor; la que llega a Google Play va firmada con una clave que nadie más tiene. Esta semana el build de la Bitácora aprende a construir varias apps desde el mismo código, a decidir cuáles existen, y a firmar la publicada sin que la clave ni sus contraseñas pasen por el repositorio. Con ella se cierra la fase 3.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Configurar** tipos de build y flavors, y saber qué variantes salen y cómo se llaman sus tareas
- **Separar** código, recursos y tests por variante, sabiendo qué recurso gana y por qué el código no se tapa
- **Usar** `BuildConfig` y la API de variantes para quitar variantes y elegir la que miran `lint` y `check`
- **Firmar** el release con una clave de subida que llega de fuera del repositorio
- **Comprobar** en el APK su `applicationId`, su versión y con qué clave está firmado

---

## 📋 Prerrequisitos

- [Semana 19](../week-19-permisos_de_camara_y_ubicacion/README.md) completada.
- El SDK de Android y `ANDROID_HOME`, con `build-tools` 36.0.0 ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)). `keytool` viene con el JDK.

---

## 🗂️ Estructura de la Semana

```
week-20-variantes_flavors_y_firma/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: Robolectric por variante, y TestKit con apksigner
├── 3-proyecto/                  # la capa 19 de tu Bitácora: dos sabores, y firmada
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Tipos de build](1-teoria/01-tipos-de-build.md) | `debug` y `release`, `applicationIdSuffix`, `namespace` frente a `applicationId` | 30 min |
| [02 · Flavors y variantes](1-teoria/02-flavors-y-variantes.md) | Dimensiones, la matriz, los nombres de las tareas, `beforeVariants`, `isDefault` | 40 min |
| [03 · Source sets por variante](1-teoria/03-source-sets-por-variante.md) | Las carpetas de cada variante, qué recurso gana, el código que no se tapa, `testDemo` | 40 min |
| [04 · BuildConfig y la API de variantes](1-teoria/04-buildconfig-y-la-api-de-variantes.md) | `buildConfigField`, lo que trae siempre, `beforeVariants` y `onVariants` | 30 min |
| [05 · Firma](1-teoria/05-firma.md) | La clave de debug, `apksigner`, la clave de subida, las propiedades, la firma de Google Play | 50 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Dos sabores](2-practicas/ejercicio-01-dos-sabores/README.md) | Una demo y una app de campo con su `applicationId`, sus textos, sus ejemplos y su servidor, probadas en cada variante | 2 h |
| [02 · Firmar sin secretos](2-practicas/ejercicio-02-firmar-sin-secretos/README.md) | El release firmado con una clave que llega por `-P` o por el entorno, comprobado con `apksigner` | 2 h |

### Proyecto

La Bitácora en dos sabores, y firmada: la de debug al lado de la publicada, una demo con observaciones de ejemplo, sin `demoRelease`, y el release firmado con tu clave de subida.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 10 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 1 h 20 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver las variantes y sus tareas | `./gradlew :app:tasks --all \| grep -E "^(assemble\|test\|lint)"` |
| Ver con qué clave firma cada variante | `./gradlew :app:signingReport` |
| Ver la firma de un APK | `$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs -v app-field-release.apk` |
| El SHA-256 de tu clave | `keytool -list -v -keystore ~/claves/bitacora-upload.jks` |
| Ejecutar los tests de una sola variante | `./gradlew :app:testFieldDebugUnitTest --tests '*PlaceAndPhotoTest'` |
| Qué variante mira `check` | `./gradlew :app:check --dry-run \| grep lint` |
| Instalar una variante en un móvil conectado | `./gradlew :app:installDemoDebug` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 12 de 12 en verde (6 por variante) y `NOTAS.md` con el paso 8
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con el paso 5
3. ✅ Proyecto: rama `week-20` con los tipos de build, los flavors, la demo, la firma desde propiedades y al menos 6 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-20` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y los tests empiezan en rojo, salvo `theDebugBuildKeepsTheDebugKey` en el ejercicio 02 y los tests de las semanas anteriores en el proyecto, que empiezan en verde.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 19](../week-19-permisos_de_camara_y_ubicacion/README.md) | **Semana 20** | Semana 21 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
