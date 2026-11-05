# Semana 27 — Pantallas grandes y el mapa

> Hasta la semana 26, la Bitácora era una columna: en una tableta o un plegable abierto, la lista ocupaba 900 dp de ancho y abrir una observación la tapaba entera. Y las coordenadas eran dos números. Esta semana la Bitácora mira el tamaño de la ventana y su postura: con sitio, la lista y el detalle van uno al lado del otro; medio plegada, el detalle arriba de la bisagra. Y las observaciones con ubicación salen en un mapa de verdad, con Maps Compose. Se cierra la fase 4.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Decidir** la forma de una pantalla por la clase de tamaño de su ventana, no por la orientación ni por el modelo de móvil
- **Montar** un list-detail con Navigation 3 que en un móvil es una pila y en una pantalla ancha son dos paneles
- **Leer** la postura de un plegable (la bisagra, la mesa) y probarla en la JVM con una bisagra de mentira
- **Pintar** un mapa con Maps Compose: la clave fuera del repositorio, un marcador por observación y la cámara sobre todas
- **Probar** lo que rodea al mapa en Robolectric, y saber qué no se puede probar sin Play Services

---

## 📋 Prerrequisitos

- [Semana 26](../week-26-accesibilidad_y_recomposicion/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Navigation 3 y los enlaces de la [semana 24](../week-24-navegacion_y_enlaces/README.md), y la ubicación de la [semana 19](../week-19-permisos_de_camara_y_ubicacion/README.md).
- Para ver el mapa en un móvil (opcional): una cuenta de Google Cloud con la Maps SDK for Android activada y una clave de API. Los tests no la necesitan.

---

## 🗂️ Estructura de la Semana

```
week-27-pantallas_grandes_y_mapa/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: la sesión que se adapta, y el mapa de la salida
├── 3-proyecto/                  # la capa 26 de tu Bitácora: list-detail y mapa
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Clases de tamaño de ventana](1-teoria/01-clases-de-tamano.md) | Compact, medium y expanded; `currentWindowAdaptiveInfoV2`; por qué no mirar la orientación; los qualifiers de Robolectric | 40 min |
| [02 · List-detail con Navigation 3](1-teoria/02-list-detail.md) | `ListDetailSceneStrategy`, los paneles en los metadatos, el hueco, abrir uno nuevo y atrás con dos paneles | 50 min |
| [03 · Plegables y posturas](1-teoria/03-plegables-y-posturas.md) | La bisagra, la mesa y el libro; `Posture`; `window-testing`; desplegar es un cambio de configuración | 40 min |
| [04 · El mapa con Maps Compose](1-teoria/04-el-mapa.md) | Las dependencias, la clave de API fuera del repositorio, `GoogleMap`, `Marker` y la cámara, y lo que entra con Play Services | 50 min |
| [05 · Probar lo que no se pinta](1-teoria/05-probar-el-mapa.md) | Qué hace el mapa en Robolectric, qué se prueba alrededor y qué solo se ve en un móvil | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · La sesión que se adapta](2-practicas/ejercicio-01-la-sesion-que-se-adapta/README.md) | Una sesión que es un panel en el móvil, dos en una pantalla ancha y se parte por la bisagra medio plegada, y que no pierde la elegida al desplegar | 2 h |
| [02 · El mapa de la salida](2-practicas/ejercicio-02-el-mapa-de-la-salida/README.md) | La clave en el manifiesto, el rectángulo que las contiene a todas, y un `GoogleMap` con un marcador por observación | 2 h |

### Proyecto

La Bitácora es un list-detail: en un móvil, como hasta ahora; con 840 dp o más, la sesión a la izquierda y la observación o el mapa a la derecha. Y tiene un mapa con las observaciones que tienen ubicación.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 35 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 55 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Una ventana ancha en un test | `@Config(qualifiers = "w900dp-h600dp")`; en Robolectric, por defecto, 320 x 470 dp |
| Desplegar a mitad de un test | `RuntimeEnvironment.setQualifiers("w900dp-h600dp")` y `scenario.recreate()` |
| Una bisagra de mentira | `WindowLayoutInfoPublisherRule` por fuera de la regla de Compose y `overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(FoldingFeature(activity, ...))))` |
| La clave de Maps sin subirla | `mapsApiKey=...` en `~/.gradle/gradle.properties`, o `ORG_GRADLE_PROJECT_mapsApiKey` en CI |
| Qué entra con una dependencia | `./gradlew --write-verification-metadata sha256 ...` y `git diff --stat gradle/verification-metadata.xml`; después, [OSV](https://osv.dev) |
| En el móvil | Android Studio tiene emuladores de plegable y de tableta, y en un móvil de verdad, Opciones de desarrollador → "Forzar que las actividades se puedan redimensionar" y la pantalla partida |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 7
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-27` con el list-detail, el destino del mapa, `area()` en `core`, la clave fuera del repositorio, la verificación regenerada y revisada, los tests de la semana 26 y los nuevos del starter en verde, y al menos 6 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-27` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`, sin la propiedad `mapsApiKey`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 26](../week-26-accesibilidad_y_recomposicion/README.md) | **Semana 27** | Semana 28 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
