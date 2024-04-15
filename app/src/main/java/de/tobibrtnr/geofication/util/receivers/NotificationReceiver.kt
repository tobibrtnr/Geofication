package de.tobibrtnr.geofication.util.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import kotlinx.coroutines.runBlocking

class NotificationReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    ServiceProvider.setInstance(context)

    val id = intent.getIntExtra("id", -1)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(1)

    runBlocking {
      when (intent.action) {
        "DISABLE_ACTION" -> {
          GeofenceUtil.setNotifActive(id, false)
        }
        "DELETE_ACTION" ->
          GeofenceUtil.deleteGeofence(id)
        else -> {}
      }

    }
  }
}