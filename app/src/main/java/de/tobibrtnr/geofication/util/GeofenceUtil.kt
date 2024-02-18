package de.tobibrtnr.geofication.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
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
    suspend fun getGeofences(geofencingClient: GeofencingClient, db: AppDatabase): List<Geofence> {
      return withContext(Dispatchers.IO) {
        val geoDao = db.geofenceDao()
        geoDao.getAll()
      }
    }

    /**
     * Add a new geofence with a given string id, latitude, longitude and radius.
     */
    fun addGeofence(
      context: Context,
      geofencingClient: GeofencingClient,
      db: AppDatabase,
      gid: String,
      latitude: Double,
      longitude: Double,
      radius: Float
    ) {
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
          radius
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
  }
}
