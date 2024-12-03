package de.tobibrtnr.geofication.util.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import de.tobibrtnr.geofication.ACTION_DELAY
import de.tobibrtnr.geofication.BuildConfig
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.serializeObject
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {
  // This receiver will be triggered when a geofence is entered or exited.
  // It handles everything from checking if the geofence is active, to setting
  // properties like the trigger count correctly, up to sending the appropriate
  // notification or alarm to the user.
  override fun onReceive(context: Context, intent: Intent) {
    CoroutineScope(Dispatchers.Main).launch {
      ServiceProvider.setInstance(context)

      val geofenceViewModel = ServiceProvider.geofenceViewModel()
      val geoficationViewModel = ServiceProvider.geoficationViewModel()

      val geofencesList = geofenceViewModel.getAll()
      val geoficationsList = geoficationViewModel.getAll()

      LogUtil.addLog("GeofenceBroadcastReceiver started.")

      val geofencingEvent = GeofencingEvent.fromIntent(intent)

      // Check if the Event has any errors
      if (geofencingEvent?.hasError() == true) {
        val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
        LogUtil.addLog("Geofence contains error: $errorMessage")
        return@launch
      }

      // Get the transition type.
      val geofenceTransition = geofencingEvent?.geofenceTransition

      // Test that the reported transition was of interest.
      if (
        geofenceTransition != com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER &&
        geofenceTransition != com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
      ) {
        return@launch
      }

      // Get the geofences that were triggered. A single event can trigger multiple geofences.
      val triggeringGeofences = geofencingEvent.triggeringGeofences

      triggeringGeofences?.forEach { geofence ->

        LogUtil.addLog(
          "Geofence ${geofence.requestId} was triggered.\n" +
              "Transition =  $geofenceTransition"
        )

        runBlocking {
          // Check if geofence is active
          val tFence = geofencesList.find { it.id == geofence.requestId.toInt() }
            ?: return@runBlocking

          // If no corresponding Geofication has been found, do not trigger
          val tNotif = geoficationsList.find { it.fenceid == geofence.requestId.toInt() }
            ?: return@runBlocking

          if(!isTriggerValid(tFence, tNotif, geofenceTransition)) {
            return@runBlocking
          }

          // Determine if a delay was set or if
          // the Geofication is triggered immediately
          if (tNotif.delay == 0) {
            handleGeofication(context, tFence, tNotif)
          } else {
            scheduleGeoficationTrigger(context, tFence, tNotif)
          }
        }
      }
    }
  }
}

private fun isTriggerValid(
  geofence: Geofence,
  geofication: Geofication,
  geofenceTransition: Int
): Boolean {
  // If geofence not active, do nothing and return
  if (!geofence.active) return false

  // If geofence was recently created or edited,
  // do nothing and return (in release mode)
  val timeDelta = if (BuildConfig.DEBUG) {
    0
  } else {
    TimeUnit.MINUTES.toMillis(3)
  }

  val currentTime = System.currentTimeMillis()
  if (currentTime - geofence.created < timeDelta ||
    currentTime - geofence.lastEdit < timeDelta
  ) {
    LogUtil.addLog("Geofence not triggered as it was created too recent.")
    return false
  }

  // If flags does not equal the transition flag and is
  // not 3 (all transitions), do not trigger
  if (geofication.flags != geofenceTransition && geofication.flags != 3) {
    return false
  }

  return true
}

// If the Geofication has a delay, set up an exact alarm and intent
// to trigger the Geofication at the desired time
private fun scheduleGeoficationTrigger(
  context: Context,
  geofence: Geofence,
  geofication: Geofication
) {
  val fenceBytes = serializeObject(geofence)
  val notifBytes = serializeObject(geofication)

  val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
    action = ACTION_DELAY
    putExtra("tFence", fenceBytes)
    putExtra("tNotif", notifBytes)
  }

  // Delay is x minutes, schedule alarm
  val pendingIntent = PendingIntent.getBroadcast(
    context,
    System.currentTimeMillis().toInt(),
    alarmIntent,
    PendingIntent.FLAG_IMMUTABLE
  )

  val triggerTimeMillis =
    SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(geofication.delay.toLong())

  val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  try {
    alarmManager.setExact(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      triggerTimeMillis,
      pendingIntent
    )
  } catch (e: SecurityException) {
    LogUtil.addLog("No permission for scheduling exact alarm!\n${e.message}")
  }

  LogUtil.addLog("Worker Request started. Notification in ${geofication.delay} minutes.")
}
