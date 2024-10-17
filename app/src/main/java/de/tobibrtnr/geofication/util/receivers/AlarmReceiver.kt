package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.getByteInput
import de.tobibrtnr.geofication.util.misc.sendNotification
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import de.tobibrtnr.geofication.util.storage.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == "de.tobibrtnr.geofication.GEOFICATION_ALARM") {
      ServiceProvider.setInstance(context)

      val tFence = getByteInput(intent.getByteArrayExtra("tFence")) as Geofence
      val tNotif = getByteInput(intent.getByteArrayExtra("tNotif")) as Geofication

      val geofenceTransition = intent.getIntExtra("geofenceTransition", 0)

      handleGeofication(context, tFence, tNotif, geofenceTransition)
    }
  }
}

fun handleGeofication(
  context: Context,
  tFence: Geofence,
  tNotif: Geofication,
  geofenceTransition: Int
) {
  CoroutineScope(Dispatchers.IO).launch {

    // If the flags equals the triggered one or is both, and the geofication is active:
    if ((tNotif.flags == geofenceTransition || tNotif.flags == 3) && tNotif.active) {
      GeofenceUtil.incrementNotifTriggerCount(tNotif.id)
    }

    when (tNotif.onTrigger) {
      1 -> GeofenceUtil.setNotifActive(tNotif.id, false)
      2 -> GeofenceUtil.deleteGeofence(tFence.id)
    }

    LogUtil.addLog("Attempt to send Notification \"${tNotif.message}\", \"${tFence.fenceName}\"")

    sendNotification(
      context,
      tFence,
      tNotif
    )
  }
}
