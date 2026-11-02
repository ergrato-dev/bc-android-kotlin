# Claves y estado de los elementos

> Compose recuerda el estado de cada composable por su sitio en la composición. En una lista, sin más información, el sitio es la posición: el primero, el segundo... Si llega una observación nueva arriba, todas bajan un puesto, pero su estado no: la fila que estaba abierta era "la primera", y la primera ahora es otra. La clave le dice a Compose quién es cada elemento, no dónde está. Esta teoría es qué pasa sin ella, qué vale como clave y para qué sirve el `contentType`.

## 🎯 Objetivos

- Ver qué le pasa al estado de un elemento cuando la lista cambia, con clave y sin ella
- Elegir una clave que funcione: única, estable y que quepa en un `Bundle`
- Dar clave también a los elementos de un bucle fuera de una lista perezosa

## 1. Sin clave, el estado se queda en su sitio

Cada fila guarda si está abierta:

```kotlin
@Composable
fun Item(name: String) {
    var open by rememberSaveable { mutableStateOf(false) }
    Text("$name: $open", Modifier.clickable { open = !open })
}
```

La lista empieza con `b` y `c`. El usuario abre `b`, y llega `a` delante:

> 🔬 Medido en la sonda:
>
> | Lista | Lo que se ve |
> | --- | --- |
> | `items(list) { Item(it) }` | `a: true`, `b: false`, `c: false` |
> | `items(list, key = { it }) { Item(it) }` | `a: false`, `b: true`, `c: false` |
>
> Sin clave, la abierta es la de arriba, `a`, que el usuario no ha tocado. Con clave, el estado sigue a `b`.

Pasa lo mismo con `remember`, con una animación o con el foco de un campo: todo el estado de una fila va con su clave. Y sin clave, Compose tampoco puede saber que una fila solo se ha movido: la recompone con los datos de otra.

## 2. Qué vale como clave

Tres condiciones:

1. **Única** en la lista. Dos elementos con la misma clave:
   > 🔬 Medido en la sonda: `IllegalArgumentException: Key "a" was already used. If you are using LazyColumn/Row please make sure you provide a unique key for each item.`
2. **Estable**: la misma para el mismo dato, aunque cambien sus campos. El código de la observación (`"A-12"`) lo es; su descripción no, porque el usuario puede editarla; su posición en la lista tampoco, que es lo que pasa sin clave.
3. **Que quepa en un `Bundle`**: Compose guarda el estado de las filas por su clave, y lo guarda en el `Bundle` para sobrevivir a la muerte del proceso.
   > 🔬 Medido en el ejercicio 02 (paso 6d): con `key = { it }`, la observación entera, fallan los 8 tests con `IllegalArgumentException: Type of the key Observation(code=A-1, ...) is not supported. On Android you can only use types which can be stored inside the Bundle.` Una `data class` no es `Parcelable`.

`String`, `Int` y `Long` valen siempre. La clave de una fila de la Bitácora es `it.code`; la de una cabecera, el nombre de su nivel. Una cabecera y una fila no pueden compartir clave.

## 3. contentType

`items(..., contentType = { "observation" })` le dice a Compose qué elementos tienen la misma forma. Al desplazarse, reutiliza lo que ya ha compuesto de una fila que sale para la que entra, si son del mismo tipo. Sin él, todo es del mismo tipo, y una cabecera se puede reutilizar para una fila. No cambia lo que se ve: es rendimiento, y en esta semana ningún test lo nota.

## 4. Fuera de una lista perezosa

Un `for` dentro de una `Column` tiene el mismo problema, y se arregla con `key` de `androidx.compose.runtime`:

```kotlin
Column {
    for (observation in observations) {
        key(observation.code) { ObservationRow(observation) }
    }
}
```

> 🔬 Medido en la sonda: el bucle sin `key` da `a: true, b: false, c: false`; con `key(it) { ... }`, `a: false, b: true, c: false`. Lo mismo que en el `LazyColumn`.

## ✅ Comprueba

- [ ] La Bitácora numera las observaciones de 1 en adelante y nunca reutiliza un código. ¿Por qué el código vale de clave y el índice no?
- [ ] ¿Por qué una `data class` normal no vale de clave, si se puede comparar con `==`?
- [ ] ¿Qué test del ejercicio 02 falla sin clave, y qué vería el usuario?
