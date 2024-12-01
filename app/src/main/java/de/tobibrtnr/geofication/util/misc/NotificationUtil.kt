package de.tobibrtnr.geofication.util.misc

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tobibrtnr.geofication.ACTION_EXECUTE_FUNCTION
import de.tobibrtnr.geofication.DELETE_ACTION
import de.tobibrtnr.geofication.DISABLE_ACTION
import de.tobibrtnr.geofication.CHANNEL_ID
import de.tobibrtnr.geofication.NOTIFICATION_ID
import de.tobibrtnr.geofication.MainActivity
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.util.receivers.AlarmForegroundService
import de.tobibrtnr.geofication.util.receivers.NotificationReceiver
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import kotlin.random.Random

/**
 * Send a notification with a title and message.
 */
fun sendNotification(
  context: Context,
  fence: Geofence,
  notif: Geofication
) {
  val notification = createNotification(context, fence, notif)

  // Show the notification
  with(NotificationManagerCompat.from(context)) {
    if ((ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED) &&
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    ) {
      ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
      )
    }

    notify(NOTIFICATION_ID, notification)
  }
}

fun createNotification(context: Context, fence: Geofence, notif: Geofication): Notification {
  // Create a notification channel for devices running Android Oreo (API level 26) and above
  createNotificationChannel(context, CHANNEL_ID)

  val pendingIntent = if(notif.link.isNotEmpty()) {
    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(notif.link))
    linkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    TaskStackBuilder.create(context).run {
      addNextIntentWithParentStack(linkIntent)

      getPendingIntent(
        System.currentTimeMillis().toInt(),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    }
  } else {
    val intent = Intent(context, MainActivity::class.java).apply {
      putExtra("openGeoId", fence.id)
    }
    PendingIntent.getActivity(
      context,
      Random.nextInt(),
      intent,
      PendingIntent.FLAG_MUTABLE
    )
  }

  // Add "Alarm" to title, if it is an alarm
  val title = if (notif.isAlarm) {
    context.getString(R.string.alarm_notif, notif.message)
  } else {
    notif.message
  }

  // Create a notification builder
  val builder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle(title)
    .setContentText(fence.fenceName)
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .setContentIntent(pendingIntent)

  // If it is an alarm, you can stop it by swiping the notification away
  // or use the quick action button
  if (notif.isAlarm) {
    val clickIntent = Intent(context, AlarmForegroundService::class.java).apply {
      action = ACTION_EXECUTE_FUNCTION
    }
    val deleteIntent = PendingIntent.getService(context, System.currentTimeMillis().toInt(), clickIntent, PendingIntent.FLAG_IMMUTABLE)

    builder
      .setDeleteIntent(deleteIntent)
      .setOngoing(true)
      .addAction(
        R.drawable.ic_notification,
        context.getString(R.string.stop_alarm),
        deleteIntent
      )
  } else {
    // For non-alarm notifications:
    // Add quick action buttons depending on trigger mode and alarm clock
    if (notif.onTrigger == 0 || notif.onTrigger == 1) {
      val deleteIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = DELETE_ACTION
        putExtra("id", fence.id)
      }
      val deletePendingIntent = PendingIntent.getBroadcast(
        context,
        Random.nextInt(),
        deleteIntent,
        PendingIntent.FLAG_MUTABLE
      )
      builder.addAction(
        R.drawable.ic_notification,
        context.getString(R.string.delete),
        deletePendingIntent
      )
    }
    if (notif.onTrigger == 0) {
      val disableIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = DISABLE_ACTION
        putExtra("id", notif.id)
      }
      val deletePendingIntent = PendingIntent.getBroadcast(
        context,
        Random.nextInt(),
        disableIntent,
        PendingIntent.FLAG_MUTABLE
      )
      builder.addAction(
        R.drawable.ic_notification,
        context.getString(R.string.disable_capitalized),
        deletePendingIntent
      )
    }

  }

  return builder.build()
}

/**
 * Create a Notification Channel, needed starting with Android O
 */
fun createNotificationChannel(context: Context, channelName: String) {
  val channel = NotificationChannel(
    channelName,
    context.getString(R.string.geofication),
    NotificationManager.IMPORTANCE_HIGH
  ).apply {
    description = context.getString(R.string.geofication_notifications)
  }

  // Register the channel with the system
  val notificationManager: NotificationManager =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  notificationManager.createNotificationChannel(channel)
}
