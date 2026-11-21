# Semana 38 — Seguridad

> La Bitácora de la semana 37 es pequeña y rápida, pero confía en todo: en cualquier CA, en cualquier enlace, en que nadie copie sus archivos. Esta semana el servidor de campo pide un token, y la app tiene que guardarlo donde no se pueda copiar (el **Keystore**, con **Tink**), mandarlo solo a su servidor (la **Network Security Config**, con **pinning**) y no perderlo en una copia de seguridad. Por el camino: qué puertas deja abiertas una app y cómo se cierran, qué secretos puede llevar un APK (ninguno de verdad), cómo se escanean las dependencias y qué ofrece **Play Integrity**.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Cifrar** datos con AEAD de Tink y una clave del Keystore, y **explicar** por qué `EncryptedSharedPreferences` ya no es la respuesta
- **Escribir** una Network Security Config sin tráfico en claro, con las CA de la persona solo en debug
- **Fijar** un servidor con dos pines y una caducidad, y **calcular** un pin con `openssl`
- **Revisar** lo que exporta una app y **crear** un `PendingIntent` explícito e inmutable
- **Sacar** las claves del repositorio y **decidir** qué va en las copias de seguridad
- **Escanear** las dependencias con OSV y **explicar** qué da Play Integrity y dónde se comprueba

---

## 📋 Prerrequisitos

- [Semana 37](../week-37-r8_y_memoria/README.md) completada.
- El SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../docs/setup.md#fase-3-el-sdk-de-android)), con `cmdline-tools` (`apkanalyzer`) y `build-tools;36.0.0` (`aapt2`).
- `openssl`, para calcular los pines.
- **Para el Keystore de verdad y los enlaces**: KVM o un móvil, como en las semanas 36 y 37. El ejercicio 01 y los tests de la semana no lo necesitan.

---

## 🗂️ Estructura de la Semana

```
week-38-seguridad/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 6 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios: cifrar con Tink, y una app expuesta
├── 3-proyecto/                  # la capa 37 de tu Bitácora: segura
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · El Keystore y Tink](1-teoria/01-keystore-y-tink.md) | Qué proteger y de quién; el Keystore; AEAD; Tink, el keyset y la rotación; `EncryptedSharedPreferences`, obsoleta; el token de la Bitácora | 45 min |
| [02 · Network Security Config](1-teoria/02-network-security-config.md) | `base-config`, `domain-config` y `debug-overrides`; el tráfico en claro; las CA; leerla en el APK | 30 min |
| [03 · Certificate pinning](1-teoria/03-certificate-pinning.md) | Qué se fija; calcular un pin; dos pines y una caducidad; cuándo no fijar | 35 min |
| [04 · Componentes exportados y PendingIntent](1-teoria/04-componentes-exportados-y-pendingintent.md) | `exported`; lo que exportan las bibliotecas; lo que entra por un enlace; `PendingIntent` explícito e inmutable; lo que ve lint | 40 min |
| [05 · Secretos y copias de seguridad](1-teoria/05-secretos-y-copias.md) | Lo del APK es público; claves por propiedades de Gradle; una clave filtrada; qué va en las copias | 30 min |
| [06 · Las dependencias y Play Integrity](1-teoria/06-dependencias-y-play-integrity.md) | La verificación; escanear con OSV; lo del build y lo del APK; Play Integrity, en el servidor | 30 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Cifrar con Tink](2-practicas/ejercicio-01-cifrar_con_tink/README.md) | Una `Vault` con AEAD, el keyset cifrado con una clave maestra y rotación, en la JVM | 1 h 30 min |
| [02 · Una app expuesta](2-practicas/ejercicio-02-una_app_expuesta/README.md) | Cerrar las puertas de una app: componentes, `PendingIntent`, red en claro, pines y una clave en el repositorio | 2 h |

### Proyecto

La Bitácora segura: el token del servidor cifrado con el Keystore, el enlace de vinculación con confirmación, la red fijada, las copias sin el token y las dependencias escaneadas.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (6 archivos) | 3 h 30 min |
| Ejercicio 01 | 1 h 30 min |
| Ejercicio 02 | 2 h |
| Proyecto | 5 h |
| Recursos y glosario | 1 h |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| El pin de un certificado | `openssl x509 -in api.pem -pubkey -noout \| openssl pkey -pubin -outform der \| openssl dgst -sha256 -binary \| openssl enc -base64` |
| Lo que se exporta | `apkanalyzer manifest print app.apk \| grep -B3 'exported="true"'` |
| La configuración de red de un APK | `apkanalyzer resources value --type xml --config default --name network_security_config app.apk`, y esa ruta con `aapt2 dump xmltree --file` |
| Abrir un componente como otra app | `adb shell am start -n paquete/.Actividad --es clave valor` |
| Probar un enlace | `adb shell am start -a android.intent.action.VIEW -d "bitacora://pair?token=…" dev.bitacora.agro.debug` |
| Ver un archivo de la debug | `adb shell run-as dev.bitacora.agro.debug ls -l files` |
| Una clave en el historial | `git log -S ak_live_ --oneline` |
| Escanear las dependencias | `osv-scanner scan source -L gradle/verification-metadata.xml`, o la API de OSV (teoría 06) |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 9 de 9 en verde y `NOTAS.md` con el paso 6
2. ✅ Ejercicio 02: 8 de 8 en verde, `./gradlew build` en verde, y `NOTAS.md` con el paso 8
3. ✅ Proyecto: rama `week-38` con el token cifrado, el enlace confirmado, la red fijada y las copias sin el token; `SecurityTest` y los tests de la semana 37 en verde, y al menos 5 tests nuevos con el valor esperado escrito a mano
4. ✅ Pull request `week-38` → `main` con la salida de `./gradlew test`
5. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto), con `ANDROID_HOME` definida:

```bash
./gradlew test
```

Los starters compilan y los tests nuevos empiezan en rojo. Al terminar, `./gradlew build` tiene que acabar con `BUILD SUCCESSFUL`. El Keystore de verdad y los enlaces necesitan un dispositivo; los tests, no.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| [Semana 37](../week-37-r8_y_memoria/README.md) | **Semana 38** | Semana 39 (próxima tanda) |

← [Volver al inicio del bootcamp](../../README.md)
