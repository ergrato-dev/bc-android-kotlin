import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoteTest {
    private val count = ":countObservations"
    private val credentials = mapOf("CACHE_USER" to "bitacora", "CACHE_PASSWORD" to "s3cret")
    private val ci = credentials + ("CI" to "true")

    private fun server() = CacheServer("bitacora", "s3cret")

    @Test
    fun withoutAUrlTheServerIsNotUsed() {
        server().use { server ->
            val sb = Sandbox()
            sb.env = ci
            assertEquals(TaskOutcome.SUCCESS, sb.run(count).outcome(count))
            assertEquals(0, server.stores.get() + server.hits.get() + server.misses.get() + server.rejected.get())
        }
    }

    @Test
    fun theCiPushesItsResults() {
        server().use { server ->
            val sb = Sandbox()
            sb.env = ci
            sb.run(count, "compileKotlin", "-Pbitacora.cacheUrl=${server.url}")
            assertEquals(2, server.stores.get())
        }
    }

    @Test
    fun aDeveloperOnlyPulls() {
        server().use { server ->
            val sb = Sandbox()
            sb.env = credentials
            assertEquals(TaskOutcome.SUCCESS, sb.run(count, "-Pbitacora.cacheUrl=${server.url}").outcome(count))
            assertEquals(1, server.misses.get())
            assertEquals(0, server.stores.get())
        }
    }

    @Test
    fun anotherMachineReusesWhatTheCiBuilt() {
        server().use { server ->
            val machine = Sandbox()
            machine.env = ci
            machine.run(count, "compileKotlin", "-Pbitacora.cacheUrl=${server.url}")
            val dev = machine.otherMachine()
            dev.env = credentials
            val result = dev.run(count, "compileKotlin", "-Pbitacora.cacheUrl=${server.url}")
            assertEquals(TaskOutcome.FROM_CACHE, result.outcome(count), result.output)
            assertEquals(TaskOutcome.FROM_CACHE, result.outcome(":compileKotlin"), result.output)
            assertEquals("observations=3\n", dev.file("build/reports/observations.txt").readText())
        }
    }

    @Test
    fun theLocalCacheIsAskedFirst() {
        server().use { server ->
            val machine = Sandbox()
            machine.env = ci
            machine.run(count, "-Pbitacora.cacheUrl=${server.url}")
            val dev = machine.otherMachine()
            dev.env = credentials
            dev.run(count, "-Pbitacora.cacheUrl=${server.url}")
            dev.run("clean")
            assertEquals(TaskOutcome.FROM_CACHE, dev.run(count, "-Pbitacora.cacheUrl=${server.url}").outcome(count))
            assertEquals(1, server.hits.get())
        }
    }

    @Test
    fun aWrongPasswordDisablesTheRemoteButNotTheBuild() {
        server().use { server ->
            val sb = Sandbox()
            sb.env = ci + ("CACHE_PASSWORD" to "wrong")
            val result = sb.run(count, "-Pbitacora.cacheUrl=${server.url}")
            assertEquals(TaskOutcome.SUCCESS, result.outcome(count))
            assertTrue(result.output.contains("The remote build cache was disabled during the build due to errors."), result.output)
            assertEquals(1, server.rejected.get())
            assertEquals(0, server.stores.get())
        }
    }

    @Test
    fun thePasswordIsNotInTheRepository() {
        val settings = Sandbox().file("settings.gradle.kts").readText()
        assertTrue(settings.contains("environmentVariable(\"CACHE_PASSWORD\")"), "La contraseña sale de CACHE_PASSWORD")
        assertFalse(settings.contains("s3cret"), "La contraseña está escrita en settings.gradle.kts")
    }
}
