import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun defaultsAreLeftOut() {
        assertEquals("""{"sensor":"s1","value":21.5}""", encodeReading(Reading("s1", 21.5)))
        assertEquals("""{"sensor":"s1","value":21.5,"unit":"F"}""", encodeReading(Reading("s1", 21.5, "F")))
    }

    @Test
    fun unknownKeysAreIgnoredAndMissingOnesFilled() {
        assertEquals(Reading("s1", 3.0), decodeReading("""{"sensor":"s1","value":3,"extra":true}"""))
        assertEquals(Reading("s1", 3.0, "K"), decodeReading("""{"unit":"K","sensor":"s1","value":3.0}"""))
    }

    @Test
    fun renamedAndTransientProperties() {
        assertEquals("""{"sensor_id":"s1","location":"norte"}""", encodeSensor(Sensor("s1", "norte", cache = "x")))
        assertEquals(Sensor("s2", "sur"), decodeSensor("""{"sensor_id":"s2","location":"sur"}"""))
    }

    @Test
    fun enumsAndValueClassesAreFlat() {
        val task = Task(Tag("riego"), Priority.HIGH)
        assertEquals("""{"tag":"riego","priority":"high"}""", encodeTask(task))
        assertEquals(task, decodeTask("""{"tag":"riego","priority":"high"}"""))
    }

    @Test
    fun sealedTypesCarryTheirName() {
        assertEquals("""{"type":"created","code":"A-1"}""", encodeEvent(Event.Created("A-1")))
        assertEquals("""{"type":"cleared"}""", encodeEvent(Event.Cleared))
        assertEquals(Event.Created("B-2"), decodeEvent("""{"type":"created","code":"B-2"}"""))
        assertEquals(Event.Cleared, decodeEvent("""{"type":"cleared"}"""))
    }

    @Test
    fun decodeSafelyExplainsEveryFailure() {
        assertEquals(Decoded.Ok(Reading("s1", 2.0)), decodeSafely("""{"sensor":"s1","value":2}"""))
        assertEquals(Decoded.Invalid("campo obligatorio ausente"), decodeSafely("""{"value":1}"""))
        assertEquals(Decoded.Invalid("sensor en blanco"), decodeSafely("""{"sensor":" ","value":1}"""))
        assertEquals(Decoded.Invalid("JSON inválido"), decodeSafely("""{"sensor":"a","value":[1]}"""))
        assertEquals(Decoded.Invalid("JSON inválido"), decodeSafely("""{"sensor":"""))
    }

    @Test
    fun customSerializerWritesTwoDecimals() {
        assertEquals("""{"value":"12.50"}""", encodeMeasurement(Measurement(12.5)))
        assertEquals("""{"value":"3.00"}""", encodeMeasurement(Measurement(3.0)))
        assertEquals(Measurement(7.5), decodeMeasurement("""{"value":"7.5"}"""))
    }
}
