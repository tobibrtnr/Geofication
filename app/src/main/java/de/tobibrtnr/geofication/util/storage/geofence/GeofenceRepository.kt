package de.tobibrtnr.geofication.util.storage.geofence

import kotlinx.coroutines.flow.Flow

class GeofenceRepository(private val geofenceDao: GeofenceDao) {

  suspend fun getAll(): List<Geofence> = geofenceDao.getAll()

  fun getAllFlow(): Flow<List<Geofence>> = geofenceDao.getAllFlow()

  suspend fun loadById(geoId: Int): Geofence = geofenceDao.loadById(geoId)

  suspend fun insert(geofence: Geofence): Long = geofenceDao.insert(geofence)

  suspend fun delete(gid: Int) = geofenceDao.delete(gid)

  suspend fun incrementTriggerCount(gid: Int) = geofenceDao.incrementTriggerCount(gid)

  suspend fun setActive(isActive: Boolean, gid: Int) = geofenceDao.setActive(isActive, gid)

  suspend fun deleteAllGeofences() = geofenceDao.deleteAllGeofences()
}
