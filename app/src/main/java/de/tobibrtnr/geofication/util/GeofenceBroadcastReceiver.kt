package de.tobibrtnr.geofication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread


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

      // Delay of the one geofication that is assigned to the geofence
      var delay = 0

      triggeringGeofences?.forEachIndexed { _, geofence ->

        LogUtil.addLog(
          "Geofence ${geofence.requestId} was triggered.\n" +
              "Entering? $geofenceTransition\n"
        )

        runBlocking {
          // Check if geofence is active
          val geofenceObject = GeofenceUtil.getGeofenceById(geofence.requestId.toInt())
          if (!geofenceObject.active) return@runBlocking

          GeofenceUtil.incrementFenceTriggerCount(geofence.requestId.toInt())

          val geofications = GeofenceUtil.getGeoficationByGeofence(geofence.requestId.toInt())

          var message = ""
          geofications.forEach {
            LogUtil.addLog(
              "Geofence ${it.id}\n" +
                  "Triggered? ${(it.flags == geofenceTransition || it.flags == 3) && it.active}"
            )
            // If the flags equals the triggered one or is both, and the geofication is active:
            if ((it.flags == geofenceTransition || it.flags == 3) && it.active) {
              GeofenceUtil.incrementNotifTriggerCount(it.id)
              message += "${it.message}, "
              delay = it.delay
            }

            when (it.onTrigger) {
              1 -> GeofenceUtil.setNotifActive(it.id, false)
              2 -> GeofenceUtil.deleteGeofence(geofenceObject.id)
            }
          }
          message = message.dropLast(2)

          if (message.isNotEmpty()) {

            LogUtil.addLog("Attempt to send Notification with message \"$message\"")

            thread {
              // x minutes in ms
              Thread.sleep((delay * 60 * 1000).toLong())

              sendNotification(
                context,
                "Geofence ${geofence.requestId} - $geofenceTransition",
                message
              )
            }
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
