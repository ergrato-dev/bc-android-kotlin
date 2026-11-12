# Semana 30 — Room y migraciones

> En la semana 29, las observaciones seguían en un archivo JSON que se leía entero al abrir y se reescribía entero con cada cambio. Esta semana pasan a una base de datos de Room, que es la fuente de verdad: el ViewModel recoge su `Flow` y le manda los cambios. Room comprueba el SQL al compilar, y la base de datos lleva versiones con migraciones que se prueban. La primera vez, lo que había en el JSON pasa a la tabla, y la pantalla aprende a esperar a una lista que ya no llega al instante.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Explicar** qué añade una base de datos a un archivo, qué pone Room encima de SQLite y qué cambia en Room 3
- **Escribir** entidades con claves foráneas e índices, y DAO con `Flow`, `@Upsert`, `@Transaction` y `@Relation`
- **Elegir** el *driver* de SQLite y crear la base de datos una vez, en Android y en la JVM
- **Migrar** el esquema con migraciones automáticas y a mano, y los datos de un formato viejo, una sola vez
- **Probar** Room en la JVM, sus `Flow`, sus migraciones con `MigrationTestHelper` y la pantalla con Robolectric

---

## 📋 Prerrequisitos

- [Semana 29](../week-29-hilt_y_datastore/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- El repositorio y el ViewModel de la semana 28, Hilt y KSP de la semana 29, y SQL básico (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `WHERE`).

---

## 🗂️ Estructura de la Semana

```
week-30-room_y_migraciones/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la libreta de la salida, y sus versiones
├── 3-proyecto/                  # la capa 29 de tu Bitácora: las observaciones en Room
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Por qué Room](1-teoria/01-por-que-room.md) | Lo que no da un JSON; lo que pone Room encima de SQLite; Room 3; Room en la Bitácora | 35 min |
| [02 · Entidades y DAO](1-teoria/02-entidades-y-dao.md) | Entidades, claves foráneas e índices; `@Insert`, `@Upsert`, `@Query`; `Flow` o `suspend`; transacciones y relaciones | 45 min |
| [03 · Drivers, hilos y la primera carga](1-teoria/03-drivers-hilos-y-carga.md) | El SQLite del sistema o el empaquetado; el builder en Android y en la JVM; una instancia; `loaded` | 40 min |
| [04 · Migraciones](1-teoria/04-migraciones.md) | Versiones y esquemas; automáticas, con *spec* y a mano; si falta una; los datos del JSON | 45 min |
| [05 · Probar Room](1-teoria/05-probar-room.md) | En la JVM; un `Flow` sin colgarse; `MigrationTestHelper`; Robolectric con la base de datos de verdad | 45 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La libreta de la salida](2-practicas/ejercicio-01-la-libreta-de-la-salida/README.md) | Dos tablas con clave foránea e índice único, un `Flow` que sigue los cambios, una relación y una copia en una transacción, en la JVM | 2 h |
| [02 · Las versiones de la libreta](2-practicas/ejercicio-02-las-versiones-de-la-libreta/README.md) | De la versión 1 a la 4: una columna nueva, un nombre nuevo y una migración a mano, con sus tests | 2 h |

### Proyecto

Las observaciones de la Bitácora en Room, en `data` y sin Android, con la base de datos en el grafo de Hilt. Lo que había en el JSON pasa a la tabla la primera vez, y un enlace a una observación espera a que la lista haya llegado. La persona no ve nada distinto: sus observaciones siguen ahí.
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
| Ver lo que genera Room | `data/build/generated/ksp/main/kotlin/dev/bitacora/data/`: `BitacoraDatabase_Impl.kt` y `ObservationDao_Impl.kt` |
| Ver el esquema de una versión | `data/schemas/dev.bitacora.data.BitacoraDatabase/1.json`: el `createSql` de cada tabla |
| Mirar la base de datos en un móvil | En Android Studio, *App Inspection* → *Database Inspector*, con la app en marcha (debug) |
| Copiarla al ordenador | `adb exec-out run-as dev.bitacora.agro.debug cat databases/bitacora.db > bitacora.db` (field, debug), con la app cerrada: Room escribe primero en `bitacora.db-wal`. Se abre con `sqlite3` |
| Empezar de cero | `adb shell pm clear dev.bitacora.agro.debug`: vuelven las de partida |
| Un test de Room, solo | `./gradlew :data:test --tests '*RoomTest.theOrderIsByNumber'` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 8 de 8 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 6 de 6 en verde, los cuatro esquemas y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-30` con las observaciones en Room (entidad, DAO y base de datos en `data`, la base de datos en `AppModule`), el JSON importado una vez, `schemas/` subido, la pantalla que espera a `loaded`, los tests de la semana 29 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-30` → `main` con la salida de `./gradlew test`
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
| [Semana 29](../week-29-hilt_y_datastore/README.md) | **Semana 30** | [Semana 31](../week-31-red_con_ktor/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
