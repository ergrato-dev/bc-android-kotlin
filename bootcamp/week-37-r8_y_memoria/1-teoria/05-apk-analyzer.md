# APK Analyzer

> "La app ocupa mucho" no es un dato. Cuánto, en qué, y qué ha cambiado desde la versión anterior, sí. **APK Analyzer** abre un APK o un AAB y lo cuenta: el tamaño de descarga, cada archivo, los dex con sus clases y métodos, los recursos. Está en Android Studio y en la terminal, como `apkanalyzer`. Esta teoría explica qué mirar y cómo comparar dos versiones.

## 🎯 Objetivos

- Distinguir el tamaño del archivo del tamaño de descarga
- Medir los dex: clases, métodos definidos y referencias
- Comparar dos APK y encontrar qué ha crecido
- Saber qué quita `isShrinkResources` y qué queda en `resources.arsc`

## 1. Dónde está

- **Android Studio**: *Build → Analyze APK…*, o arrastra el APK al editor.
- **Terminal**: `$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer`. Es lo que usan los tests de la Bitácora desde la semana 15 (`src/test/kotlin/Apk.kt`).

## 2. Dos tamaños

```bash
apkanalyzer apk file-size app-field-release-unsigned.apk       # 2254321
apkanalyzer apk download-size app-field-release-unsigned.apk   # 2070138
```

`file-size` es lo que ocupa el archivo. `download-size` es una estimación de lo que se descarga, comprimido. Lo que importa a quien instala es el de descarga, y lo que ocupa instalado, que es más (el dex compilado por ART, teoría 03 de la semana 36).

Con un AAB, Play genera un APK por dispositivo (solo su densidad, su ABI, su idioma). Para saber lo que baja un móvil concreto, `bundletool get-size total`.

## 3. Los dex

```bash
apkanalyzer dex list app.apk           # classes.dex, classes2.dex
apkanalyzer dex references app.apk     # referencias a métodos de cada dex
apkanalyzer dex packages app.apk       # cada paquete, clase y método, con lo que ocupa
```

`dex references` cuenta los métodos que cada dex **nombra**: los suyos y los que llama. Cada dex admite 65 536; por encima, hace falta otro (multidex, siempre activo con `minSdk` 21 o más).

En la Bitácora (medido):

| | Sin R8 | Con R8 |
| --- | --- | --- |
| `classes.dex` | 53 976 | 14 827 |
| `classes2.dex` | 65 506 | 15 499 |
| `classes3.dex` | 64 809 | |
| `classes4.dex` | 15 471 | |
| **Total** | **199 762** | **30 326** |

`dex packages` sirve para buscar quién ocupa. Su salida es una línea por paquete (`P`), clase, método y campo, con una letra de estado (`d` definido, `r` solo nombrado), los métodos definidos, los nombrados y los bytes. Tras R8, casi todas las clases están en el paquete sin nombre (`fk`, `gf`): con el mapping, `apkanalyzer` las devuelve a su paquete.

```bash
apkanalyzer dex packages --defined-only --proguard-mappings mapping.txt app.apk \
  | grep "^P" | sort -t$'\t' -k3 -n -r | head
```

En la release de la Bitácora (medido), de 3 081 850 bytes de dex:

| Paquete | Bytes |
| --- | --- |
| `androidx` | 2 100 556 |
| `androidx.compose` | 1 441 532 |
| `kotlinx` | 243 115 |
| `com.google` | 162 165 |
| `io.ktor` | 150 866 |
| `kotlin` | 149 730 |
| `okhttp3` | 123 473 |
| `dev.bitacora` | 60 967 |

La Bitácora es un 2 % de su propio dex. Compose, casi la mitad.

## 4. Comparar dos versiones

```bash
apkanalyzer apk compare --different-only antes.apk despues.apk
```

Saca, archivo por archivo, el tamaño de antes, el de después y la diferencia. Es lo primero que se mira cuando una versión crece sin motivo: una biblioteca nueva, un recurso enorme, una regla `{ *; }` que conserva un paquete entero.

En una pull request, el tamaño de descarga de la release al lado del de `main` evita sorpresas. Los tests de la Bitácora ponen un techo: `ShrinkTest.theReleaseIsShrunk` falla si el APK de field pasa de 4 MB.

## 5. Los recursos

`isShrinkResources` quita los recursos que nadie usa, después de que R8 haya quitado el código. `resources.txt` (en `build/outputs/mapping`) dice por qué se queda o se va cada uno:

```
animator:fragment_close_enter:2130837504 reachable from int androidx.fragment.R$animator.fragment_close_enter
drawable:old_logo:2131034131 is not reachable.
```

En el ejercicio 02, `old_logo` está en la debug y no en la release: `aapt2 dump resources` lo lista en una y no en la otra (medido). En la Bitácora, `resources.arsc` pasa de 150 764 a 32 260 bytes: con el código que R8 quita, se van también los recursos que solo usaba ese código.

Los recursos que se usan por su nombre (`getIdentifier("logo_" + crop, "drawable", ...)`) son la reflexión de los recursos: el reductor no los ve. Se conservan con un `res/raw/keep.xml` (en el ejercicio 02, con este archivo `old_logo` vuelve a la release: medido):

```xml
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@drawable/old_*" />
```

Y `localeFilters` (semana 15) quita los idiomas que la app no tiene, de todas las bibliotecas: eso no lo hace el reductor.

## ⚠️ Errores comunes

| Error | Por qué |
| --- | --- |
| Mirar el tamaño de la debug | No pasa por R8, lleva LeakCanary y no está comprimida igual. Se mide la release |
| Confundir referencias con métodos definidos | `dex references` cuenta también los métodos de Android que la app llama |
| `getIdentifier` sin `tools:keep` | El recurso no está en la release, y `getIdentifier` devuelve 0 |
| Comparar el APK de un AAB con un APK universal | Uno tiene todas las densidades e idiomas, y el otro no |

## 🧪 Comprueba que lo entendiste

1. ¿Qué diferencia hay entre `file-size` y `download-size`? ¿Cuál importa a quien instala?
2. ¿Por qué la Bitácora necesitaba 4 dex sin R8 y 2 con él?
3. ¿Qué comando usarías para saber por qué la versión nueva pesa 300 KB más?
4. ¿Qué hace falta para que un recurso que se busca por su nombre llegue a la release?

---

← [04 · El mapping y retrace](04-mapping-y-retrace.md) · Siguiente: [06 · La memoria](06-la-memoria.md)
