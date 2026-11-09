package dev.bitacora

import androidx.room3.Room
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LibretaTest {
    private val database = Room.inMemoryDatabaseBuilder<LibretaDatabase>().setDriver(BundledSQLiteDriver()).build()
    private val libreta = database.libreta()

    @AfterTest
    fun close() = database.close()

    @Test
    fun eachInsertGetsItsId() = runTest {
        assertEquals(1, libreta.insert(Trip(name = "Páramo")))
        assertEquals(2, libreta.insert(Trip(name = "Costa")))
    }

    @Test
    fun pendingOnlyHasWhatIsNotPacked() = runTest {
        val trip = libreta.insert(Trip(name = "Páramo"))
        libreta.insert(Item(tripId = trip, name = "linterna"))
        libreta.insert(Item(tripId = trip, name = "agua", packed = true))
        libreta.insert(Item(tripId = trip, name = "brújula"))
        assertEquals(listOf("brújula", "linterna"), libreta.pending(trip).first().map { it.name })
    }

    // El Flow vuelve a emitir con cada cambio en la tabla. Se recoge en otro hilo, como en la app: Room emite desde el suyo.
    @Test
    fun pendingFollowsTheChanges() = runTest {
        val trip = libreta.insert(Trip(name = "Páramo"))
        val emitted = Channel<List<String>>(Channel.UNLIMITED)
        backgroundScope.launch(Dispatchers.Default) { libreta.pending(trip).collect { list -> emitted.send(list.map { it.name }) } }
        assertEquals(emptyList(), emitted.receive())
        val id = libreta.insert(Item(tripId = trip, name = "linterna"))
        assertEquals(listOf("linterna"), emitted.receive())
        libreta.update(Item(id, trip, "linterna", packed = true))
        assertEquals(emptyList(), emitted.receive())
    }

    @Test
    fun deletingATripDeletesItsItems() = runTest {
        val trip = libreta.insert(Trip(name = "Páramo"))
        libreta.insert(Item(tripId = trip, name = "linterna"))
        libreta.deleteTrip(trip)
        assertEquals(emptyList(), libreta.itemsOf(trip))
    }

    @Test
    fun anItemNeedsItsTrip() = runTest {
        assertFailsWith<SQLiteException> { libreta.insert(Item(tripId = 7, name = "linterna")) }
    }

    @Test
    fun theSameNameTwiceInATripFails() = runTest {
        val trip = libreta.insert(Trip(name = "Páramo"))
        libreta.insert(Item(tripId = trip, name = "linterna"))
        assertFailsWith<SQLiteException> { libreta.insert(Item(tripId = trip, name = "linterna")) }
        libreta.insert(Item(tripId = libreta.insert(Trip(name = "Costa")), name = "linterna"))
    }

    @Test
    fun tripsComeWithTheirItems() = runTest {
        val paramo = libreta.insert(Trip(name = "Páramo"))
        libreta.insert(Item(tripId = paramo, name = "linterna"))
        libreta.insert(Item(tripId = paramo, name = "agua"))
        libreta.insert(Trip(name = "Costa"))
        val trips = libreta.trips().first()
        assertEquals(listOf("Costa" to 0, "Páramo" to 2), trips.map { it.trip.name to it.items.size })
    }

    @Test
    fun copyingATripCopiesItsItemsUnpacked() = runTest {
        val paramo = libreta.insert(Trip(name = "Páramo"))
        libreta.insert(Item(tripId = paramo, name = "linterna", packed = true))
        libreta.insert(Item(tripId = paramo, name = "agua"))
        val copy = libreta.copy(paramo, "Páramo otra vez")
        assertEquals(listOf("agua", "linterna"), libreta.pending(copy).first().map { it.name })
    }
}
