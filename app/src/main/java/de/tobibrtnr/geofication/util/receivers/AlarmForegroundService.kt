package de.tobibrtnr.geofication.util.receivers

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.tobibrtnr.geofication.AlarmActivity
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.misc.createNotificationChannel
import de.tobibrtnr.geofication.util.storage.log.LogUtil

private const val CHANNEL_ID = "geofication_general"
class AlarmForegroundService : Service() {

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    LogUtil.addLog("Start Alarm Foreground Service!")
    // Create a notification channel for devices running Android Oreo (API level 26) and above
    createNotificationChannel(this)

    // Show a notification to comply with Android's foreground service requirements
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Alarm")
      .setContentText("An alarm is active")
      .setSmallIcon(R.drawable.ic_notification)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .build()

    if (Build.VERSION.SDK_INT < 34) {
      startForeground(1, notification)
    } else {
      startForeground(1, notification,
        FOREGROUND_SERVICE_TYPE_LOCATION)
    }

    // Start the AlarmActivity in a new task
    val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
      putExtra("tNotif", intent.getByteArrayExtra("tNotif"))
    }
    startActivity(alarmIntent)

    // Stop the service after starting the activity
    stopSelf()
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}
