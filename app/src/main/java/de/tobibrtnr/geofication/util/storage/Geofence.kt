package de.tobibrtnr.geofication.util.storage

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import de.tobibrtnr.geofication.ui.common.MarkerColor
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

data class GeoficationGeofence(
  val message: String,
  val active: Boolean,

  val fenceName: String,
  val latitude: Double,
  val longitude: Double,
  var radius: Float,
  var color: MarkerColor,
)

@Entity
data class Geofence(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,

  val fenceName: String,
  val latitude: Double,
  val longitude: Double,
  var radius: Float,

  var color: MarkerColor,
  var active: Boolean,
  val triggerCount: Int,

  val created: Long,
  var lastEdit: Long
) : Serializable

@Entity(
  foreignKeys = [ForeignKey(
    entity = Geofence::class,
    parentColumns = ["id"],
    childColumns = ["fenceid"],
    onDelete = ForeignKey.CASCADE
  )]
)
data class Geofication(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @ColumnInfo(index = true) var fenceid: Int,

  var message: String,
  var flags: Int,
  var delay: Int,
  val repeat: Boolean,
  val active: Boolean,
  var onTrigger: Int,
  var link: String,

  val triggerCount: Int,
  val created: Long,
  var lastEdit: Long
) : Serializable

@Entity
data class LogEntry(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val timestamp: Long,
  val message: String,
  val severity: Int
)

@Entity
data class Setting(
  @PrimaryKey
  val key: String,
  val value: ByteArray
) {

  // Generated
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Setting

    if (key != other.key) return false

    return true
  }

  override fun hashCode(): Int {
    return key.hashCode()
  }
}

@Dao
interface GeofenceDao {
  @Query("SELECT * FROM geofence")
  fun getAll(): List<Geofence>

  @Query("SELECT * FROM geofence")
  fun getAllFlow(): Flow<List<Geofence>>

  @Query("SELECT * FROM geofence WHERE id = :geoId")
  fun loadById(geoId: Int): Geofence

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(geofence: Geofence): Long

  @Query("DELETE FROM geofence WHERE id = :gid")
  fun delete(gid: Int)

  @Query("UPDATE geofence SET triggerCount = triggerCount + 1 WHERE id = :gid")
  fun incrementTriggerCount(gid: Int)

  @Query("UPDATE geofence SET active = :isActive WHERE id = :gid")
  fun setActive(isActive: Boolean, gid: Int)

  @Query("DELETE FROM geofence")
  fun deleteAllGeofences()
}

@Dao
interface GeoficationDao {
  @Query("SELECT * FROM geofication")
  fun getAll(): List<Geofication>

  @Query("SELECT * FROM geofication")
  fun getAllFlow(): Flow<List<Geofication>>

  @Query("SELECT * FROM geofication WHERE id = :geoId")
  fun loadById(geoId: Int): Geofication

  @Query("SELECT * FROM geofication WHERE fenceid = :fenceId")
  fun getByGeofence(fenceId: Int): List<Geofication>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg geofications: Geofication)

  @Query("DELETE FROM geofication WHERE id = :gid")
  fun delete(gid: Int)

  @Query("UPDATE geofication SET triggerCount = triggerCount + 1 WHERE id = :gid")
  fun incrementTriggerCount(gid: Int)

  @Query("UPDATE geofication SET active = :isActive WHERE id = :gid")
  fun setActive(isActive: Boolean, gid: Int)

  @Query("UPDATE geofication SET active = 0 WHERE fenceid = :fenceid")
  fun deactivateAll(fenceid: Int)

  @Transaction
  @Query("SELECT geofication.message, geofication.active, geofence.fenceName, geofence.latitude, geofence.longitude, geofence.radius, geofence.color FROM geofication INNER JOIN geofence ON geofication.fenceId = geofence.id WHERE geofication.message LIKE '%' || :query || '%' OR geofence.fenceName LIKE '%' || :query || '%'")
  fun searchGeofications(query: String): List<GeoficationGeofence>
}

@Dao
interface LogDao {
  @Query("SELECT * FROM logEntry ORDER BY id DESC")
  fun getAll(): List<LogEntry>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg logEntries: LogEntry)

  @Query("DELETE FROM logEntry")
  fun deleteAll()
}

@Dao
interface SettingsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun setSetting(setting: Setting)

  @Query("SELECT value FROM setting WHERE `key` = :name")
  fun getSetting(name: String): ByteArray

  @Query("DELETE from setting")
  fun resetSettings()
}

@Database(
  entities = [Geofence::class, Geofication::class, LogEntry::class, Setting::class],
  version = 1
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun geofenceDao(): GeofenceDao
  abstract fun geoficationDao(): GeoficationDao
  abstract fun logDao(): LogDao
  abstract fun settingsDao(): SettingsDao
}
