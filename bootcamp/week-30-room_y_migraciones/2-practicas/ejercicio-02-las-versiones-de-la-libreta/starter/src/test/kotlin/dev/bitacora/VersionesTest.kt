package dev.bitacora

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersionesTest {
    @TempDir
    lateinit var dir: File

    private val file get() = File(dir, "libreta.db")

    private val helper by lazy {
        MigrationTestHelper(Path.of("schemas"), file.toPath(), BundledSQLiteDriver(), LibretaDatabase::class)
    }

    // Una libreta de la versión 1, escrita con el SQL de entonces: el código de entonces ya no existe.
    private suspend fun version1() = helper.createDatabase(1).use {
        it.execSQL("INSERT INTO items (trip, name, packed) VALUES ('Páramo', 'linterna', 1), ('Páramo', 'agua', 0)")
    }

    private fun SQLiteConnection.rows(sql: String): List<String> = prepare(sql).use { statement ->
        buildList { while (statement.step()) add((0 until statement.getColumnCount()).joinToString("|") { statement.getText(it) }) }
    }

    @Test
    fun fromOneToTwoEveryItemHasQuantityOne() = runTest {
        version1()
        helper.runMigrationsAndValidate(2).use {
            assertEquals(listOf("linterna|1", "agua|1"), it.rows("SELECT name, quantity FROM items ORDER BY id"))
        }
    }

    @Test
    fun fromTwoToThreeTheTripKeepsItsValue() = runTest {
        version1()
        helper.runMigrationsAndValidate(3).use {
            assertEquals(listOf("Páramo|linterna", "Páramo|agua"), it.rows("SELECT tripName, name FROM items ORDER BY id"))
        }
    }

    @Test
    fun fromThreeToFourPackedBecomesStatus() = runTest {
        version1()
        helper.runMigrationsAndValidate(3).close()
        helper.runMigrationsAndValidate(4, listOf(MIGRATION_3_4)).use {
            assertEquals(listOf("linterna|PACKED", "agua|PENDING"), it.rows("SELECT name, status FROM items ORDER BY id"))
        }
    }

    @Test
    fun fromOneToFourInOneGo() = runTest {
        version1()
        helper.runMigrationsAndValidate(4, listOf(MIGRATION_3_4)).use {
            assertEquals(
                listOf("Páramo|linterna|1|PACKED", "Páramo|agua|1|PENDING"),
                it.rows("SELECT tripName, name, quantity, status FROM items ORDER BY id"),
            )
        }
    }

    // Lo que hace la app al abrir un archivo viejo: el builder encuentra las migraciones y llega a la última.
    @Test
    fun theAppOpensAVersionOneFile() = runTest {
        version1()
        val database = Room.databaseBuilder<LibretaDatabase>(file.path).setDriver(BundledSQLiteDriver())
            .addMigrations(MIGRATION_3_4).build()
        assertEquals(2, database.items().count())
        database.close()
    }

    // Sin la migración a mano, Room no adivina: se niega a abrir, y no borra nada.
    @Test
    fun withoutTheManualMigrationItRefuses() = runTest {
        helper.createDatabase(3).close()
        val database = Room.databaseBuilder<LibretaDatabase>(file.path).setDriver(BundledSQLiteDriver()).build()
        val error = assertFailsWith<IllegalStateException> { database.items().count() }
        assertEquals(true, error.message?.contains("from 3 to 4"), error.message)
        database.close()
    }
}
