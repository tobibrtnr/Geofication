package de.tobibrtnr.geofication.util.misc

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tobibrtnr.geofication.MainActivity
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.receivers.NotificationReceiver
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import kotlin.random.Random

// Constants
private const val CHANNEL_ID = "geofication_general"
private const val NOTIFICATION_ID = 1

/**
 * Send a notification with a title and message.
 */
fun sendNotification(context: Context, fence: Geofence, notif: Geofication) {
  // Create a notification channel for devices running Android Oreo (API level 26) and above
  createNotificationChannel(context)

  val intent = Intent(context, MainActivity::class.java).apply {
    //flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    putExtra("openGeoId", fence.id)
  }
  val pendingIntent = PendingIntent.getActivity(
    context,
    Random.nextInt(),
    intent,
    PendingIntent.FLAG_MUTABLE
  )

  // Create a notification builder
  val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle(notif.message)
    .setContentText(fence.fenceName)
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .setContentIntent(pendingIntent)

  // Add quick action buttons depending on trigger mode
  if(notif.onTrigger == 0 || notif.onTrigger == 1) {
    val deleteIntent = Intent(context, NotificationReceiver::class.java).apply {
      action = "DELETE_ACTION"
      putExtra("id", fence.id)
    }
    val deletePendingIntent = PendingIntent.getBroadcast(
      context,
      Random.nextInt(),
      deleteIntent,
      PendingIntent.FLAG_MUTABLE
    )
    builder.addAction(R.drawable.ic_notification, "Delete", deletePendingIntent)
  }
  if(notif.onTrigger == 0) {
    val disableIntent = Intent(context, NotificationReceiver::class.java).apply {
      action = "DISABLE_ACTION"
      putExtra("id", notif.id)
    }
    val deletePendingIntent = PendingIntent.getBroadcast(
      context,
      Random.nextInt(),
      disableIntent,
      PendingIntent.FLAG_MUTABLE
    )
    builder.addAction(R.drawable.ic_notification, "Disable", deletePendingIntent)
  }

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
