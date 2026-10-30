package dev.bitacora.core

import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingTest {
    @Test
    fun saysTheDomainAndTheVersion() = assertEquals("Bitácora agro v1.0.0", greeting("agro", "1.0.0"))
}
