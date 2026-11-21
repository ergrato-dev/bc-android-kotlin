package dev.bitacora

import org.junit.Test
import kotlin.test.assertEquals

// Semana 31: cada flavor habla con su servidor. La URL sale de BuildConfig, que AGP genera por variante.
class ServerTest {
    @Test
    fun theFieldAppTalksToTheRealServer() = assertEquals("https://api.bitacora.example/v1/", BuildConfig.API_URL)
}
