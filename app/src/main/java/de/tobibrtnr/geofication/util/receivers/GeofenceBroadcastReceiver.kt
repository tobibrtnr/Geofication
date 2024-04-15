package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.LogUtil
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.sendNotification
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
          val tFence = GeofenceUtil.getGeofenceById(geofence.requestId.toInt())
          if (!tFence.active) return@runBlocking

          GeofenceUtil.incrementFenceTriggerCount(geofence.requestId.toInt())

          val geofications = GeofenceUtil.getGeoficationByGeofence(geofence.requestId.toInt())

          if (geofications.isEmpty()) {
            return@runBlocking
          }

          val tNotif = geofications[0]

          // If the flags equals the triggered one or is both, and the geofication is active:
          if ((tNotif.flags == geofenceTransition || tNotif.flags == 3) && tNotif.active) {
            GeofenceUtil.incrementNotifTriggerCount(tNotif.id)
            delay = tNotif.delay
          }

          when (tNotif.onTrigger) {
            1 -> GeofenceUtil.setNotifActive(tNotif.id, false)
            2 -> GeofenceUtil.deleteGeofence(tFence.id)
          }

          LogUtil.addLog("Attempt to send Notification \"${tNotif.message}\", \"${tFence.fenceName}\"")

          thread {
            // x minutes in ms
            // TODO does not work on real device
            Thread.sleep((delay * 60 * 1000).toLong())

            sendNotification(
              context,
              tFence,
              tNotif
            )
          }
        }
      }
    } else {
      println("Geofence registered, but not interested")
    }
  }
}
