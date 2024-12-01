package de.tobibrtnr.geofication.util.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.DELETE_ACTION
import de.tobibrtnr.geofication.DISABLE_ACTION
import de.tobibrtnr.geofication.NOTIFICATION_ID
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// This receiver will be triggered if the user uses one of the
// quick action buttons in a notification.
class NotificationReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    CoroutineScope(Dispatchers.Main).launch {
      ServiceProvider.setInstance(context)

      val geofenceViewModel = ServiceProvider.geofenceViewModel()
      val geoficationViewModel = ServiceProvider.geoficationViewModel()
      val geoficationsList = geoficationViewModel.getAll()

      val id = intent.getIntExtra("id", -1)

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.cancel(NOTIFICATION_ID)

      runBlocking {
        when (intent.action) {
          // Disable Geofication
          DISABLE_ACTION -> {
            val tNotif = geoficationsList.find { it.fenceid == id }
            tNotif?.let {
              geoficationViewModel.setActive(false, tNotif.id)
            }
            geofenceViewModel.setActive(false, id)
          }

          // Delete Geofication
          DELETE_ACTION -> {
            geofenceViewModel.delete(id)
          }
        }
      }
    }
  }
}
