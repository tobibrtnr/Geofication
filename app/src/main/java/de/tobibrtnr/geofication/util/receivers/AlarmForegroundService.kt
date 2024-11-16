package de.tobibrtnr.geofication.util.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.misc.createNotification
import de.tobibrtnr.geofication.util.misc.getByteInput
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.log.LogUtil


const val ACTION_EXECUTE_FUNCTION = "de.tobibrtnr.ACTION_EXECUTE_FUNCTION"
private const val NOTIFICATION_ID = 1
class AlarmForegroundService : Service() {

  private var mediaPlayer: MediaPlayer = MediaPlayer()

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    if (intent.action == ACTION_EXECUTE_FUNCTION) {
      dismissNotification()
      return START_NOT_STICKY
    }

    LogUtil.addLog("Start Alarm Foreground Service!")

    val tNotif = getByteInput(intent.getByteArrayExtra("tNotif")) as Geofication
    val tFence = getByteInput(intent.getByteArrayExtra("tFence")) as Geofence

    val notification = createNotification(this, tFence, tNotif)

    notification.flags = Notification.FLAG_ONGOING_EVENT

    if (Build.VERSION.SDK_INT < 34) {
      startForeground(NOTIFICATION_ID, notification)
    } else {
      startForeground(NOTIFICATION_ID, notification,
        FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }

    // Wake up the screen
    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(
      PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
      "AlarmClock::WakeLockTag"
    )
    wakeLock.acquire(10 * 60 * 1000L)

    // Start default alarm
    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    mediaPlayer.setDataSource(this, alarmUri)

    mediaPlayer.setAudioAttributes(
      AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()
    )
    mediaPlayer.prepare()
    mediaPlayer.start()

    // Start vibrate with pattern
    Vibrate.startVibratePattern(this, 1000)

    return START_NOT_STICKY;
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    dismissNotification()
  }

  private fun dismissNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID)
    Vibrate.cancelVibration(this)
    mediaPlayer.stop()
    mediaPlayer.release()
    stopSelf()
  }
}
