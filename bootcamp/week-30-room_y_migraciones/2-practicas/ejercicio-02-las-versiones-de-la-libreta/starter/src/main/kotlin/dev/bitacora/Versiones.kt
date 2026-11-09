package dev.bitacora

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection

// La versión 1 de la libreta, la que ya tienen los móviles: su esquema está en schemas/, y no se toca.
// Paso 2: quantity (versión 2). Paso 3: trip se llama tripName (versión 3). Paso 4: packed pasa a status (versión 4).
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trip: String,
    val name: String,
    val packed: Boolean = false,
)

@Dao
interface ItemDao {
    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int
}

// Paso 4: de packed (0 o 1) a status (PENDING o PACKED), con SQL.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
    }
}

@Database(entities = [Item::class], version = 1)
abstract class LibretaDatabase : RoomDatabase() {
    abstract fun items(): ItemDao
}
