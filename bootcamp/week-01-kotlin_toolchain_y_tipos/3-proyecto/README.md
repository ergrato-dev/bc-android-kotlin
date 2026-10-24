# Proyecto — El `core` de la Bitácora

⏱️ 4 h 30 min · Es la capa 0 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

El módulo de datos y de informes de una **Bitácora de campo** para tu dominio (lista en [`docs/dominios.md`](../../../docs/dominios.md)). Todavía no hay pantallas ni base de datos: solo Kotlin puro, bien tipado y probado.

## Pasos

1. **Elige tu dominio** con el instructor y escribe en el README de tu repositorio qué observas y qué mides.
2. **Crea el repositorio** `bc-android-kotlin-<tu-dominio>` y copia `starter/` dentro. Rama `week-01`.
3. **Adapta el modelo.** Renombra `Observation` y sus propiedades al vocabulario de tu dominio, en inglés (con el *rename* del IDE, que actualiza también los tests). `quantity` y `measure` deben tener sentido: por ejemplo, plantas contadas y porcentaje de daño.
4. **Implementa** `row`, `total`, `average` y `report` (hay un `TODO` en cada una).
   - `row`: código a la izquierda (8), descripción a la izquierda (24), cantidad a la derecha (5), medida a la derecha con 2 decimales (10).
   - `total`: suma de `quantity * measure`.
   - `average`: media de `measure` con **división real**; lista vacía → `0.0`.
   - `report`: cabecera, una fila por observación y una línea final `Total: X  Media: Y` con 2 decimales.
5. **Usa `Locale.ROOT`** en cada `String.format`.
6. **Escribe al menos 4 tests más** en `ObservationTest.kt`, con valores **calculados a mano** antes de ejecutar. Cubre: una fila, el total, la media, la lista vacía.
7. **Cambia los datos** de `Main.kt` por tres observaciones de tu dominio y ejecuta `./gradlew run`.
8. **Abre un pull request** `week-01` → `main` con tu dominio, la salida de `./gradlew --version` y la de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): 5 tests o más, sin warnings, sin `!!`, `val` por defecto, formato con `Locale.ROOT`.

## Pistas

- `list.sumOf { it.quantity * it.measure }` suma sobre una lista.
- Para dividir por el tamaño de la lista y obtener decimales, la suma debe ser `Double`.
- `String.format(Locale.ROOT, "%-8s %-24s %5d %10.2f", ...)` te da una fila alineada.
- Si un test pasa "por casualidad" porque copiaste la salida, no demuestra nada: calcula tú el valor esperado.
