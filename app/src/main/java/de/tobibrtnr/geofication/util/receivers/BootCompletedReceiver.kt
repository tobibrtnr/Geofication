package de.tobibrtnr.geofication.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import kotlinx.coroutines.runBlocking

// When the phone is rebooted, you have to re-add all
// existing geofences in order to be triggered correctly
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

// Re-add all existing geofences
suspend fun addAllGeofences(context: Context) {
  ServiceProvider.setInstance(context)

  val geofenceViewModel = ServiceProvider.geofenceViewModel()
  val geofences = geofenceViewModel.getAll()

  val geoficationsViewModel = ServiceProvider.geoficationViewModel()
  val geofications = geoficationsViewModel.getAll()

  geofences.forEach { geofence ->
    // Update last edit time for Geofences in order to not get triggered instantly
    geofence.lastEdit = System.currentTimeMillis()

    // We also need to re-add all geofications, as they are removed when
    // their foreign key geofence is removed / re-added
    geofenceViewModel.addGeofence(
      context,
      geofence,
      geofications.find { geofence.id == it.fenceid},
      forceAddGeofence = true
    )
  }
}
