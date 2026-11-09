package dev.bitacora

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Relation
import androidx.room3.RoomDatabase
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

// Una salida al campo. El id lo pone SQLite: 0 quiere decir "todavía no tiene".
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

// Lo que hay que llevar a una salida.
// Paso 2: que no pueda existir sin su salida (ForeignKey, y que se borre con ella) y que no se repita el nombre dentro
// de la misma salida (un Index único de dos columnas).
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val name: String,
    val packed: Boolean = false,
)

// Ya está hecho: una salida con sus cosas. Room hace dos consultas, una por tabla, y las junta.
data class TripWithItems(
    @Embedded val trip: Trip,
    @Relation(parentColumns = ["id"], entityColumns = ["tripId"])
    val items: List<Item>,
)

@Dao
interface LibretaDao {
    @Insert
    suspend fun insert(trip: Trip): Long

    @Insert
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: Long)

    @Query("SELECT * FROM items WHERE tripId = :tripId")
    suspend fun itemsOf(tripId: Long): List<Item>

    // Paso 3: solo lo que falta por guardar en la mochila, por nombre.
    @Query("SELECT * FROM items WHERE tripId = :tripId")
    fun pending(tripId: Long): Flow<List<Item>>

    // Paso 4: por nombre, y las dos consultas de la relación en la misma transacción.
    @Query("SELECT * FROM trips")
    fun trips(): Flow<List<TripWithItems>>

    // Paso 5: la salida nueva con las mismas cosas, sin guardar, y todo o nada.
    suspend fun copy(from: Long, name: String): Long = insert(Trip(name = name))
}

@Database(entities = [Trip::class, Item::class], version = 1)
abstract class LibretaDatabase : RoomDatabase() {
    abstract fun libreta(): LibretaDao
}
