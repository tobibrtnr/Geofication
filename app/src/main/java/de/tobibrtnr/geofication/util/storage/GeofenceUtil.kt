package de.tobibrtnr.geofication.util.storage

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.GeofencingRequest
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.receivers.GeofenceBroadcastReceiver
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
     */
    fun addGeofence(
      context: Context,
      daoGeofence: Geofence,
      daoGeofication: Geofication? = null,
      forceAddGeofence: Boolean = false
    ) {
      val db = ServiceProvider.database()
      val geofencingClient = ServiceProvider.geofence()

      // Coroutine in order to add geofence asynchronously to storage
      CoroutineScope(SupervisorJob()).launch {
        val newId = if (daoGeofence.id <= 0 || forceAddGeofence) {
          // Add geofence to local database
          val geofenceDao = db.geofenceDao()
          val idToAdd = geofenceDao.insert(daoGeofence).toInt()

          // If 8 Geofications have been created, show a rating popup
          // TODO test internally
          if(idToAdd == 8) {
            //val manager = FakeReviewManager(context)
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
              if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                  // The flow has finished. The API does not indicate whether the user
                  // reviewed or not, or even whether the review dialog was shown. Thus, no
                  // matter the result, we continue our app flow.
                }
              } else {
                // There was some problem, log or handle the error code.
                @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
                println(reviewErrorCode)
              }
            }
          }

          idToAdd
        } else {
          daoGeofence.id
        }

        val newGeofence = com.google.android.gms.location.Geofence.Builder()
          .setRequestId(newId.toString())
          .setCircularRegion(daoGeofence.latitude, daoGeofence.longitude, daoGeofence.radius)
          .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
          .setTransitionTypes(
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
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

              if(daoGeofication != null) {
                daoGeofication.fenceid = newId
                addGeofication(daoGeofication)
              }
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
      geofication: Geofication
    ) {
      CoroutineScope(SupervisorJob()).launch {
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
    /*suspend fun setFenceActive(fenceId: Int, isActive: Boolean) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.setActive(isActive, fenceId)

        if (!isActive) {
          val notifDao = db.geoficationDao()
          notifDao.deactivateAll(fenceId)
        }
      }
    }*/

    /**
     * Set active value for Geofication, also set Geofence active state
     */
    suspend fun setNotifActive(notifId: Int, isActive: Boolean) {
      withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.setActive(isActive, notifId)

        val geofication = geoDao.loadById(notifId)
        val geofenceDao = db.geofenceDao()
        geofenceDao.setActive(isActive, geofication.fenceid)
      }
    }

    /**
     * Search for a query that matches the Geofication message or the geofence name.
     */
    suspend fun searchGeofications(query: String): List<GeoficationGeofence> {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geoficationDao()
        geoDao.searchGeofications(query)
      }
    }

    /**
     * Delete all Geofications and Geofences
     */
    suspend fun deleteAllGeofications() {
      return withContext(Dispatchers.IO) {
        val db = ServiceProvider.database()
        val geoDao = db.geofenceDao()
        geoDao.deleteAllGeofences()
      }
    }
  }
}
