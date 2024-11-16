package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.getByteInput
import de.tobibrtnr.geofication.util.misc.sendNotification
import de.tobibrtnr.geofication.util.misc.serializeObject
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == "de.tobibrtnr.geofication.GEOFICATION_ALARM") {
      ServiceProvider.setInstance(context)

      val tFence = getByteInput(intent.getByteArrayExtra("tFence")) as Geofence
      val tNotif = getByteInput(intent.getByteArrayExtra("tNotif")) as Geofication

      handleGeofication(context, tFence, tNotif)
    }
  }
}

fun handleGeofication(
  context: Context,
  tFence: Geofence,
  tNotif: Geofication
) {
  LogUtil.addLog("AlarmReceiver handleGeofence started.")

  CoroutineScope(Dispatchers.IO).launch {

    val geofenceViewModel = ServiceProvider.geofenceViewModel()
    val geoficationViewModel = ServiceProvider.geoficationViewModel()

    geofenceViewModel.incrementTriggerCount(tNotif.id)

    when (tNotif.onTrigger) {
      1 -> {
        geofenceViewModel.setActive(false, tFence.id)
        geoficationViewModel.setActive(false, tNotif.id)
      }
      2 -> {
        geofenceViewModel.delete(tFence.id)
      }
    }

    if (tNotif.isAlarm) {
      LogUtil.addLog("Attempt to send Alarm \"${tNotif.message}\", \"${tFence.fenceName}\"")
      // Create AlarmActivity
      val notifBytes = serializeObject(tNotif)
      val fenceBytes = serializeObject(tFence)
      val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
        putExtra("tNotif", notifBytes)
        putExtra("tFence", fenceBytes)
      }
      context.startForegroundService(serviceIntent)
    } else {
      LogUtil.addLog("Attempt to send Notification \"${tNotif.message}\", \"${tFence.fenceName}\"")
      // Send Notification
      sendNotification(
        context,
        tFence,
        tNotif
      )
    }
  }
}
