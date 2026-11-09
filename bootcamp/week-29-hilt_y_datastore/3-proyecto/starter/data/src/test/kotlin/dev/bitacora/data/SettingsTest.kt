package dev.bitacora.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SettingsTest {
    @TempDir
    lateinit var dir: File

    private val file get() = File(dir, "settings.preferences_pb")

    @Test
    fun withoutAFileTheDefaults() = runTest {
        assertEquals(ListSettings(), DataStoreSettingsRepository(file, backgroundScope).settings.first())
    }

    @Test
    fun whatIsSavedIsRead() = runTest {
        // Como cerrar y abrir la app: el primero se cierra (su scope termina) antes de crear el segundo.
        val session = CoroutineScope(backgroundScope.coroutineContext + Job())
        DataStoreSettingsRepository(file, session).save(ListSettings(newestFirst = true))
        session.coroutineContext.job.cancelAndJoin()
        assertEquals(ListSettings(newestFirst = true), DataStoreSettingsRepository(file, backgroundScope).settings.first())
    }

    @Test
    fun aBrokenFileGivesTheDefaults() = runTest {
        file.writeText("esto no es un protobuf")
        assertEquals(ListSettings(), DataStoreSettingsRepository(file, backgroundScope).settings.first())
    }

    @Test
    fun twoStoresOnOneFileFail() = runTest {
        val one = DataStoreSettingsRepository(file, backgroundScope)
        val two = DataStoreSettingsRepository(file, backgroundScope)
        one.settings.first()
        assertFailsWith<IllegalStateException> { two.settings.first() }
    }
}
