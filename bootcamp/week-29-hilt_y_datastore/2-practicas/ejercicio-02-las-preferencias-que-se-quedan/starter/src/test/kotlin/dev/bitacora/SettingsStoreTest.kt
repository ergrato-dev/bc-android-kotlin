package dev.bitacora

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsStoreTest {
    @TempDir
    lateinit var dir: File

    private val file get() = File(dir, "trip.preferences_pb")

    // Como cerrar la app: el scope del DataStore termina, y el archivo queda libre para el siguiente.
    private suspend fun TestScope.session(block: suspend (SettingsStore) -> Unit) {
        val scope = CoroutineScope(backgroundScope.coroutineContext + Job())
        block(SettingsStore(file, scope))
        scope.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun withoutAFileTheDefaults() = runTest {
        assertEquals(TripSettings(), SettingsStore(file, backgroundScope).settings.first())
    }

    // Paso 1.
    @Test
    fun whatIsSavedIsInTheFile() = runTest {
        session { it.setReminders(true) }
        assertTrue(file.exists())
        assertEquals(TripSettings(reminders = true), SettingsStore(file, backgroundScope).settings.first())
    }

    // Cada edit cambia una clave y deja las demás.
    @Test
    fun oneChangeKeepsTheOther() = runTest {
        session {
            it.setReminders(true)
            it.setUnits(Units.IMPERIAL)
        }
        assertEquals(TripSettings(Units.IMPERIAL, reminders = true), SettingsStore(file, backgroundScope).settings.first())
    }

    // Uno por archivo: la teoría 04, medida.
    @Test
    fun twoStoresOnOneFileFail() = runTest {
        val one = SettingsStore(file, backgroundScope)
        val two = SettingsStore(file, backgroundScope)
        one.settings.first()
        assertFailsWith<IllegalStateException> { two.settings.first() }
    }

    // Paso 2.
    @Test
    fun aBrokenFileGivesTheDefaults() = runTest {
        file.writeText("esto no es un protobuf")
        assertEquals(TripSettings(), SettingsStore(file, backgroundScope).settings.first())
    }

    // Paso 3: lo de la versión anterior pasa a DataStore, y el archivo viejo se borra.
    @Test
    fun theOldFileIsMigrated() = runTest {
        val legacy = File(dir, "trip.properties").apply { writeText("units=imperial\nreminders=true\n") }
        assertEquals(TripSettings(Units.IMPERIAL, reminders = true), SettingsStore(file, backgroundScope, legacy).settings.first())
        assertFalse(legacy.exists())
    }

    // Quien decide si se migra es shouldMigrate: con el archivo viejo ahí, lo suyo pisa lo que ya hay en DataStore.
    // Por eso cleanUp lo borra.
    @Test
    fun theMigrationOverwritesWhatIsThere() = runTest {
        session { it.setUnits(Units.METRIC) }
        val legacy = File(dir, "trip.properties").apply { writeText("units=imperial\n") }
        assertEquals(Units.IMPERIAL, SettingsStore(file, backgroundScope, legacy).settings.first().units)
    }
}
