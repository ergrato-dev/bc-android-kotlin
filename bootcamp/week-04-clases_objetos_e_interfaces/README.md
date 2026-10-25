# Semana 04 — Clases, objetos e interfaces

> Hasta aquí tus datos eran `data class` y tus operaciones, funciones sueltas. Esta semana los juntas: un objeto que **se protege a sí mismo**, que no puede existir en un estado imposible y que se comporta según su tipo. Tu Bitácora deja de ser un conjunto de funciones y pasa a ser un objeto con reglas.

Todavía no hay `enum`, `sealed` ni genéricos propios (semana 5), ni coroutines. Sí hay interfaces, herencia, `object`, extensiones y delegación.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Declarar** clases con constructor primario, `init` y constructores secundarios
- **Escribir** propiedades con `get`/`set`, calculadas y con `private set`
- **Proteger** el estado interno con una propiedad de respaldo y una vista de solo lectura
- **Definir** invariantes y **cumplirlas** en `init` y en los métodos
- **Explicar** qué genera una `data class` y qué entra en su `equals`
- **Usar** interfaces, clases `abstract`/`open` y `override`, y **aprovechar** el polimorfismo
- **Elegir** entre interfaz, herencia y composición, y **delegar** con `by`
- **Escribir** un `companion object` con una fábrica y funciones de extensión
- **Implementar** `Comparable` y **probar** invariantes con `assertFailsWith`

---

## 📋 Prerrequisitos

- [Semana 03](../week-03-colecciones_y_lambdas/README.md) completada: colecciones, lambdas, `groupBy`, `sortedWith`.
- Tu repositorio `bc-android-kotlin-<tu-dominio>` con las semanas 1 a 3 en `main`.

---

## 🗂️ Estructura de la Semana

```
week-04-clases_objetos_e_interfaces/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 3 de tu Bitácora: el objeto Logbook
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Clases, propiedades y constructores](1-teoria/01-clases-propiedades-y-constructores.md) | `init`, `get`/`set`, `field`, visibilidad, `lateinit` | 45 min |
| [02 · Encapsulación y datos](1-teoria/02-encapsulacion-y-datos.md) | Propiedad de respaldo, invariantes, `data class`, `==` y `===` | 40 min |
| [03 · Interfaces, herencia y composición](1-teoria/03-interfaces-herencia-y-composicion.md) | `open`, `abstract`, `override`, polimorfismo, `by` | 45 min |
| [04 · `object`, `companion` y extensiones](1-teoria/04-object-companion-y-extensiones.md) | Fábricas, extensiones, `Comparable`, operadores | 40 min |
| [05 · Modelar un dominio](1-teoria/05-modelar-un-dominio.md) | De la descripción a las clases, dato o servicio, probar clases | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Clases y propiedades](2-practicas/ejercicio-01-clases-y-propiedades/README.md) | `Counter`, `Temperature`, `Rectangle`, `Point`, `Playlist` | 2 h |
| [02 · Interfaces y herencia](2-practicas/ejercicio-02-interfaces-y-herencia/README.md) | `Shape`, `Employee`, `Version` con `Comparable`, extensiones | 2 h |

### Proyecto

`Observation` se valida sola, `Logbook` guarda y protege las observaciones y escribe su informe, y `Observation.parse` y `Logbook.fromLines` leen datos externos.
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
| Generar `equals`, `hashCode`, `toString` | Convierte la clase en `data class` |
| Ver qué genera una `data class` | IntelliJ: Tools → Kotlin → Show Kotlin Bytecode → Decompile |
| Implementar los miembros abstractos | Alt+Enter sobre la clase → "Implement members" |
| Renombrar sin romper tests | Shift+F6 sobre el nombre (actualiza tests y usos) |
| Ver la jerarquía de una clase | Ctrl+H sobre el nombre |
| Comprobar la visibilidad real | Intenta usar el miembro desde otro archivo: el IDE marca el error |
| Ejecutar un solo test | `./gradlew test --tests "LogbookTest.addDup"` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 6 de 6 en verde
2. ✅ Ejercicio 02: 7 de 7 en verde y `NOTAS.md` con la respuesta del paso 8
3. ✅ Proyecto: rama `week-04` con `Observation.kt`, `Logbook.kt` y al menos 8 tests nuevos calculados a mano, todos en verde
4. ✅ Pull request `week-04` → `main` con la salida de `./gradlew test` y la de `./gradlew run`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Los starters empiezan en rojo (`NotImplementedError`): es lo esperado.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 03](../week-03-colecciones_y_lambdas/README.md) | **Semana 04** | [Semana 05](../week-05-enums_sealed_y_genericos/README.md) |

← [Volver al inicio del bootcamp](../../README.md)
