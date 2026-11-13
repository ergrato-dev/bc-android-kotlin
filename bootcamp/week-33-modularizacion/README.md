# Semana 33 — Modularización

> Desde la semana 30, la Bitácora guarda en Room, habla con el servidor y sincroniza sin red. Todo eso vive en un solo módulo `data`, al lado de un `core` con el dominio, y la app tiene todas las pantallas. Esta semana, la que cierra la fase 5, la Bitácora se parte como se parte una app grande: cuatro módulos `core` de Kotlin puro (modelo, base de datos, red y datos) y una feature, `:feature:map`, que es una biblioteca de Android con sus textos, sus dependencias y sus tests. El APK no cambia. Lo que cambia es lo que ve cada módulo y lo que se recompila con cada cambio, y las dos cosas se miden.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Decidir** dónde partir una app (app, feature, core) y con qué reglas se mantiene el grafo limpio
- **Partir** un módulo de datos en módulos de Kotlin puro, con `api`, `implementation` e `internal` elegidos por las firmas
- **Crear** una biblioteca de Android con su namespace, su `R`, sus recursos, su manifiesto y sus tests, y un convention plugin común con la app
- **Comprobar** las reglas del grafo en el build, al configurar
- **Medir** qué recompila un cambio según dónde está y si cambia la ABI

---

## 📋 Prerrequisitos

- [Semana 32](../week-32-workmanager_y_offline_first/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Los módulos y `api`/`implementation` de la semana 10, y los convention plugins y TestKit de las semanas 11 y 12.

---

## 🗂️ Estructura de la Semana

```
week-33-modularizacion/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: una biblioteca de Android, y las reglas del grafo
├── 3-proyecto/                  # la capa 32 de tu Bitácora: la app en módulos
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Por qué partir una app en módulos](1-teoria/01-por-que-modularizar.md) | Lo que se gana y lo que cuesta; app, feature y core; el grafo de la Bitácora y sus reglas; cuándo no | 35 min |
| [02 · Los módulos core: partir los datos](1-teoria/02-los-modulos-core.md) | Cuatro módulos por lo que cambia junto; `api`, `implementation` e `internal`; el smart cast entre módulos; versiones por módulo | 45 min |
| [03 · Bibliotecas de Android: el módulo del mapa](1-teoria/03-bibliotecas-de-android.md) | `com.android.library`; namespace y `R` no transitiva; recursos y manifiestos que se juntan; tests con `targetSdk`; `CommonExtension` | 45 min |
| [04 · Features que no se conocen y reglas del grafo](1-teoria/04-features-y-reglas-del-grafo.md) | Datos y lambdas en la frontera; lo que se queda en la app; lo que Gradle no ve; las reglas en un convention plugin; el grafo en un test | 40 min |
| [05 · Medir y probar el build modular](1-teoria/05-medir-el-build-modular.md) | La ABI decide; tres cambios medidos frente a la semana 32; en paralelo; el APK no cambia; lista para partir un módulo | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Una biblioteca de Android](2-practicas/ejercicio-01-una-biblioteca-de-android/README.md) | La ayuda en su módulo: sus textos en dos idiomas, su actividad en su manifiesto, sus tests en Robolectric; la app le cambia el título y le quita un permiso | 2 h |
| [02 · Las reglas del grafo](2-practicas/ejercicio-02-las-reglas-del-grafo/README.md) | Un convention plugin que para el build cuando un módulo declara una dependencia prohibida, probado con TestKit | 1 h 30 min |

### Proyecto

La Bitácora en módulos: `:core:model`, `:core:database`, `:core:network` y `:core:data` de Kotlin puro, `:feature:map` como biblioteca de Android, y la app que los junta. Mismo APK, mismos tests de la app.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 20 min |
| Ejercicio 01 | 2 h |
| Ejercicio 02 | 1 h 30 min |
| Proyecto | 5 h |
| Recursos y glosario | 1 h 10 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Ver los módulos | `./gradlew projects` |
| Qué ve un módulo al compilar | `./gradlew :app:dependencies --configuration fieldDebugCompileClasspath` |
| Por qué está una biblioteca, y de quién viene | `./gradlew :app:dependencyInsight --configuration fieldDebugRuntimeClasspath --dependency maps-compose` |
| Qué se ejecutaría, sin ejecutarlo | `./gradlew :feature:map:testDebugUnitTest -m` |
| Qué se ha ejecutado de verdad | `./gradlew :app:assembleFieldDebug --console=plain \| grep '^> Task' \| grep -vE 'UP-TO-DATE\|NO-SOURCE\|SKIPPED'` |
| El manifiesto final de la app | `app/build/intermediates/merged_manifest/fieldDebug/processFieldDebugMainManifest/AndroidManifest.xml` |
| Probar un módulo solo | `./gradlew :core:data:test` o `./gradlew :feature:map:testDebugUnitTest` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 11 de 11 en verde y `NOTAS.md` con los pasos 2 y 8
2. ✅ Ejercicio 02: 11 de 11 en verde y `NOTAS.md` con el paso 6
3. ✅ Proyecto: rama `week-33` con los cuatro módulos `core` y `:feature:map`, los convention plugins de Android, los tests de la semana 32 y `ModulesTest` en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-33` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida para el ejercicio 01 y el proyecto:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 32](../week-32-workmanager_y_offline_first/README.md) | **Semana 33** | Semana 34 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
