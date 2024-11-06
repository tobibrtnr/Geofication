package de.tobibrtnr.geofication.util.storage.geofence

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.receivers.GeofenceBroadcastReceiver
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.geofication.GeoficationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeofenceViewModel(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

  private val repository: GeofenceRepository
  private val geoficationRepository: GeoficationRepository
  private val geofencingClient: GeofencingClient

  val getAllFlow : StateFlow<List<Geofence>>

  init {
    val geofenceDao = ServiceProvider.database().geofenceDao()
    repository = GeofenceRepository(geofenceDao)
    val geoficationDao = ServiceProvider.database().geoficationDao()
    geoficationRepository = GeoficationRepository(geoficationDao)
    getAllFlow = repository.getAllFlow()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    geofencingClient = ServiceProvider.geofence()
  }

  /**
   * Add a new Geofence to database and geofencing client
   */
  fun addGeofence(
    context: Context,
    daoGeofence: Geofence,
    daoGeofication: Geofication? = null,
    forceAddGeofence: Boolean = false
  ) {
    // Coroutine in order to add geofence asynchronously to storage
    CoroutineScope(SupervisorJob()).launch {
      val newId = if (daoGeofence.id <= 0 || forceAddGeofence) {
        // Add geofence to local database
        val idToAdd = repository.insert(daoGeofence).toInt()

        // If 8 Geofications have been created, show a rating popup
        // TODO test internally
        if(idToAdd == 8) {
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
              viewModelScope.launch {
                geoficationRepository.insertAll(daoGeofication)
              }
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
   * Delete a Geofence from database and geofencing client
   */
  fun delete(gid: Int) {
    viewModelScope.launch {
      repository.delete(gid)
      geofencingClient.removeGeofences(listOf(gid.toString()))
    }
  }

  /**
   * Increment trigger count for a Geofence
   */
  fun incrementTriggerCount(gid: Int) {
    viewModelScope.launch {
      repository.incrementTriggerCount(gid)
    }
  }

  /**
   * Set active state for a Geofence
   */
  fun setActive(isActive: Boolean, gid: Int) {
    viewModelScope.launch {
      repository.setActive(isActive, gid)
    }
  }

  /**
   * Delete all Geofences and Geofications
   */
  fun deleteAllGeofences() {
    viewModelScope.launch {
      repository.deleteAllGeofences()
    }
  }

  /**
   * Get current State of view model data
   */
  suspend fun getAll(): List<Geofence> {
    return withContext(dispatcher) {
      repository.getAll()
    }
  }
}