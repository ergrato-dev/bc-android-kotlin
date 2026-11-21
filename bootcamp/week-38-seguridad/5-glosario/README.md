# Glosario — Semana 38

| Término | Definición |
| --- | --- |
| **AEAD** | Cifrado autenticado con datos asociados: cifra, detecta cualquier cambio y comprueba un contexto que no se cifra |
| **`AndroidKeystore`** | La clase de `tink-android` que crea una clave AES-GCM en el Keystore y da una `Aead` con ella. Lo que recomienda Tink en lugar de `AndroidKeysetManager` |
| **CA (autoridad de certificación)** | Quien firma los certificados TLS. Android confía en las del sistema; desde `targetSdk` 24, no en las de la persona |
| **Cifrado envolvente** | Una clave cifra los datos y otra (la maestra) cifra esa clave: la maestra puede ser lenta o estar en hardware |
| **Clave de reserva** | Una clave generada y guardada sin usar, cuyo pin ya va en la app: el servidor pasa a ella si tiene que cambiar la suya |
| **Datos asociados** | En AEAD, un texto que no se cifra pero se comprueba al descifrar: el «para qué» de lo cifrado |
| **`dataExtractionRules`** | Las reglas de lo que va a la copia de Google y al pasar a otro móvil, desde Android 12 (`res/xml`) |
| **`debug-overrides`** | La parte de la configuración de red que solo se aplica si la app es depurable |
| **`EncryptedSharedPreferences`** | Las preferencias cifradas de `androidx.security:security-crypto`, obsoletas en la 1.1.0 |
| **`exported`** | Atributo de un componente: si otras apps pueden abrirlo |
| **`FLAG_IMMUTABLE`** | Un `PendingIntent` cuyo `Intent` no puede cambiar quien lo recibe |
| **`fullBackupContent`** | Las reglas de las copias de seguridad hasta Android 11 |
| **Keyset** | En Tink, un conjunto de claves con una primaria, con la que se cifra; las demás sirven para descifrar lo de antes |
| **Keystore** | El almacén de claves de Android: la clave se crea dentro, se usa desde fuera y no se puede copiar |
| **Network Security Config** | El XML que dice qué tráfico deja salir la app y en qué certificados confía |
| **Nonce** | Un número que se usa una sola vez con una clave; en AES-GCM, 12 bytes aleatorios por cifrado |
| **OSV** | La base de datos abierta de vulnerabilidades, con una API para consultar paquetes y versiones |
| **`PendingIntent`** | Un `Intent` que otro (el sistema de notificaciones) ejecuta con la identidad de tu app |
| **Pin** | El SHA-256 en Base64 de la clave pública (SPKI) de un servidor, que la app exige además de la CA |
| **Pinning** | Aceptar de un servidor solo las claves fijadas en la app |
| **Play Integrity** | API de Google Play que da al servidor un veredicto firmado sobre la app, el dispositivo y la cuenta |
| **SPKI** | *SubjectPublicKeyInfo*: la clave pública de un certificado, con su algoritmo. Lo que se fija |
| **Tink** | La biblioteca de criptografía de Google: primitivas con parámetros seguros, sin elegir modos ni nonces |
