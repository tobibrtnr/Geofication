package de.tobibrtnr.geofication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.runBlocking


class GeofenceBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent) {
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

      triggeringGeofences?.forEachIndexed { i, geofence ->
        println("Triggered Geofence $i")
        println("Geofence ${geofence.requestId}")
        println("Entering? $geofenceTransition")

        runBlocking {
          // Check if geofence is active
          val geofenceObject = GeofenceUtil.getGeofenceById(geofence.requestId)
          if(!geofenceObject.active) return@runBlocking

          GeofenceUtil.incrementFenceTriggerCount(geofence.requestId)

          val geofications = GeofenceUtil.getGeoficationByGeofence(geofence.requestId)
          println("THESE GEOFICATIONS ARE TRIGGERED")
          println(geofications)

          var message = ""
          geofications.forEach {
            // If the flags equals the triggered one or is both, and the geofication is active:
            if((it.flags == geofenceTransition || it.flags == 3) && it.active) {
              GeofenceUtil.incrementNotifTriggerCount(it.gid)
              message += "${it.gid}, "
            }
          }
          message = message.dropLast(2)

          if (context != null && message.isNotEmpty()) {
            sendNotification(
              context,
              "Geofence ${geofence.requestId} - $geofenceTransition",
              message
            )
          } else {
            println("No context for notification given.")
          }
        }
      }
    } else {
      println("Geofence registered, but not interested")
    }
  }
}
