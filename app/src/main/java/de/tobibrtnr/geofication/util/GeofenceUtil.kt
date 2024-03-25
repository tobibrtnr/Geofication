package de.tobibrtnr.geofication.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.GeofencingRequest
import de.tobibrtnr.geofication.ui.MarkerColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeofenceUtil {
  companion object {
    /**
     * Get geofence by id
     */
    suspend fun getGeofenceById(fenceid: Int): Geofence {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.loadById(fenceid)
      }
    }

    /**
     * Get all existing geofences.
     */
    suspend fun getGeofences(): List<Geofence> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.getAll()
      }
    }

    /**
     * Add a new geofence with a given string id, latitude, longitude and radius.
     * TODO if the addGeofence is called after reboot, we do not need to add it again
     * TODO to the database. so maybe alternative without db.
     */
    fun addGeofence(
      context: Context,
      geofenceName: String,
      latitude: Double,
      longitude: Double,
      radius: Float,
      color: MarkerColor,
      active: Boolean,
      triggerCount: Int
    ) {
      val db = ServiceProvider.database()
      val geofencingClient = ServiceProvider.geofence()

      // Coroutine in order to add geofence asynchronously to storage
      CoroutineScope(SupervisorJob()).launch {
        // Add geofence to local database
        val geofenceDao = db.geofenceDao()
        val daoGeofence = Geofence(
          fenceName = geofenceName,
          latitude = latitude,
          longitude = longitude,
          radius = radius,
          color = color,
          active = active,
          triggerCount = triggerCount
        )
        val newId = geofenceDao.insert(daoGeofence)

        val newGeofence = com.google.android.gms.location.Geofence.Builder()
          .setRequestId(newId.toString())
          .setCircularRegion(latitude, longitude, radius)
          .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
          .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
          .build()

        val geofenceRequest = GeofencingRequest.Builder()
          .setInitialTrigger(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
          .addGeofence(newGeofence)
          .build()

        val geofencePendingIntent: PendingIntent by lazy {
          val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
          PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        }

        if (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
          ) == PackageManager.PERMISSION_GRANTED
        ) {
          geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent)
            .addOnSuccessListener {
              // Geofence added successfully
              println("Geofence Added:")
              println(newGeofence)
            }
            .addOnFailureListener { e ->
              // Geofence addition failed
              println("Add Geofence Error:")
              println(e)
              // TODO THIS ERROR IS IMPORTANT TO SHOW AS IT IS THROWN E.G. WHEN
              // LOCATION PRECISION ENHANCEMENT IS DISABLED
            }
        }
      }
    }

    /**
     * Delete the geofence with certain gid from storage and geofencing client.
     */
    suspend fun deleteGeofence(gid: Int) {
      return withContext(Dispatchers.IO) {
        val geofencingClient = ServiceProvider.geofence()
        val db = ServiceProvider.database()
        val geofenceDao = db.geofenceDao()
        geofenceDao.delete(gid)

        geofencingClient.removeGeofences(listOf(gid.toString()))

        println("Removed geofence $gid")
      }
    }


    /**
     * Get all existing geofications.
     */
    suspend fun getGeofications(): List<Geofication> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.getAll()
      }
    }

    /**
     * Add a geofication
     */
    fun addGeofication(
      fenceid: Int,
      message: String,
      flags: Int,
      delay: Int,
      repeat: Boolean,
      active: Boolean,
      onTrigger: Int,
      triggerCount: Int
    ) {
      CoroutineScope(SupervisorJob()).launch {
        val geofication = Geofication(
          fenceid = fenceid,
          message = message,
          flags = flags,
          delay = delay,
          repeat = repeat,
          active = active,
          onTrigger = onTrigger,
          triggerCount = triggerCount
        )

        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.insertAll(geofication)
      }
    }

    /**
     * Delete a geofication
     */
    suspend fun deleteGeofication(gid: Int) {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.delete(gid)
      }
    }

    /**
     * Get all geofications with a certain geofence id
     */
    suspend fun getGeoficationByGeofence(fenceid: Int): List<Geofication> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.getByGeofence(fenceid)
      }
    }

    /**
     * Increment trigger count for geofence
     */
    suspend fun incrementFenceTriggerCount(fenceid: Int) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.incrementTriggerCount(fenceid)
      }
    }

    /**
     * Increment trigger count for geofication
     */
    suspend fun incrementNotifTriggerCount(notifId: Int) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.incrementTriggerCount(notifId)
      }
    }

    /**
     * Set active value for geofence
     */
    suspend fun setFenceActive(fenceId: Int, isActive: Boolean) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.setActive(isActive, fenceId)

        if (!isActive) {
          val notifDao = db.geoficationDao()
          notifDao.deactivateAll(fenceId)
        }
      }
    }

    /**
     * Set active value for Geofication
     */
    suspend fun setNotifActive(fenceid: Int, isActive: Boolean) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.setActive(isActive, fenceid)

        if (isActive) {
          val geofication = geoDao.loadById(fenceid)
          val geofenceDao = db.geofenceDao()
          geofenceDao.setActive(true, geofication.fenceid)
        }
      }
    }
  }
}
