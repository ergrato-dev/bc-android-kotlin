import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun saludaPorNombre() = assertEquals("Hola, Ada", saludo("Ada"))

    @Test
    fun saludoConNombreEnBlancoEsMundo() = assertEquals("Hola, mundo", saludo("   "))

    @Test
    fun resumenTieneElFormatoPedido() =
        assertEquals(
            "Kotlin ${KotlinVersion.CURRENT} / JDK ${Runtime.version().feature()}",
            resumenToolchain(),
        )

    @Test
    fun laToolchainEsLaPinada() {
        assertTrue(KotlinVersion.CURRENT.isAtLeast(2, 4, 21), "Kotlin ${KotlinVersion.CURRENT}")
        assertTrue(Runtime.version().feature() >= 21, "JDK ${Runtime.version().feature()}")
    }
}
