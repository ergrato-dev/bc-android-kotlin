# Diseñar y probar formatos y DSLs

> Un formato de archivo y un DSL son **contratos**: otros (o tú dentro de un año) dependen de ellos. Los dos se prueban de la misma forma: con valores exactos, a mano.

## 🎯 Objetivos

- Probar la serialización con textos exactos (*golden*) y con ida y vuelta
- Probar la tolerancia: lo desconocido, lo ausente, lo roto, lo futuro
- Probar un DSL con resultados y mensajes de error exactos
- Revisar un diseño con una lista de comprobación

## 1. Pruebas de serialización

Tres tipos, de más a menos específico:

**Texto exacto (*golden*)**: fija el contrato. Si alguien cambia el formato sin querer, falla.

```kotlin
@Test
fun theGoldenFile() {
    val file = LogbookFile(domain = "agro", observations = listOf(one), counts = mapOf(Level.MEDIUM to 1))
    assertEquals(
        """{"version":1,"domain":"agro","observations":[{"code":"A-1","description":"Riego","quantity":3,"measure":12.5}],"counts":{"medium":1}}""",
        encodeFile(file),
    )
}
```

El valor esperado se **escribe a mano** (con las comillas triples, que no escapan nada), no se copia de la salida del programa: si copias la salida, la prueba solo comprueba que el código es igual a sí mismo.

**Ida y vuelta**: lo que se escribe se puede leer y es igual.

```kotlin
assertEquals(file.observations, (importFile(encodeFile(file)) as Imported.Ok).observations)
```

Por sí sola no basta: un formato horrible también hace ida y vuelta. Va **junto** al texto exacto.

**Tolerancia**: una prueba por cada regla de la lección 3. Versión ausente, claves desconocidas, versión futura, JSON roto, raíz que no es objeto, cada motivo de elemento inválido con su posición.

## 2. Pruebas de DSL

```kotlin
@Test
fun logbookRejectsARepeatedCode() {
    val e = assertFailsWith<IllegalArgumentException> {
        logbook("agro") {
            observation("A-1") { description = "uno" }
            observation("A-1") { description = "otra vez" }
        }
    }
    assertEquals("code repetido: A-1", e.message)
}
```

Comprueba: el resultado con lo obligatorio y con los valores por defecto, el **orden**, el **mensaje exacto** de cada error, el bloque vacío y las combinaciones (`filter` con varias condiciones). El **mensaje** forma parte del contrato porque es lo que ve quien usa el DSL.

Lo que no se puede probar en ejecución (`@DslMarker`) se prueba **a mano una vez**: quita el marcador, escribe la llamada interior incorrecta y comprueba que compila; ponlo y comprueba que no. Es lo que pide el `NOTAS.md` del ejercicio 02.

## 3. Lista de comprobación de un diseño

| Pregunta | Si la respuesta es no |
| --- | --- |
| ¿El formato lleva versión? | Añádela ahora: después es mucho más difícil |
| ¿Cada campo nuevo tiene valor por defecto? | Los archivos viejos dejarán de leerse |
| ¿Los nombres del JSON son un contrato (`@SerialName`)? | Un renombrado interno rompe archivos |
| ¿La lectura de datos de fuera puede lanzar hacia fuera? | Devuelve un resultado (`Ok`/`Rejected`), no una excepción |
| ¿Cada motivo de rechazo es un texto estable? | No lo mezcles con `e.message` de la biblioteca |
| ¿Lo obligatorio se valida en `build()` con un mensaje claro? | El error aparecerá lejos del origen |
| ¿El DSL es opcional o hay una función normal más clara? | Quita el DSL |
| ¿Los constructores llevan `@DslMarker`? | Un bloque interior llamará al exterior sin avisar |

## 4. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| El test pasa aunque el formato cambió | El esperado se copió de la salida | Escribe el JSON esperado a mano |
| Solo hay prueba de ida y vuelta | Un formato malo también hace ida y vuelta | Añade el texto exacto |
| El test de un motivo falla al actualizar la biblioteca | Comparas con `e.message` de la biblioteca | Compara con **tus** motivos |
| El Double sale como `3.0` y esperabas `3` | JSON de `Double` conserva el `.0` | Escribe `3.0` en el esperado |
| El orden de las claves cambia | Un `Map` sin orden estable | Usa un mapa con orden (`associateWith` sobre `entries`) |
| Una prueba depende de `prettyPrint` | La sangría cambia con la versión | Prueba el formato compacto |

## ✅ Qué debes saber al terminar

- Un test de formato = texto exacto + ida y vuelta + tolerancia.
- Un test de DSL = resultado, orden, valores por defecto y mensajes exactos.
- Un formato sin versión, un campo nuevo sin valor por defecto y una lectura que lanza son los tres errores a evitar.
