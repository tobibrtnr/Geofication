package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.runBlocking


class BootCompletedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
      return
    }

    runBlocking {
      addAllGeofences(context)
    }
  }
}

suspend fun addAllGeofences(context: Context) {
  ServiceProvider.setInstance(context)

  val geofences = GeofenceUtil.getGeofences()
  geofences.forEach {
    GeofenceUtil.addGeofence(
      context,
      it
    )
  }
}