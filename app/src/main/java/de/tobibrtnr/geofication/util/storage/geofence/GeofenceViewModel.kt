package de.tobibrtnr.geofication.util.storage.geofence

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
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
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeofenceViewModel(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

  // Repositories and geofencing client that will be used
  private val repository: GeofenceRepository
  private val geoficationRepository: GeoficationRepository
  private val geofencingClient: GeofencingClient

  // State flow to get live view of the data
  val getAllFlow : StateFlow<List<Geofence>>

  // Initialize both repositories, the GeofencingClient and state flow.
  init {
    val geofenceDao = ServiceProvider.database().geofenceDao()
    repository = GeofenceRepository(geofenceDao)

    val geoficationDao = ServiceProvider.database().geoficationDao()
    geoficationRepository = GeoficationRepository(geoficationDao)

    getAllFlow = repository.getAllFlow()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    geofencingClient = ServiceProvider.geofence()
  }

  // Add a new Geofence to the database and geofencing client
  fun addGeofence(
    context: Context,
    newGeofence: Geofence,
    newGeofication: Geofication? = null,
    forceAddGeofence: Boolean = false
  ) {
    // Coroutine in order to add geofence asynchronously to storage
    CoroutineScope(SupervisorJob()).launch {
      val newId = if (newGeofence.id <= 0 || forceAddGeofence) {
        // Add geofence to local database
        val idToAdd = repository.insert(newGeofence).toInt()

        // If 8 Geofications have been created, show a rating popup
        if(idToAdd == 8) {
          showReviewPopup(context)
        }

        idToAdd
      } else {
        newGeofence.id
      }

      // Create Geofencing API object
      val newGoogleGeofence = com.google.android.gms.location.Geofence.Builder()
        .setRequestId(newId.toString())
        .setCircularRegion(newGeofence.latitude, newGeofence.longitude, newGeofence.radius)
        .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
        .setTransitionTypes(
          com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or
              com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
        .build()

      // Request to add new geofence
      val geofenceRequest = GeofencingRequest.Builder()
        .setInitialTrigger(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
        .addGeofence(newGoogleGeofence)
        .build()

      // Intent that will be called when the geofence is triggered
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
            // Geofence added successfully. If a Geofication is given, too,
            // we also insert it and set the fence id to the new one.
            if(newGeofication != null) {
              newGeofication.fenceid = newId
              viewModelScope.launch {
                geoficationRepository.insertAll(newGeofication)
              }
            }
          }
          .addOnFailureListener { e ->
            // Geofence creation failed
            LogUtil.addLog("Error while adding Geofence: $e", severity = 5)
            Toast.makeText(context, "Error while adding Geofence.", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  // Opens a popup that asks the user to
  // leave a review for the app on Google Play Store.
  private fun showReviewPopup(context: Context) {
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

  // Delete a Geofence from database and geofencing client
  // This also deletes all Geofications that use this Geofence with cascade.
  fun delete(gid: Int) {
    viewModelScope.launch {
      repository.delete(gid)
      geofencingClient.removeGeofences(listOf(gid.toString()))
    }
  }

  // Increment trigger count for a Geofence
  fun incrementTriggerCount(gid: Int) {
    viewModelScope.launch {
      repository.incrementTriggerCount(gid)
    }
  }

  // Set the active property for a Geofence
  fun setActive(isActive: Boolean, gid: Int) {
    viewModelScope.launch {
      repository.setActive(isActive, gid)
    }
  }

  // Delete all Geofences (and Geofications with cascade)
  fun deleteAllGeofences() {
    viewModelScope.launch {
      repository.deleteAllGeofences()
    }
  }

  // Get current State of view model data
  @SuppressWarnings("kotlin:S6313")
  suspend fun getAll(): List<Geofence> {
    return withContext(dispatcher) {
      repository.getAll()
    }
  }
}
