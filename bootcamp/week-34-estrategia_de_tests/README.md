# Semana 34 — Estrategia de tests

> La Bitácora llega a la fase 6 con casi 300 tests en siete módulos. Se han ido escribiendo semana a semana, cada uno donde hacía falta. Esta semana, la primera de la fase de calidad, los ordena. La pirámide dice cuánto cuesta cada nivel, y los tiempos están medidos. Los fakes pasan de la app a los `testFixtures` de `:core:data`, y un contrato los compara con Room. `runTest` y Turbine prueban el tiempo y los `Flow` sin esperar. Kover junta la cobertura de los módulos `core` y para el build si baja. La app no cambia.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Decidir** en qué nivel de la pirámide va un test, sabiendo lo que cuesta cada uno
- **Elegir** entre un fake y un mock, y **mantener** honesto un fake con un test de contrato
- **Compartir** fakes entre módulos con `java-test-fixtures`
- **Probar** código con tiempo y corrutinas con `runTest`, los `TestDispatcher` y el reloj virtual
- **Probar** un `Flow` valor a valor con Turbine
- **Medir** la cobertura con Kover, leerla con criterio y hacer que pare el build

---

## 📋 Prerrequisitos

- [Semana 33](../week-33-modularizacion/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)).
- Las corrutinas y `Flow` de las semanas 6 y 7, y los tests del ViewModel de la semana 28.

---

## 🗂️ Estructura de la Semana

```
week-34-estrategia_de_tests/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 3 diagramas SVG
├── 1-teoria/                    # 6 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: el tiempo virtual, y fakes compartidos con cobertura
├── 3-proyecto/                  # la capa 33 de tu Bitácora: la estrategia de tests
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · La pirámide de tests de la Bitácora](1-teoria/01-la-piramide-de-tests.md) | Unidad, integración, extremo a extremo y build; lo que cuesta cada nivel, medido; dónde va un test nuevo; lo que no merece un test | 30 min |
| [02 · Dobles de prueba: fakes frente a mocks](1-teoria/02-fakes-y-mocks.md) | Dummy, stub, fake, spy y mock; MockK; por qué fakes; el contrato que encontró un fake que mentía | 35 min |
| [03 · Compartir fakes: test fixtures](1-teoria/03-test-fixtures.md) | `java-test-fixtures`; qué ve y quién lo ve; lo que no llega al APK; los de AGP | 25 min |
| [04 · Corrutinas en los tests](1-teoria/04-corrutinas-en-los-tests.md) | `runTest` y el reloj virtual; `Standard` y `Unconfined`; mover el reloj; lo que se escapa; `setMain`, `backgroundScope` y el límite de tiempo | 40 min |
| [05 · Probar un Flow con Turbine](1-teoria/05-flow-con-turbine.md) | `test { awaitItem() }`; cómo falla; `StateFlow` y los valores intermedios; efectos; varios `Flow` | 30 min |
| [06 · Cobertura con Kover](1-teoria/06-cobertura-con-kover.md) | El agente y el informe junto; lo que no debería contar; lo que oculta juntar; líneas y ramas; la regla; Android; lo que no dice | 35 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · El tiempo virtual](2-practicas/ejercicio-01-el-tiempo-virtual/README.md) | Reintentos con espera doble, una búsqueda con `debounce` y un sondeo periódico, probados en milisegundos con `runTest` y Turbine | 1 h 45 min |
| [02 · Fakes compartidos y cobertura](2-practicas/ejercicio-02-fakes-compartidos_y_cobertura/README.md) | El fake de un almacén en `testFixtures`, un contrato contra el fake y el archivo, y Kover que para `check`, probado con TestKit | 1 h 45 min |

### Proyecto

La Bitácora con estrategia de tests: los fakes en los `testFixtures` de `:core:data`, el contrato contra el fake y Room, Turbine en los `Flow`, `:core:network` con tests propios y Kover con la regla del 90 % sobre los cuatro módulos `core`.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (6 archivos) | 3 h 15 min |
| Ejercicio 01 | 1 h 45 min |
| Ejercicio 02 | 1 h 45 min |
| Proyecto | 5 h |
| Recursos y glosario | 1 h 15 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Cuánto tarda cada clase de tests | `grep -o '<testsuite name="[^"]*" tests="[0-9]*"[^>]*time="[^"]*"' app/build/test-results/testFieldDebugUnitTest/*.xml` |
| Un solo test | `./gradlew :core:data:test --tests '*RepositoryTest.theOrderIsByNumber'` |
| La cobertura en la consola | `./gradlew koverLog` (el raíz, junto) o `./gradlew :core:network:koverLog` (el módulo, solo) |
| El informe para leer | `./gradlew koverHtmlReport` y `build/reports/kover/html/index.html` |
| Qué ejecuta un informe sin ejecutarlo | `./gradlew koverXmlReport -m` |
| Qué hay en los fixtures | `./gradlew :core:data:testFixturesJar` y `unzip -l core/data/build/libs/data-1.0.0-test-fixtures.jar` |
| Que los fakes no van en el APK | `./gradlew :app:dependencies --configuration fieldReleaseRuntimeClasspath -q \| grep -c test-fixtures` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 12 de 12 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 8 de 8 en verde y `NOTAS.md` con el paso 8
3. ✅ Proyecto: rama `week-34` con los fakes en `testFixtures`, el contrato, Turbine, los tests de `:core:network` y Kover; los tests de la semana 33 y `TestingTest` en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-34` → `main` con la salida de `./gradlew test`
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
| [Semana 33](../week-33-modularizacion/README.md) | **Semana 34** | Semana 35 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
