package de.tobibrtnr.geofication.util.storage.geofication

import de.tobibrtnr.geofication.util.storage.GeoficationGeofence
import kotlinx.coroutines.flow.Flow

class GeoficationRepository(private val geoficationDao: GeoficationDao) {
  suspend fun getAll(): List<Geofication> = geoficationDao.getAll()

  fun getAllFlow(): Flow<List<Geofication>> = geoficationDao.getAllFlow()

  suspend fun loadById(geoId: Int): Geofication = geoficationDao.loadById(geoId)

  suspend fun getByGeofence(fenceId: Int): List<Geofication> = geoficationDao.getByGeofence(fenceId)

  suspend fun insertAll(vararg geofications: Geofication) = geoficationDao.insertAll(*geofications)

  suspend fun delete(gid: Int) = geoficationDao.delete(gid)

  suspend fun incrementTriggerCount(gid: Int) = geoficationDao.incrementTriggerCount(gid)

  suspend fun setActive(isActive: Boolean, gid: Int) = geoficationDao.setActive(isActive, gid)

  suspend fun deactivateAll(fenceid: Int) = geoficationDao.deactivateAll(fenceid)

  suspend fun searchGeofications(query: String): List<GeoficationGeofence>
    = geoficationDao.searchGeofications(query)
}