# Ejercicio 02 — Los enlaces

⏱️ 2 h · Teoría previa: 04 · Haz antes el ejercicio 01

## Objetivo

`bitacora://observation/A-2` abre la Bitácora en esa observación, con la sesión debajo, para que atrás vuelva a la lista. Un enlace que no encaja, o con un código que no existe, abre la sesión. Y el lanzador sigue abriendo la sesión.

## Cómo está montado

`starter/` es el ejercicio 01 resuelto, con un cambio: `BitacoraNav` recibe `start`, la lista de claves con que empieza el back stack, y `MainActivity` la calcula con `startOf(intent) { ... }`, que por ahora siempre devuelve la sesión. `startOf` está en `Links.kt`.

Los tests (`app/src/test/kotlin/dev/bitacora/LinkTest.kt`, no los toques) prueban el manifiesto con `queryIntentActivities`, `startOf` sola y la actividad abierta con un enlace. Al empezar, fallan 4 de 8: los que esperan la sesión, o que el enlace no sea de nadie, ya están en verde.

## Pasos

1. **El manifiesto**: en `MainActivity`, un segundo `intent-filter` con `android.intent.action.VIEW`, las categorías `DEFAULT` y `BROWSABLE`, y `<data android:scheme="bitacora" android:host="observation" />` (teoría 04).
2. **El buscador** (`Links.kt`): un `UriDeepLinkMatcher("bitacora://observation/{code}".toUri(), ObservationKey.serializer())` con `.withBackStack { listOf(SessionKey, it.key) }`. Los imports, de `androidx.navigation3.runtime.deeplink`, también `invoke` y `withBackStack`.
3. **`startOf`**: sin `intent.data`, la sesión. Si no, `match(DeepLinkRequest(intent))`; si encaja y `exists(código)`, su back stack, y si no, la sesión.
4. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew build`. Con un emulador, prueba también `adb shell am start -W -a android.intent.action.VIEW -d "bitacora://observation/A-1"`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la línea de `intent.data == null`. ¿Qué test falla, con qué mensaje? ¿Por qué el `Intent` del lanzador pasa y el explícito no? Restáuralo.
   - (b) Quita el `takeIf { exists(...) }`. ¿Qué dos tests fallan, con qué mensajes? ¿Qué le pasaría a la app con un enlace a `A-99` mandado por otra app? Restáuralo.
   - (c) Quita el `withBackStack` (y devuelve `listOf(match.key)`). ¿Qué tests fallan? ¿Qué hace atrás para quien llega por el enlace? Restáuralo.
   - (d) Quita la categoría `DEFAULT`. ¿Qué test falla, y qué devuelve `queryIntentActivities`? Restáuralo.
   - (e) Quita la categoría `BROWSABLE`. ¿Falla algún test? Escribe uno tuyo con `addCategory(Intent.CATEGORY_BROWSABLE)` que lo note. ¿Desde dónde dejaría de abrirse el enlace? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `DeepLinkRequest(intent)` no compila | Falta `import androidx.navigation3.runtime.deeplink.invoke` |
| `Unresolved reference 'withBackStack'` | Falta `import androidx.navigation3.runtime.deeplink.withBackStack` |
| `DeepLinkRequest must have either a uri or extras.` | Llega un `Intent` sin `Uri` ni acción: la guarda de `intent.data` va antes (teoría 04) |
| `androidSendsTheLinksToTheBitacora` da `[]` | Falta `DEFAULT`, o el esquema o el host no son `bitacora` y `observation` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5, con tu test del apartado (e).
