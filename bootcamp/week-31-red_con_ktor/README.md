# Semana 31 — Red con Ktor client

> En la semana 30, las observaciones pasaron a Room y la base de datos quedó como fuente de verdad. Esta semana la Bitácora sale a la red: un botón «Actualizar» pide al servidor las observaciones del equipo y las deja en la base de datos, y la pantalla las ve llegar por el mismo `Flow` de siempre. El cliente es Ktor 3, con kotlinx-serialization para el JSON, tiempos máximos para que nada espere para siempre y errores con nombre propio. La red se prueba sin red: `MockEngine` contesta desde el propio test, y los tiempos agotados pasan en tiempo virtual.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** qué necesita una app Android para hablar HTTP, qué es un motor de Ktor y en qué se diferencia Ktor client de Retrofit
- **Configurar** un `HttpClient` con `ContentNegotiation`, `defaultRequest`, `expectSuccess` y `HttpTimeout`, y leer y mandar JSON con clases `@Serializable`
- **Convertir** las excepciones de la red en errores de la app (sin red, tiempo agotado, error del servidor, respuesta ilegible) sin tragarse la cancelación, y reintentar solo lo que tiene arreglo
- **Integrar** la red en la Bitácora: el cliente en el grafo de Hilt, la URL del servidor por flavor, y lo que llega, a Room
- **Probar** la red con `MockEngine`: lo que se pide, lo que se contesta, los errores y los tiempos, en milisegundos

---

## 📋 Prerrequisitos

- [Semana 30](../week-30-room_y_migraciones/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los flavors de la semana 20, Hilt de la semana 29, Room de la semana 30, y HTTP básico (métodos, códigos de estado, cabeceras, JSON).

---

## 🗂️ Estructura de la Semana

```
week-31-red_con_ktor/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el parte del tiempo, y cuando la red falla
├── 3-proyecto/                  # la capa 30 de tu Bitácora: las observaciones del equipo
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · HTTP en Android y Ktor](1-teoria/01-http-en-android-y-ktor.md) | El permiso, el hilo y el texto en claro; Ktor, sus motores y sus plugins; Ktor o Retrofit | 35 min |
| [02 · El cliente y el JSON](1-teoria/02-el-cliente-y-el-json.md) | `HttpClient`, `ContentNegotiation`, `defaultRequest`; clases `@Serializable`, `@SerialName` y los campos que no se conocen; GET con parámetros y POST con cuerpo | 45 min |
| [03 · Errores, tiempos y reintentos](1-teoria/03-errores-tiempos-y-reintentos.md) | `expectSuccess` y sus excepciones; los tres tiempos de `HttpTimeout`; errores con nombre; la cancelación; `HttpRequestRetry` | 45 min |
| [04 · La red en la Bitácora](1-teoria/04-la-red-en-la-bitacora.md) | Tres clases para una observación; la red escribe en Room; el cliente en Hilt; la URL por flavor; lo que añade al APK | 40 min |
| [05 · Probar la red](1-teoria/05-probar-la-red.md) | `MockEngine`; mirar la petición; contestar errores; el tiempo virtual; la pantalla sin red | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El parte del tiempo](2-practicas/ejercicio-01-el-parte-del-tiempo/README.md) | Un cliente para un servicio del tiempo: los nombres del servidor, los campos que faltan o sobran, la clave en una cabecera, parámetros en la URL y un aviso por POST | 2 h |
| [02 · Cuando la red falla](2-practicas/ejercicio-02-cuando-la-red-falla/README.md) | Cada fallo con su nombre, un tiempo máximo, y reintentos con espera exponencial solo para los 5xx, todo en tiempo virtual | 2 h |

### Proyecto

«Actualizar» en la Bitácora: pide al servidor de su flavor las observaciones del equipo y las guarda en Room, donde las del servidor pisan a las de aquí con el mismo código y las demás se quedan. Mientras tanto, el botón espera; al terminar, un aviso dice cuántas han llegado o qué ha fallado, con palabras de la app.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver qué pide el cliente, en un test | En el `MockEngine`, guarda cada `HttpRequestData` en una lista: `url`, `method`, `headers` y `body` |
| Ver el cuerpo de un POST | `request.body.toByteArray().decodeToString()` (de `io.ktor.client.engine.mock`) |
| La URL de cada flavor | `app/build/generated/source/buildConfig/field/debug/dev/bitacora/BuildConfig.java` |
| Probar contra un servidor de verdad | Cambia la URL del flavor por la tuya; con `http://` hace falta un `network_security_config` (teoría 01) |
| Ver la red del móvil | En Android Studio, *App Inspection* → *Network Inspector*, con la app en marcha (debug) |
| Un test de la red, solo | `./gradlew :data:test --tests '*NetworkTest.aSlowServerTimesOut'` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 8 de 8 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 9 de 9 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-31` con la red en `data` (clases del servidor, `bitacoraClient`, `ObservationApi`, `toNetworkError` y `refresh`), el cliente en `AppModule` con OkHttp y la URL del flavor, «Actualizar» con su aviso, los tests de la semana 30 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-31` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida para el proyecto:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 30](../week-30-room_y_migraciones/README.md) | **Semana 31** | [Semana 32](../week-32-workmanager_y_offline_first/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
