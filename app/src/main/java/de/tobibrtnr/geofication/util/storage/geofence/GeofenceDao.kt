package de.tobibrtnr.geofication.util.storage.geofence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {
  @Query("SELECT * FROM geofence")
  suspend fun getAll(): List<Geofence>

  @Query("SELECT * FROM geofence")
  fun getAllFlow(): Flow<List<Geofence>>

  @Query("SELECT * FROM geofence WHERE id = :geoId")
  suspend fun loadById(geoId: Int): Geofence

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(geofence: Geofence): Long

  @Query("DELETE FROM geofence WHERE id = :gid")
  suspend fun delete(gid: Int)

  @Query("UPDATE geofence SET triggerCount = triggerCount + 1 WHERE id = :gid")
  suspend fun incrementTriggerCount(gid: Int)

  @Query("UPDATE geofence SET active = :isActive WHERE id = :gid")
  suspend fun setActive(isActive: Boolean, gid: Int)

  @Query("DELETE FROM geofence")
  suspend fun deleteAllGeofences()
}
