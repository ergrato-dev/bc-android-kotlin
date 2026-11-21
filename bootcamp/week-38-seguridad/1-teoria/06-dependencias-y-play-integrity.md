# Las dependencias y Play Integrity

> La mayor parte del código de la Bitácora no lo ha escrito nadie del equipo: son 927 componentes en `verification-metadata.xml`. Desde la semana 11, Gradle comprueba que cada uno es el que se revisó (su sha256). Falta la otra pregunta: ¿alguno tiene una vulnerabilidad conocida? Esta teoría escanea la Bitácora contra OSV, separa lo que llega al APK de lo que solo corre en el build, y termina con lo que la app no puede comprobar sola: si quien llama al servidor es tu app, sin tocar, en un móvil de verdad. Eso es Play Integrity.

## 🎯 Objetivos

- Distinguir lo que hace la verificación de dependencias de lo que hace un escáner
- Escanear las dependencias de un proyecto con OSV
- Saber si un aviso afecta a la app o solo al build
- Explicar qué da Play Integrity, y por qué se comprueba en el servidor

## 1. Verificar no es escanear

| Herramienta | Pregunta | Desde |
| --- | --- | --- |
| `verification-metadata.xml` | ¿Es este archivo el que revisamos? (sha256) | Semana 11 |
| Revisar en OSV antes de fijar | ¿Tiene avisos la versión que entra? | Cada semana, en `docs/stack-versions.md` |
| Un escáner, en CI | ¿Ha salido un aviso **después** de fijarla? | Esta semana |

La verificación protege de un archivo cambiado. No sabe nada de un fallo de seguridad publicado ayer en una versión que lleva meses en el proyecto. Eso hay que preguntarlo cada vez, y por eso va en CI.

## 2. OSV

[OSV](https://osv.dev/) junta los avisos de GitHub (GHSA), de los CVE y de cada ecosistema, por paquete y versión. Su API acepta hasta 1 000 consultas por petición:

```bash
curl -s -X POST https://api.osv.dev/v1/query \
  -d '{"package":{"name":"com.google.crypto.tink:tink-android","ecosystem":"Maven"},"version":"1.24.0"}'
# {}  : sin avisos
```

Para todo el proyecto, `osv-scanner` lee `gradle/verification-metadata.xml` directamente:

```bash
osv-scanner scan source -L gradle/verification-metadata.xml
```

O un script con la API: sacar `group`, `name` y `version` de cada `<component>` y mandarlos a `/v1/querybatch`. 🔬 Sobre la Bitácora de esta semana: **927 componentes, 11 con avisos**.

## 3. Lo del build y lo del APK

`verification-metadata.xml` tiene todo lo que Gradle descarga: lo que va en el APK, lo que corre en el build (AGP, KSP, Kotlin, lint) y versiones que perdieron la resolución de conflictos y de las que solo se bajó el POM o el `.module`. 🔬 Los 11 de la Bitácora:

APK_TABLE

Un escáner no sabe si una versión se usa: hay que preguntárselo a Gradle.

```bash
./gradlew :app:dependencies --configuration fieldReleaseRuntimeClasspath | grep okio
./gradlew :app:dependencyInsight --configuration fieldReleaseRuntimeClasspath --dependency okio
```

Lo que llega al APK se corrige ya: con la versión corregida en el catálogo, o con una restricción (`constraints`) si llega de otra biblioteca, como `wire-runtime` 6.4.7 en la semana 36 o `guava` 33.7.2 para Robolectric. Lo del build importa menos, pero no nada: corre en tu máquina y en CI, con tus credenciales.

## 4. Play Integrity

El servidor de campo recibe un token y no sabe quién lo manda: la Bitácora, una copia modificada, o un script con el token copiado de un móvil con root. La app no puede demostrarlo sola, porque quien la modifica también modifica la comprobación.

Play Integrity lo pregunta a Google Play:

1. La app pide un token de integridad (`StandardIntegrityManager`), con un hash de la petición que quiere proteger.
2. Lo manda a **su** servidor junto con la petición.
3. El servidor lo pasa a la API de Google, que lo descifra y devuelve el veredicto.
4. El servidor decide.

| Veredicto | Qué dice |
| --- | --- |
| `appIntegrity` | Si el APK es el que subiste a Play, firmado con tu clave |
| `deviceIntegrity` | Si es un Android de verdad, con Play, sin root ni emulador |
| `accountDetails` | Si la cuenta tiene la app instalada desde Play |

Lo que hay que entender:

- **La decisión es del servidor.** Un veredicto comprobado en la app se salta quitando el `if`.
- **No es para todo.** Hay cuota de peticiones, y un móvil sin Play no tiene veredicto. Se pide en lo que importa (vincular el móvil, una operación cara), no en cada sincronización.
- **No sustituye nada de esta semana.** Es una capa más, con el token cifrado, la red fijada y el servidor validando cada petición.

La Bitácora no lo integra: necesita la app publicada en Play y un servidor que llame a Google. Queda como introducción.

## ⚠️ Errores comunes

| Error | Por qué |
| --- | --- |
| Creer que la verificación de dependencias detecta vulnerabilidades | Solo detecta archivos cambiados |
| Escanear una vez al fijar y nunca más | Los avisos salen después |
| Tratar los 11 avisos igual | Unos llegan a los móviles; otros, solo al build |
| Comprobar el veredicto de Play Integrity en la app | Quien modifica la app quita la comprobación |
| Pedir un veredicto en cada petición | Hay cuota, y añade latencia a todo |

## 🧪 Comprueba que lo entendiste

1. ¿Qué detecta la verificación de dependencias y qué no?
2. ¿Por qué `verification-metadata.xml` tiene versiones que no se usan?
3. ¿Cómo sabes si un aviso afecta a tu APK? ¿Cómo lo corriges si llega de otra biblioteca?
4. ¿Por qué el veredicto de Play Integrity se comprueba en el servidor?

---

← [05 · Secretos y copias de seguridad](05-secretos-y-copias.md)
