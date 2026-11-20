# Ejercicio 01 — Reglas keep

⏱️ 1 h 30 min · Teoría previa: 01 y 02

## Objetivo

Un programa pequeño de Kotlin con lo que R8 no puede seguir: sensores que se crean por el nombre de su clase, escrito en un archivo, y un objeto que se escribe leyendo sus campos por reflexión. Sin R8 funciona; tras R8, no. Escribes las reglas que lo arreglan, y **ni una más**: los tests comprueban también que R8 sigue quitando y renombrando lo demás.

No necesita Android: R8 trabaja igual sobre clases de la JVM (`--classfile`), y el programa se ejecuta con `java`.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM (`dev.bitacora.keep`):

- `src/main/kotlin/dev/bitacora/keep/Main.kt`: el programa, ya hecho. Los sensores `Rain` y `Wind`, que se crean con `Class.forName`; `Observation`, que `describe` escribe leyendo sus campos; `Helper`, que el código usa de forma directa, y `Unused`, que nadie usa.
- `src/main/resources/sensors.txt`: los nombres de las clases de los sensores.
- `rules.pro`: las reglas. Trae la del punto de entrada (`main`); las demás, tuyas.
- `build.gradle.kts`, ya hecho: R8 9.4.24 (el de AGP 9.4.1) como programa, y la tarea `shrink`, que pasa el programa y la biblioteca de Kotlin por R8 y deja `build/r8/shrunk.jar` y `build/r8/mapping.txt`. `./gradlew run` ejecuta el programa sin R8; `./gradlew runShrunk`, el que sale de R8.
- `src/test/kotlin/KeepTest.kt`: 7 tests que ejecutan el programa reducido y leen su mapping. No los toques.

Al empezar fallan 4 de 7.

## Pasos

1. **Sin R8**: `./gradlew run`. Apunta las tres líneas que escribe.
2. **Con R8**: `./gradlew runShrunk`. ¿Con qué excepción falla? Abre `build/r8/mapping.txt` y busca `dev.bitacora.keep.Rain`. ¿Está? ¿Por qué R8 lo ha quitado, si `sensors.txt` lo nombra? (Teoría 02, apartado 1.)
3. **Los sensores**: una regla en `rules.pro` para que toda clase que implemente `Sensor` conserve su nombre y su constructor sin argumentos. Una sola regla para los dos, no una por clase. `./gradlew runShrunk`: ¿qué escribe ahora en la tercera línea? Pasan `theSensorsAreFound` y `theSensorsKeepTheirNames`.
4. **Los campos**: busca `dev.bitacora.keep.Observation` en el mapping. ¿Qué nombre tiene? ¿Qué campos le quedan? ¿Y qué argumentos tiene su constructor (mira la `residualsignature`)? Escribe la regla que conserva los nombres de sus campos **sin** conservar el nombre de la clase (teoría 02, apartado 3). Pasan los 7.
5. **Lo que no se conserva**: `./gradlew test` con los 7 en verde, y mira el mapping. ¿Qué nombre tienen `Observation`, `Sensor` y `MainKt`? ¿Dónde están `Helper` y `Unused`?
6. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) Cambia todas tus reglas por `-keep class dev.bitacora.keep.** { *; }`. ¿Funciona el programa? ¿Qué tests fallan, y qué dice cada uno de lo que ha dejado de hacer R8?
   - (b) Cambia tu regla de los campos por `-keep class dev.bitacora.keep.Observation`. ¿Qué escribe la tercera línea? ¿Por qué no basta conservar la clase?
   - (c) Quita `Wind` de `sensors.txt`. ¿Sigue en el mapping con su nombre? ¿Por qué? (Lee tu regla: ¿dice "si alguien la usa"?)
   - (d) En la salida de `shrink` sale dos veces `Warning: Resource 'META-INF/MANIFEST.MF' already exists.` ¿De dónde sale cada uno? (Mira qué archivos le pasa la tarea a R8.)

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `java.lang.ClassNotFoundException: dev.bitacora.keep.Rain` | Es el punto de partida: falta la regla de los sensores |
| La tercera línea sale vacía | R8 ha quitado los campos de `Observation`: nadie los leía en el código |
| `code=A-1, ...` sale bien pero falla `observationCanBeRenamed` | Has conservado la clase entera: basta con sus miembros |
| Fallan `helperIsRenamed` o `unusedIsGone` | Una regla demasiado ancha (`**`, `{ *; }`) |
| `Could not find com.android.tools:r8:9.4.24.` | R8 está en el repositorio de Google: `google()` en `repositories` (ya está en el starter) |
| Cambias `rules.pro` y los tests no se ejecutan | No debería pasar: `shrink` es una entrada de `test`. Si pasa, `./gradlew test --rerun` |

## Entrega

Los 7 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 6.
