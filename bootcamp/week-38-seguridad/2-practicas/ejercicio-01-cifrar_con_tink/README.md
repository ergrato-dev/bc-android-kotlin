# Ejercicio 01 — Cifrar con Tink

⏱️ 1 h 30 min · Teoría previa: 01

## Objetivo

Una `Vault` que guarda textos de la Bitácora (un token, una nota privada) para que solo los abra quien tiene la clave maestra. Es el cifrado envolvente de Tink: una clave de Tink cifra los textos, y la maestra cifra esa clave en disco. En Android, la maestra sería `AndroidKeystore.getAead(alias)`; aquí, en la JVM, otra `Aead` de Tink, y los tests no necesitan un móvil.

Al terminar:

- lo que sale de `seal` no contiene el texto, y cifrar lo mismo dos veces da resultados distintos;
- un contexto distinto, o un bit cambiado, y `open` devuelve `null`;
- la clave se guarda cifrada con la maestra, y sin ella no se lee;
- `rotate` cambia la clave con que se cifra, y lo de antes se sigue abriendo.

## Cómo está montado

`starter/` es un proyecto de la JVM, sin Android:

- `build.gradle.kts`: Kotlin y Tink 1.24.0 (`com.google.crypto.tink:tink`). En Android sería `tink-android`, con la misma API.
- `src/main/kotlin/dev/bitacora/vault/Vault.kt`: la clase, con su API. No cifra: guarda `"contexto:texto"` tal cual.
- `src/test/kotlin/dev/bitacora/vault/VaultTest.kt`: 9 tests. No los toques.

Compila, y al empezar fallan 7 de 9.

## Pasos

1. **Lee los tests** y apunta qué propiedad de AEAD comprueba cada uno (teoría 01, apartado 3). ¿Por qué ya pasan `itOpens` e `itSurvivesARestart`?
2. **La clave** (teoría 01, apartado 4): en `Vault`, un `KeysetHandle`. Si `keyset` existe, se lee con `TinkJsonProtoKeysetFormat.parseEncryptedKeyset` y la maestra; si no, se genera con `PredefinedAeadParameters.AES256_GCM` y se guarda con `serializeEncryptedKeyset`. Usa siempre la configuración de `AeadConfig2026.get()`: las versiones sin ella están obsoletas y, con `allWarningsAsErrors`, no compilan.
3. **`seal` y `open`**: la primitiva `Aead` del keyset. El contexto va como datos asociados. `open` devuelve `null` si `decrypt` lanza `GeneralSecurityException`, y solo entonces.
4. **`rotate`**: `KeysetHandle.newBuilder(handle)`, una entrada nueva con `makePrimary()`, y guárdalo. ¿Qué falta si `build()` dice `no key creator for this class was registered`?
5. `./gradlew test`: 9 de 9. Después, `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) Imprime el tamaño de `seal(token, "token")`. ¿Cuántos bytes más que el token? ¿De dónde sale cada uno?
   - (b) Abre `keyset.json` (en un test, imprime su contenido). ¿Qué se ve y qué no?
   - (c) Rota dos veces y mira `keysetInfo`: ¿cuántas claves hay y cuál es la primaria?
   - (d) Cifra el keyset con `KEYSET_CONTEXT` (o lo que hayas usado) y léelo con otro contexto. ¿Qué pasa? ¿Para qué sirve?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `'static fun generateNew(parameters: Parameters!): KeysetHandle!' is deprecated` | Pásale `AeadConfig2026.get()` como segundo argumento |
| `no key creator for this class was registered` | El builder no tiene configuración: `.setConfiguration(config)` |
| `anotherMasterKeyCannotReadTheKeyset` no lanza | La clave se lee del archivo en el constructor (o al primer uso) con la maestra recibida, no se genera otra |
| `oldTextsOpenAfterRotating`: los 5 primeros bytes son iguales | La nueva no es la primaria: `makePrimary()` |
| `theKeysetIsEncrypted` ve `keyData` | Se guardó con un formato sin cifrar: `serializeEncryptedKeyset` |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con el paso 6.
