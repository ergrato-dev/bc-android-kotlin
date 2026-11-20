# Reglas keep

> R8 sigue las llamadas del código. Lo que se usa sin una llamada que R8 pueda ver (una clase creada por su nombre, un campo leído por reflexión, un nombre guardado en una base de datos) lo quita o lo renombra, y la app falla al ejecutar, no al compilar. Una **regla keep** le dice qué conservar. Esta teoría explica cuándo hace falta una, cómo se escribe la más pequeña que funciona, y cómo se comprueba.

## 🎯 Objetivos

- Reconocer el código que R8 no puede seguir: reflexión, nombres en texto, JNI, nombres guardados
- Elegir entre `-keep`, `-keepclassmembers`, `-keepclasseswithmembers` y los modificadores `allowobfuscation` y `allowshrinking`
- Escribir la regla más pequeña que funciona, y comprobarlo con `seeds.txt`, `usage.txt` y `mapping.txt`
- Saber por qué la Bitácora no tiene reglas propias

## 1. Lo que R8 no ve

| Caso | Ejemplo | Qué pasa tras R8 |
| --- | --- | --- |
| Una clase creada por su nombre | `Class.forName("dev.bitacora.keep.Rain")` | `ClassNotFoundException`: la clase no está, o se llama `a` |
| Campos leídos por reflexión | Un serializador que recorre `declaredFields` | Los campos no están: nadie los leía (medido en el ejercicio 01) |
| Un nombre guardado | WorkManager guarda en su base de datos el nombre de la clase del worker | La versión siguiente renombra el worker, y el trabajo pendiente no encuentra su clase |
| Código nativo (JNI) | `external fun` llamada desde C | El nombre del método ya no coincide con el del `.so` |
| Lo que nombra un XML | Una vista en un layout, la actividad en el manifiesto | Nada: AGP genera reglas para esto (`Generated ProGuard rules` en `configuration.txt`) |

En el ejercicio 01, sin reglas, el programa pasa por R8 sin un error y al ejecutarse falla con `java.lang.ClassNotFoundException: dev.bitacora.keep.Rain`.

## 2. Las reglas

Una regla dice **qué clases** y **qué miembros**:

```
-keep class * implements dev.bitacora.keep.Sensor {
    <init>();
}
```

"Toda clase que implemente `Sensor`: consérvala con su nombre, y también su constructor sin argumentos".

| Regla | Conserva |
| --- | --- |
| `-keep class X { m; }` | La clase X, con su nombre, y los miembros `m` |
| `-keepclassmembers class X { m; }` | Solo los miembros `m`, si X se queda por otra razón. X puede cambiar de nombre |
| `-keepclasseswithmembers class * { m; }` | Las clases que tengan todos los miembros `m`, con ellos |
| `-keepnames class X` | Abreviatura de `-keep,allowshrinking`: si X se queda, con su nombre; pero se puede quitar |

Y los modificadores que relajan una regla:

| Modificador | Permite |
| --- | --- |
| `allowshrinking` | Quitarlo si nadie lo usa |
| `allowobfuscation` | Cambiarle el nombre |
| `allowoptimization` | Optimizarlo (meterlo en otro método, cambiar su firma) |

Los miembros se escriben como en Java: `<init>()` es el constructor sin argumentos, `<init>(...)` cualquiera, `<fields>` todos los campos, `<methods>` todos los métodos, `*` todo. Los nombres admiten `*` (un trozo de nombre sin `.`) y `**` (cualquier cosa).

Hay una condicional: `-if class X -keep class Y`. Solo conserva Y si X se queda.

## 3. La más pequeña que funciona

Una regla de más no rompe nada, y por eso se acumulan: `-keep class dev.bitacora.** { *; }` hace que todo funcione y deja la app sin renombrar ni reducir. Cada regla tiene que conservar **solo** lo que se usa sin que R8 lo vea:

| Lo que hace el código | Regla |
| --- | --- |
| Crea la clase por su nombre | `-keep class ... { <init>(); }`: nombre y constructor |
| Lee los campos por reflexión, y la clase la nombra el código | `-keepclassmembers class ... { <fields>; }`: los campos; la clase puede cambiar de nombre |
| Busca la clase por su nombre si alguien la usa | `-keepnames class ...` |

El ejercicio 01 lo comprueba: con la regla de todo (`**`), fallan los tres tests que miran que `Helper` se renombra, que `Unused` desaparece y que `Observation` puede cambiar de nombre.

`@Keep` (de `androidx.annotation`) hace lo mismo desde el código: las reglas de AGP conservan todo lo que lo lleva. Es cómodo, pero conserva la clase entera, con nombre.

## 4. Comprobarlo

Una regla no se da por buena porque la app compile. Se mira lo que R8 ha hecho:

- `seeds.txt`: lo que conservan las reglas. ¿Está tu clase? ¿Y nada más?
- `usage.txt`: lo que ha quitado. Sin la regla del ejercicio 02, `dev.bitacora.Humidity` y `dev.bitacora.Temperature` aparecen aquí (medido).
- `mapping.txt`: con qué nombre ha quedado. `dev.bitacora.SyncWorker -> dev.bitacora.SyncWorker:` conserva el nombre; `dev.bitacora.BitacoraViewModel -> fk:`, no.
- `configanalyzer.html`: cada regla, con cuántas clases, métodos y campos conserva. Una regla que conserva miles es sospechosa.

Y luego, se ejecuta la release. Un test que lo mire, mejor: en la Bitácora, `ShrinkTest.theWorkerKeepsItsName`.

## 5. La Bitácora, sin reglas propias

La Bitácora usa reflexión, y mucha: Hilt convierte componentes en sus *entry points* con un cast, Room crea `BitacoraDatabase_Impl` por su nombre, WorkManager crea `SyncWorker` por el suyo, kotlinx.serialization busca el `Companion` de cada clase `@Serializable`, y Ktor busca sus extensiones de serialización con `ServiceLoader` (StrictMode lo pilla: teoría 06). Y su `proguard-rules.pro` está vacío.

Con R8 encendido y sin una regla propia, la release se abrió, añadió 40 observaciones y recorrió la lista: los benchmarks de la semana 36 pasaron (medido). Cada una de esas bibliotecas trae sus reglas, y llegan solas (teoría 03). Por ejemplo, las de WorkManager:

```
-keepnames class * extends androidx.work.ListenableWorker
-keepclassmembers public class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
```

Por eso `SyncWorker` conserva su nombre. Sin `ShrinkTest`, eso dependería de que la próxima versión de WorkManager siga trayendo la regla.

Así que la primera regla de `proguard-rules.pro` es no escribir ninguna hasta que algo falle, y que un test la pida.

## ⚠️ Errores comunes

| Error | Por qué |
| --- | --- |
| `-keep class dev.bitacora.** { *; }` "para que funcione" | Funciona porque apaga R8 en tu código |
| `-dontwarn` para callar un aviso de clase que falta | Si la clase se usa al ejecutar, falla allí. Primero, averigua quién la pide |
| `-keep` cuando bastaba `-keepclassmembers` | Conserva el nombre de la clase, y con él, todo lo que nombra |
| Una regla para una biblioteca que ya la trae | Repetida, no hace daño; equivocada (`{ *; }`), sí. Mira antes `configuration.txt` |
| Copiar reglas de una respuesta de hace años | Eran para ProGuard o para el modo de compatibilidad. Comprueba con `seeds.txt` qué conserva cada una |

## 🧪 Comprueba que lo entendiste

1. ¿Por qué un error de R8 aparece al ejecutar y no al compilar?
2. ¿Qué diferencia hay entre `-keep` y `-keepclassmembers`? ¿Cuál usarías para campos leídos por reflexión de una clase que el código crea con `new`?
3. ¿Dónde miras qué ha conservado una regla? ¿Y qué ha quitado R8?
4. ¿Por qué `SyncWorker` tiene que conservar su nombre? ¿Quién escribe esa regla?

---

← [01 · R8](01-r8.md) · Siguiente: [03 · Las reglas de las bibliotecas](03-reglas-de-las-bibliotecas.md)
