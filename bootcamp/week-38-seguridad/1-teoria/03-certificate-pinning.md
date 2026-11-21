# Certificate pinning

> TLS confía en todas las autoridades que trae el sistema. Basta con que una emita, por error o por presión, un certificado para `api.bitacora.example`, y quien lo tenga puede ponerse en medio. Fijar (*pinning*) es decir: para este servidor, solo estas claves. Es la defensa más fuerte que tiene la red de una app, y la que más apps ha dejado sin conexión. Esta teoría explica qué se fija, cómo se calcula un pin, la clave de reserva y la fecha de caducidad.

## 🎯 Objetivos

- Calcular el pin de un certificado y de una clave pública
- Fijar un servidor en `network_security_config.xml` con dos pines y una caducidad
- Explicar qué pasa cuando el servidor cambia de clave
- Saber cuándo no fijar

![Sin pin, vale cualquier certificado firmado por una CA de confianza; con pin, además, su clave tiene que ser una de las dos fijadas](../0-assets/03-pinning.svg)

## 1. Qué se fija

Un certificado caduca cada pocos meses y se renueva; la clave puede seguir siendo la misma. Por eso no se fija el certificado sino **su clave pública**: el hash SHA-256 del `SubjectPublicKeyInfo` (SPKI), en Base64.

```bash
# El pin de un certificado (server/api.pem)
openssl x509 -in server/api.pem -pubkey -noout \
  | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64

# El de una clave pública suelta (server/backup.pem)
openssl pkey -pubin -in server/backup.pem -outform der | openssl dgst -sha256 -binary | openssl enc -base64

# El del servidor en marcha
openssl s_client -connect api.bitacora.example:443 -servername api.bitacora.example </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
```

🔬 Para `server/api.pem` del proyecto sale `HjvVwpOMkSx7CifAd2M9T2jCptpKdzYQj1SSP3pAynk=`. `SecurityTest` lo calcula igual, en Kotlin (`MessageDigest` sobre `publicKey.encoded`), y lo compara con lo que hay en el APK.

Se puede fijar la clave del servidor (la hoja) o la de una CA intermedia. La de la hoja es la más estricta; la de la intermedia sobrevive a un cambio de clave del servidor, pero acepta cualquier certificado de esa CA.

## 2. En la configuración

```xml
<domain-config>
    <domain includeSubdomains="false">api.bitacora.example</domain>
    <pin-set expiration="2027-12-01">
        <pin digest="SHA-256">HjvVwpOMkSx7CifAd2M9T2jCptpKdzYQj1SSP3pAynk=</pin>
        <pin digest="SHA-256">G7HF5errMDYsEUVBLTK9MKVx/Q00gIPrcjFsPBiL/qs=</pin>
    </pin-set>
</domain-config>
```

Basta con que **una** clave de la cadena coincida con **un** pin. Y la cadena tiene que ser válida igual: el pin se comprueba además de la CA, no en su lugar.

🔬 En el emulador, la app del ejercicio 02 con dos pines que no son los de `www.google.com`, pidiendo `https://www.google.com/`:

```
javax.net.ssl.SSLHandshakeException: Pin verification failed
```

La conexión se corta en el *handshake*: no sale la petición, ni las cabeceras, ni el token.

Lo aplica la plataforma: vale para `HttpURLConnection` y para OkHttp, que en Android usa el `TrustManager` del sistema. OkHttp tiene además su propio `CertificatePinner`, en código; con la configuración de red no hace falta, y los pines quedan donde está el resto de la política de red.

## 3. La clave de reserva

El día que el servidor cambie de clave (porque se filtró, porque se renueva), las versiones instaladas solo aceptan los pines que llevan dentro. Si ninguno es el de la clave nueva, **dejan de conectar** hasta que la persona actualice. Con una app que sincroniza en segundo plano, ni se entera.

Por eso hay siempre dos pines:

1. El de la clave que usa hoy el servidor.
2. El de una **clave de reserva**, generada ya, guardada fuera del servidor y sin usar.

Cuando haya que cambiar, el servidor pasa a la de reserva (que las apps ya aceptan), y la siguiente versión de la app trae una reserva nueva. 🔬 En el proyecto, `server/backup.pem` es solo una clave pública: no hay certificado, porque aún no lo usa nadie.

## 4. La caducidad

`expiration="2027-12-01"`: a partir de esa fecha, Android deja de comprobar los pines de ese dominio y vale cualquier certificado de confianza. Es la red de seguridad para la versión que nadie actualiza: un móvil con una app de hace dos años vuelve a conectar, sin pinning, en vez de quedarse sin servicio para siempre.

La fecha va más allá de la vida esperada de la clave, y cada versión nueva la empuja.

## 5. Cuándo no fijar

| Caso | Por qué |
| --- | --- |
| Servidores que no controlas (Maps, un CDN) | Cambian de clave sin avisarte |
| Sin un plan de rotación escrito | El día que haga falta, la app se queda sin red |
| Para «que no se pueda ver la API» | Con root, una herramienta como Frida quita el pinning en tiempo de ejecución. Protege a la persona de un tercero en la red, no al servidor de la persona |

En la Bitácora se fija **solo** `api.bitacora.example`: el servidor de campo es nuestro, y el token viaja hacia él.

## ⚠️ Errores comunes

| Error | Por qué |
| --- | --- |
| Un solo pin | El primer cambio de clave deja sin red a todas las versiones instaladas |
| Pin del certificado en lugar del de la clave (`openssl x509 -outform der \| sha256`) | Cambia con cada renovación, aunque la clave sea la misma |
| Sin `expiration` | Una versión vieja con pines viejos no vuelve a conectar nunca |
| `includeSubdomains="true"` sin pensar | Fija también `cdn.`, `mail.` y lo que venga, con las claves de `api.` |
| Fijar en debug sin `overridePins` para las CA de la persona | El proxy no puede mirar el tráfico de la API |

## 🧪 Comprueba que lo entendiste

1. ¿Por qué se fija la clave y no el certificado?
2. ¿Qué tiene que coincidir con qué para que pase la conexión?
3. ¿Para qué sirve la clave de reserva? ¿Por qué no tiene certificado?
4. ¿Qué pasa a partir de la fecha de `expiration`?
5. ¿Protege el pinning tu API de alguien que tiene root en su móvil?

---

← [02 · Network Security Config](02-network-security-config.md) · Siguiente: [04 · Componentes exportados y PendingIntent](04-componentes-exportados-y-pendingintent.md)
