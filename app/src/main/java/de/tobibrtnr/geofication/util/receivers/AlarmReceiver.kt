package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.ACTION_DELAY
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
  // This receiver is triggered when a delay that was set to the
  // Geofication elapsed.
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == ACTION_DELAY) {
      ServiceProvider.setInstance(context)

      val tFence = getByteInput(intent.getByteArrayExtra("tFence")) as Geofence
      val tNotif = getByteInput(intent.getByteArrayExtra("tNotif")) as Geofication

      handleGeofication(context, tFence, tNotif)
    }
  }
}

// Handle the triggering of the Geofication and send
// the corresponding notification / alarm to the user.
fun handleGeofication(
  context: Context,
  geofence: Geofence,
  geofication: Geofication
) {
  CoroutineScope(Dispatchers.IO).launch {
    val geofenceViewModel = ServiceProvider.geofenceViewModel()
    val geoficationViewModel = ServiceProvider.geoficationViewModel()

    // Increment the trigger count of the Geofication and the Geofence
    geofenceViewModel.incrementTriggerCount(geofence.id)
    geoficationViewModel.incrementTriggerCount(geofication.id)

    // On trigger, the user can choose between deactivating or deleting
    // the Geofication or doing nothing.
    when (geofication.onTrigger) {
      1 -> {
        geofenceViewModel.setActive(false, geofence.id)
        geoficationViewModel.setActive(false, geofication.id)
      }
      2 -> {
        geofenceViewModel.delete(geofence.id)
      }
    }

    LogUtil.addLog("Send \"${geofication.message}\" at \"${geofence.fenceName}\"")

    if (geofication.isAlarm) {
      // Create AlarmActivity and start it
      val notifBytes = serializeObject(geofication)
      val fenceBytes = serializeObject(geofence)
      val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
        putExtra("tNotif", notifBytes)
        putExtra("tFence", fenceBytes)
      }
      context.startForegroundService(serviceIntent)
    } else {
      // Send Notification
      sendNotification(
        context,
        geofence,
        geofication
      )
    }
  }
}
