package de.tobibrtnr.geofication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
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
      it.gid,
      it.latitude,
      it.longitude,
      it.radius,
      it.color
    )
  }
}
