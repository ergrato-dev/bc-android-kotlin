# Semana 08 — Serialization y DSLs

> Tu Bitácora vive en memoria: al cerrar el programa, todo se pierde. Esta semana aprende a **escribirse y leerse** como JSON, con un formato que pueda crecer sin romper los archivos viejos, y a **construirse con una sintaxis que se lee como una declaración**. Es la última semana de la fase 1 (Kotlin): en la siguiente empieza Gradle.

Todavía no hay red ni base de datos (fase 5): hoy el archivo es un texto que tú produces y consumes. Los DSL de esta semana son los mismos que verás en `build.gradle.kts`.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Configurar** `kotlinx.serialization` (plugin y biblioteca) y convertir objetos en JSON y de vuelta
- **Controlar** el formato con `@SerialName`, `@Transient`, valores por defecto y una única instancia de `Json`
- **Serializar** enums, `value class` y jerarquías `sealed`, y escribir un `KSerializer` propio
- **Leer con tolerancia** un archivo con el árbol `JsonElement`: lo bueno entra, lo malo se explica
- **Evolucionar** un formato con versión, valores por defecto y rechazo de versiones futuras
- **Escribir** lambdas con receptor, un *builder* con validación y `@DslMarker`
- **Probar** formatos y DSLs con textos y mensajes exactos

---

## 📋 Prerrequisitos

- [Semana 07](../week-07-flow_y_programacion_reactiva/README.md) completada.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 7 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-08-serialization_y_dsls/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 7 de tu Bitácora: archivo JSON versionado y DSL
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · JSON con kotlinx.serialization](1-teoria/01-json-con-kotlinx-serialization.md) | Plugin, `@Serializable`, `Json { }`, `@SerialName`, errores | 45 min |
| [02 · Tipos propios y polimorfismo](1-teoria/02-tipos-propios-y-polimorfismo.md) | Enums, `value class`, `sealed`, `KSerializer` | 45 min |
| [03 · JSON dinámico y evolución](1-teoria/03-json-dinamico-y-evolucion.md) | `JsonElement`, lectura tolerante, versiones | 45 min |
| [04 · Lambdas con receptor y DSLs](1-teoria/04-lambdas-con-receptor-y-dsl.md) | *Builders*, `@DslMarker`, operadores | 45 min |
| [05 · Diseñar y probar](1-teoria/05-disenar-y-probar.md) | Pruebas *golden*, tolerancia, lista de comprobación | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · JSON con serialization](2-practicas/ejercicio-01-json-con-serialization/README.md) | Valores por defecto, `@SerialName`, enum, `value class`, `sealed`, errores, `KSerializer` | 2 h |
| [02 · DSL con lambdas con receptor](2-practicas/ejercicio-02-dsl-con-lambdas-con-receptor/README.md) | *Builders*, filtro, informe, operadores y `buildJsonObject` | 2 h |

### Proyecto

La Bitácora se guarda como un archivo JSON **versionado** y se construye con un DSL. La importación es tolerante: lo bueno entra, lo malo se cuenta con su posición y su motivo, y una versión futura se rechaza con claridad.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h |
| Recursos y glosario | 1 h 30 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver cómo queda un objeto en JSON mientras pruebas | `println(Json { prettyPrint = true }.encodeToString(x))` en un `main` temporal |
| Escribir JSON en un test sin escapar comillas | Cadenas de comillas triples `"""..."""` |
| Ver el serializador generado | Tools → Kotlin → Show Kotlin Bytecode sobre la clase `@Serializable` |
| Ver qué receptor tiene cada llamada de un DSL | Ctrl+Shift+P sobre `this` o el gutter de IntelliJ en la lambda |
| Ejecutar un solo test | `./gradlew test --tests "FileTest.theGoldenFile"` |
| Aceptar una API experimental solo donde se usa | `@OptIn(ExperimentalSerializationApi::class)` en la función |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 7 de 7 en verde
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con la respuesta del paso 7
3. ✅ Proyecto: rama `week-08` con `LogbookFile.kt`, `Import.kt`, `Dsl.kt` y al menos 8 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-08` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters compilan y empiezan en rojo (`NotImplementedError`): es lo esperado. Si tu código no compila por `needs opt-in` o `Redundant creation of Json format`, revisa las lecciones 1 y 3.

---

## 🏁 Cierre de la fase 1 (semanas 1 a 8)

Has terminado la fase de Kotlin. Comprueba que dominas esto **sin mirar**:

- [ ] Compilar, probar y leer un `build.gradle.kts` mínimo (S1)
- [ ] Colecciones, lambdas y funciones de orden superior (S3)
- [ ] Modelar con `enum`, `sealed`, `value class` y genéricos (S4-S5)
- [ ] Lanzar, cancelar y probar coroutines con tiempo virtual (S6)
- [ ] Observar estado con `StateFlow` y combinar flujos (S7)
- [ ] Guardar y leer un formato versionado, y construir con un DSL (S8)

Tu repositorio debe tener el `core` de la Bitácora completo: modelo tipado, lectura con motivos, sincronización asíncrona, almacén observable y archivo JSON, todo con tests propios. La **fase 2** convierte ese `core` en un proyecto Gradle de varios módulos; los `build.gradle.kts` que has estado copiando son, ahora, el objeto de estudio.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 07](../week-07-flow_y_programacion_reactiva/README.md) | **Semana 08** | [Semana 09](../week-09-gradle_por_dentro/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
