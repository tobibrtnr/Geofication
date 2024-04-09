package de.tobibrtnr.geofication.util.misc

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tobibrtnr.geofication.R

// Constants
private const val CHANNEL_ID = "geofication_general"
private const val NOTIFICATION_ID = 1

/**
 * Send a notification with a title and message.
 */
fun sendNotification(context: Context, title: String, message: String) {
  // Create a notification channel for devices running Android Oreo (API level 26) and above
  createNotificationChannel(context)

  // Create a notification builder
  val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle(title)
    .setContentText(message)
    .setPriority(NotificationCompat.PRIORITY_HIGH)

  // Show the notification
  with(NotificationManagerCompat.from(context)) {
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
          context as Activity,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
        )
      }
    }
    notify(NOTIFICATION_ID, builder.build())
  }
}

/**
 * Create a Notification Channel, needed starting with Android O
 */
private fun createNotificationChannel(context: Context) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Geofication",
      NotificationManager.IMPORTANCE_HIGH
    ).apply {
      description = "Geofication Notifications"
    }

    // Register the channel with the system
    val notificationManager: NotificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }
}
