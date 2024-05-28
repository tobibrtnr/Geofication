package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.serializeObject
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.LogUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    ServiceProvider.setInstance(context)

    LogUtil.addLog("GeofenceBroadcastReceiver started.")

    val geofencingEvent = GeofencingEvent.fromIntent(intent)

    // Check if the Event has errors
    if (geofencingEvent?.hasError() == true) {
      val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
      println("Geofence contains error:")
      println(errorMessage)
      return
    }

    // Get the transition type.
    val geofenceTransition = geofencingEvent?.geofenceTransition

    // Test that the reported transition was of interest.
    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
      geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
    ) {

      // Get the geofences that were triggered. A single event can trigger multiple geofences.
      val triggeringGeofences = geofencingEvent.triggeringGeofences

      triggeringGeofences?.forEachIndexed { _, geofence ->

        LogUtil.addLog(
          "Geofence ${geofence.requestId} was triggered.\n" +
              "Entering? $geofenceTransition"
        )

        runBlocking {
          // Check if geofence is active
          val tFence = GeofenceUtil.getGeofenceById(geofence.requestId.toInt())
          if (!tFence.active) return@runBlocking

          GeofenceUtil.incrementFenceTriggerCount(geofence.requestId.toInt())

          val geofications = GeofenceUtil.getGeoficationByGeofence(geofence.requestId.toInt())

          if (geofications.isEmpty()) {
            return@runBlocking
          }

          // Get current triggered geofication
          val tNotif = geofications[0]

          val fenceBytes = serializeObject(tFence)
          val notifBytes = serializeObject(tNotif)

          val input = Data.Builder()
            .putByteArray("tFence", fenceBytes)
            .putByteArray("tNotif", notifBytes)
            .putInt("geofenceTransition", geofenceTransition)
            .build()

          val notifWorkerRequest = OneTimeWorkRequestBuilder<GeoficationWorker>()
            .setInputData(input)
            .setInitialDelay(tNotif.delay.toLong(), TimeUnit.MINUTES)
            .build()

          WorkManager.getInstance(context).enqueue(notifWorkerRequest)

          LogUtil.addLog("Worker Request started. Notification in ${tNotif.delay} minutes.")
        }
      }
    } else {
      println("Geofence registered, but not interested")
    }
  }
}
