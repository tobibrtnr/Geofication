package de.tobibrtnr.geofication.util.storage.geofication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.tobibrtnr.geofication.util.storage.GeoficationGeofence
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoficationDao {
  @Query("SELECT * FROM geofication")
  suspend fun getAll(): List<Geofication>

  @Query("SELECT * FROM geofication")
  fun getAllFlow(): Flow<List<Geofication>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(vararg geofications: Geofication)

  @Query("DELETE FROM geofication WHERE id = :gid")
  suspend fun delete(gid: Int)

  @Query("UPDATE geofication SET triggerCount = triggerCount + 1 WHERE id = :gid")
  suspend fun incrementTriggerCount(gid: Int)

  @Query("UPDATE geofication SET active = :isActive WHERE id = :gid")
  suspend fun setActive(isActive: Boolean, gid: Int)

  @Query("UPDATE geofication SET active = 0 WHERE fenceid = :fenceid")
  suspend fun deactivateAll(fenceid: Int)

  @Transaction
  @Query("SELECT geofication.message, geofication.active, geofence.fenceName, geofence.latitude, geofence.longitude, geofence.radius, geofence.color FROM geofication INNER JOIN geofence ON geofication.fenceId = geofence.id WHERE geofication.message LIKE '%' || :query || '%' OR geofence.fenceName LIKE '%' || :query || '%'")
  suspend fun searchGeofications(query: String): List<GeoficationGeofence>
}
