# El modelo de permisos

> Una app de Android vive en su propia caja: no puede leer los contactos, saber dónde está el móvil ni encender la cámara si nadie le deja. Los permisos son las llaves de esa caja. Unas las da Android al instalar, sin preguntar. Otras las tiene que dar el usuario, en el momento en que la app las necesita, y las puede quitar cuando quiera.

## 🎯 Objetivos

- Distinguir los permisos normales, los peligrosos y los de firma
- Declarar un permiso en el manifiesto y comprobar si está concedido
- Ver qué permisos lleva un APK, también los que no has escrito tú

## 1. Declarar

Todo permiso empieza en el manifiesto:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

Lo que no está declarado no se puede conceder: en un móvil, pedir un permiso que no está en el manifiesto da un "no" inmediato, sin diálogo. Robolectric no lo comprueba:

> 🔬 Medido en el ejercicio 01, paso 7d: sin `ACCESS_FINE_LOCATION` en el manifiesto, los 7 tests siguen en verde y lint no dice nada.

## 2. Tres niveles de protección

| Nivel | Quién lo concede | Ejemplos |
| --- | --- | --- |
| **Normal** | Android, al instalar, sin preguntar | `INTERNET`, `VIBRATE` |
| **Peligroso** (*dangerous*) | El usuario, en tiempo de ejecución, con un diálogo | `ACCESS_FINE_LOCATION`, `CAMERA`, `READ_CONTACTS` |
| **De firma** (*signature*) | Android, solo a las apps firmadas con la misma clave que la que lo define | `dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` |

Los peligrosos son los que dan acceso a datos personales o a sensores. Desde Android 6 (API 23) no basta con declararlos: la app tiene que pedirlos mientras se usa (teoría 02). Con `minSdk = 26`, la Bitácora siempre está en ese modelo.

El último de la tabla no lo has escrito tú:

> 🔬 Medido con `apkanalyzer manifest permissions` en la sonda: el APK lleva `dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Lo declara `androidx.core` (que llega con `androidx.activity`) para proteger los receptores que registra en código. En `apkanalyzer manifest print`, su `<permission>` lleva `android:protectionLevel="0x2"`, que es `signature`: solo tu app lo tiene.

## 3. Comprobar

```kotlin
val granted = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
```

`checkSelfPermission` dice cómo está el permiso **ahora**. No lo guardes en una variable para más tarde: el usuario puede quitarlo desde los ajustes en cualquier momento, y Android mata el proceso de la app cuando lo hace. Comprueba siempre justo antes de usar lo que protege.

## 4. Lo que hay en el APK

```bash
apkanalyzer manifest permissions app-debug.apk
```

```text
dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_COARSE_LOCATION
android.permission.CAMERA
```

> 🔬 Medido en la sonda.

Esa lista sale de fusionar tu manifiesto con los de todas tus bibliotecas (teoría 05). Un permiso que tu app no usa es un riesgo y una pregunta más de Google Play al publicar. La regla: **solo los permisos que la app usa, y cada uno justo cuando lo usa**.

## ✅ Comprueba

- [ ] ¿Qué diferencia hay entre `INTERNET` y `ACCESS_FINE_LOCATION` para el usuario?
- [ ] Tu APK lleva un permiso que no está en tu manifiesto. ¿De dónde puede salir?
- [ ] ¿Por qué no se guarda en una variable el resultado de `checkSelfPermission`?
