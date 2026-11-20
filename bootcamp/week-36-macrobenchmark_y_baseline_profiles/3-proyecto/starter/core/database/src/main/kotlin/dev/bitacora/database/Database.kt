package dev.bitacora.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnInfo
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.bitacora.model.Level
import kotlinx.coroutines.flow.Flow

// Una fila de la tabla. Aparte de Observation: core no sabe de Room, y la tabla puede cambiar sin tocar el dominio.
@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey val code: String,
    val level: Level,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val photo: String?,
    // Semana 32: cuándo cambió por última vez, aquí o en el servidor (milisegundos desde 1970). Decide quién gana en un
    // conflicto. El valor por defecto es para las filas de la versión 1: sin él, la migración no sabría qué poner.
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0,
)

// Un cambio hecho aquí que el servidor aún no tiene: la cola de la sincronización. Una fila por código, el último
// cambio; un borrado es una fila con deleted = true (la observación ya no está para saber que se borró).
@Entity(tableName = "pending")
data class PendingChange(
    @PrimaryKey val code: String,
    val deleted: Boolean,
    val updatedAt: Long,
)

@Dao
interface ObservationDao {
    // Por el número del código: A-10 después de A-2.
    @Query("SELECT * FROM observations ORDER BY CAST(substr(code, instr(code, '-') + 1) AS INTEGER)")
    fun all(): Flow<List<ObservationEntity>>

    @Upsert
    suspend fun upsert(observations: List<ObservationEntity>)

    @Query("DELETE FROM observations WHERE code NOT IN (:codes)")
    suspend fun deleteOthers(codes: List<String>)

    @Query("SELECT * FROM observations")
    suspend fun snapshot(): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE code = :code")
    suspend fun find(code: String): ObservationEntity?

    @Query("SELECT * FROM pending ORDER BY updatedAt, code")
    suspend fun pending(): List<PendingChange>

    @Upsert
    suspend fun queue(changes: List<PendingChange>)

    @Query("DELETE FROM pending WHERE code IN (:codes)")
    suspend fun unqueue(codes: List<String>)

    // Solo si sigue siendo ese cambio: si se ha vuelto a cambiar mientras se subía, el nuevo se queda en la cola.
    @Query("DELETE FROM pending WHERE code = :code AND updatedAt = :updatedAt")
    suspend fun sent(code: String, updatedAt: Long)

    // La lista nueva entera, y en la cola, lo que ha cambiado: las nuevas, las que no son iguales (sin contar la hora)
    // y las que ya no están. Las que no han cambiado conservan su hora. Todo o nada: quien escucha no ve la lista a
    // medias, y no hay cambio sin su fila en la cola.
    @Transaction
    suspend fun replace(observations: List<ObservationEntity>, now: Long) {
        val before = snapshot().associateBy { it.code }
        val after = observations.map { new ->
            val old = before[new.code]
            if (old != null && old.copy(updatedAt = 0) == new.copy(updatedAt = 0)) old else new.copy(updatedAt = now)
        }
        val gone = before.keys - observations.map { it.code }.toSet()
        deleteOthers(observations.map { it.code })
        upsert(after)
        queue(after.filter { it.updatedAt == now }.map { PendingChange(it.code, false, now) } + gone.map { PendingChange(it, true, now) })
    }

    // Lo que llega del servidor, con la regla «gana el último»: si aquí hay un cambio sin subir más nuevo (o de la misma
    // hora), se queda el de aquí; si el del servidor es más nuevo, entra y el de aquí sale de la cola sin subirse.
    @Transaction
    suspend fun merge(received: List<ObservationEntity>) {
        val waiting = pending().associateBy { it.code }
        val newer = received.filter { remote -> waiting[remote.code].let { it == null || it.updatedAt < remote.updatedAt } }
        upsert(newer)
        unqueue(newer.map { it.code }.filter { it in waiting })
    }
}

// Semana 32: versión 2, con updatedAt y la cola. Room escribe la migración (columna nueva con valor por defecto, tabla
// nueva) comparando schemas/1.json con schemas/2.json.
@Database(
    entities = [ObservationEntity::class, PendingChange::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class BitacoraDatabase : RoomDatabase() {
    abstract fun observations(): ObservationDao
}
