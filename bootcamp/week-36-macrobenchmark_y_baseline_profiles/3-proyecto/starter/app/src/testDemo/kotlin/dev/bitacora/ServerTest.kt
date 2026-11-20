package dev.bitacora

import org.junit.Test
import kotlin.test.assertEquals

// Semana 31: la demo no toca los datos de verdad. Su servidor es otro.
class ServerTest {
    @Test
    fun theDemoTalksToTheDemoServer() = assertEquals("https://demo.bitacora.example/v1/", BuildConfig.API_URL)
}
