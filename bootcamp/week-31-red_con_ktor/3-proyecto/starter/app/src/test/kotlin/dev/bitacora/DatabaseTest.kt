package dev.bitacora

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test
    fun theSamplesAreInTheDatabase() = runBlocking {
        val database = AppModule.database(ApplicationProvider.getApplicationContext())
        assertEquals(Samples.observations, AppModule.observations(database, AppModule.client()).observations.first())
        database.close()
    }
}
