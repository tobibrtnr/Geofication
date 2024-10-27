package de.tobibrtnr.geofication.util.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import de.tobibrtnr.geofication.BuildConfig
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.serializeObject
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.LogUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

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

      triggeringGeofences?.forEachIndexed { _, geofence ->

        LogUtil.addLog(
          "Geofence ${geofence.requestId} was triggered.\n" +
              "Entering? $geofenceTransition"
        )

        runBlocking {
          // Check if geofence is active
          val tFence = GeofenceUtil.getGeofenceById(geofence.requestId.toInt())

          // If geofence not active, do nothing and return
          if (!tFence.active) return@runBlocking

          // If geofence was recently created or edited, do nothing and return (in release mode)
          val timeDelta = if (BuildConfig.DEBUG) {
            0
          } else {
            TimeUnit.MINUTES.toMillis(3)
          }

          val currentTime = System.currentTimeMillis()
          if(currentTime - tFence.created < timeDelta ||
             currentTime - tFence.lastEdit < timeDelta) {
            LogUtil.addLog("Geofence not triggered as it was created too recent.")
            return@runBlocking
          }

          GeofenceUtil.incrementFenceTriggerCount(geofence.requestId.toInt())

          val geofications = GeofenceUtil.getGeoficationByGeofence(geofence.requestId.toInt())

          if (geofications.isEmpty()) {
            return@runBlocking
          }

          // Get current triggered geofication
          val tNotif = geofications[0]

          val fenceBytes = serializeObject(tFence)
          val notifBytes = serializeObject(tNotif)

          val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "de.tobibrtnr.geofication.GEOFICATION_ALARM"
            putExtra("tFence", fenceBytes)
            putExtra("tNotif", notifBytes)
            putExtra("geofenceTransition", geofenceTransition)
          }

          if (tNotif.delay == 0) {
            // No delay, send notification immediately
            handleGeofication(context, tFence, tNotif, geofenceTransition)
          } else {
            // Delay is x minutes, schedule alarm
            val pendingIntent = PendingIntent.getBroadcast(
              context, System.currentTimeMillis().toInt(), alarmIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTimeMillis =
              SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(tNotif.delay.toLong())

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

            LogUtil.addLog("Worker Request started. Notification in ${tNotif.delay} minutes.")
          }
        }
      }
    } else {
      println("Geofence registered, but not interested")
    }
  }
}
