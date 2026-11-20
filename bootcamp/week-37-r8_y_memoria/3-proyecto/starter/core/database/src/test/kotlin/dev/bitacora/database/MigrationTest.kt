package dev.bitacora.database

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

// Semana 32: de la versión 1 (semana 30) a la 2, con updatedAt y la cola. Los esquemas, de schemas/.
class MigrationTest {
    @TempDir
    lateinit var dir: File

    private val file get() = File(dir, "bitacora.db")

    private val helper by lazy {
        MigrationTestHelper(Path.of("schemas"), file.toPath(), BundledSQLiteDriver(), BitacoraDatabase::class)
    }

    // Una base de datos de la versión 1, con el SQL de entonces.
    private suspend fun version1() = helper.createDatabase(1).use {
        it.execSQL("INSERT INTO observations VALUES ('A-1', 'HIGH', 'roya en el lote 2', 4.6, -74.08, NULL)")
    }

    private fun SQLiteConnection.rows(sql: String): List<String> = prepare(sql).use { statement ->
        buildList { while (statement.step()) add((0 until statement.getColumnCount()).joinToString("|") { statement.getText(it) }) }
    }

    // Lo que había sigue, con la hora 0 (más vieja que cualquier cambio), y la cola empieza vacía.
    @Test
    fun fromOneToTwoKeepsTheRowsAtTimeZero() = runTest {
        version1()
        helper.runMigrationsAndValidate(2).use {
            assertEquals(listOf("A-1|roya en el lote 2|0"), it.rows("SELECT code, description, updatedAt FROM observations"))
            assertEquals(emptyList(), it.rows("SELECT * FROM pending"))
        }
    }

    // Lo que hace la app con el archivo de la versión 1: abrirlo, migrarlo y leerlo. Con el DAO: el repositorio es de
    // :core:data, que este módulo no ve.
    @Test
    fun theAppOpensAVersionOneFile() = runTest {
        version1()
        val database = Room.databaseBuilder<BitacoraDatabase>(file.path).setDriver(BundledSQLiteDriver()).build()
        assertEquals(listOf("A-1"), database.observations().all().first().map { it.code })
        database.close()
    }
}
