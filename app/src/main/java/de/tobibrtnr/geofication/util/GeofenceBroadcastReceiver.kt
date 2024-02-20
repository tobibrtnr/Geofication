package de.tobibrtnr.geofication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


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

        if (context != null) {
          sendNotification(
            context,
            "Geofication",
            "Geofence ${geofence.requestId} - $geofenceTransition"
          )
        } else {
          println("No context for notification given.")
        }
      }
    } else {
      println("Geofence registered, but not interested")
    }
  }
}
