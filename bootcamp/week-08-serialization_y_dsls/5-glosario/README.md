# Glosario — Semana 08

| Término | Definición |
| --- | --- |
| **@DslMarker** | Anotación que impide que, dentro de un bloque interior, se llame sin querer a los miembros del constructor exterior |
| **@SerialName** | Cambia el nombre de una propiedad, un enum o un subtipo en el JSON |
| **@Serializable** | Marca una clase para que el plugin genere su serializador |
| **@Transient** | La propiedad no se escribe ni se lee; necesita valor por defecto |
| **buildJsonObject** | DSL de kotlinx.serialization para construir un árbol JSON |
| **Builder** | Objeto mutable que una lambda rellena y cuyo `build()` valida y devuelve el objeto inmutable |
| **classDiscriminator** | Nombre del campo que identifica el subtipo en una jerarquía sellada (`type` por defecto) |
| **DSL** | Lenguaje específico de un dominio, escrito con las funciones del propio lenguaje |
| **Discriminador** | Campo del JSON que dice a qué subtipo corresponde un objeto |
| **encodeDefaults** | Opción de `Json`: escribir también los valores por defecto |
| **Entrada no confiable** | Dato que viene de fuera y puede estar roto: se valida, no se da por bueno |
| **Golden (test)** | Prueba que compara con un texto esperado exacto, escrito a mano |
| **ignoreUnknownKeys** | Opción de `Json`: ignorar las claves que la clase no tiene |
| **infix** | Función de un argumento que se llama sin punto ni paréntesis: `a to b` |
| **Json** | El objeto que configura y ejecuta la conversión; se crea una vez y se reutiliza |
| **JsonElement** | El árbol de un JSON: `JsonObject`, `JsonArray`, `JsonPrimitive`, `JsonNull` |
| **KSerializer** | Serializador de un tipo: un `descriptor` y las funciones `serialize` y `deserialize` |
| **Lambda con receptor** | Lambda de tipo `T.() -> Unit` en la que `this` es un `T` |
| **MissingFieldException** | Falta una propiedad obligatoria al leer (experimental en 1.12.0) |
| **namingStrategy** | Opción de `Json` para convertir nombres, por ejemplo a `snake_case` |
| **operator** | Función con nombre fijo (`get`, `contains`, `plus`, `unaryPlus`...) que habilita una sintaxis |
| **parseToJsonElement** | Lee un texto JSON como árbol |
| **Plugin de serialization** | Plugin del compilador que genera los serializadores |
| **prettyPrint** | Opción de `Json`: sangría para leer a ojo |
| **SerializationException** | Raíz de los errores de la biblioteca; extiende `IllegalArgumentException` |
| **Versión del formato** | Número en el archivo que permite evolucionarlo y rechazar lo que no se entiende |
