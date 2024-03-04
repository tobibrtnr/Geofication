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
      gid: String,
      latitude: Double,
      longitude: Double,
      radius: Float,
      color: MarkerColor
    ) {
      val db = ServiceProvider.database()
      val geofencingClient = ServiceProvider.geofence()
      val geofence = com.google.android.gms.location.Geofence.Builder()
        .setRequestId(gid)
        .setCircularRegion(latitude, longitude, radius)
        .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
        .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
        .build()

      // Coroutine in order to add geofence asynchronously to storage
      CoroutineScope(SupervisorJob()).launch {
        // Add geofence to local database
        val geofenceDao = db.geofenceDao()
        val daoGeofence = Geofence(
          gid,
          latitude,
          longitude,
          radius,
          color
        )
        geofenceDao.insertAll(daoGeofence)
      }

      val geofenceRequest = GeofencingRequest.Builder()
        .setInitialTrigger(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
        .addGeofence(geofence)
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
            println(geofence)
          }
          .addOnFailureListener { e ->
            // Geofence addition failed
            println("Add Geofence Error:")
            println(e)
          }
      }
    }

    /**
     * Delete the geofence with certain gid from storage and geofencing client.
     */
    suspend fun deleteGeofence(gid: String) {
      return withContext(Dispatchers.IO) {
        val geofencingClient = ServiceProvider.geofence()
        val db = ServiceProvider.database()
        val geofenceDao = db.geofenceDao()
        geofenceDao.delete(gid)

        geofencingClient.removeGeofences(listOf(gid))

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
      gid: String,
      fenceid: String,
      message: String,
      flags: Int,
      delay: Int,
      repeat: Boolean,
      color: MarkerColor
    ) {
      CoroutineScope(SupervisorJob()).launch {
        val geofication = Geofication(gid, fenceid, message, flags, delay, repeat, color)

        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.insertAll(geofication)
      }
    }

    /**
     * Delete a geofication
     */
    suspend fun deleteGeofication(gid: String) {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.delete(gid)
      }
    }

    /**
     * Get all geofications with a certain geofence id
     */
    suspend fun getGeoficationByGeofence(fenceid: String): List<Geofication> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.getByGeofence(fenceid)
      }
    }
  }
}
