package de.tobibrtnr.geofication

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.ui.infos.openLink
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.Vibrate.Companion.cancelVibration
import de.tobibrtnr.geofication.util.misc.Vibrate.Companion.startVibratePattern
import de.tobibrtnr.geofication.util.misc.getByteInput
import de.tobibrtnr.geofication.util.storage.LocaleUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import de.tobibrtnr.geofication.util.storage.log.LogUtil
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil

class AlarmActivity : ComponentActivity() {
  private var ringtone: Ringtone? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    ServiceProvider.setInstance(this)

    LocaleUtil.init(this)
    UnitUtil.init(this)
    SettingsUtil.init()

    LogUtil.addLog("AlarmActivity started.")

    val tNotif = getByteInput(intent.getByteArrayExtra("tNotif")) as Geofication

    turnScreenOn()

    // Wake up the screen
    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(
      PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
      "AlarmClock::WakeLockTag"
    )
    wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)


    // Play the default alarm ringtone
    ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
    ringtone?.play()

    // Vibrate pattern
    startVibratePattern(this, 1000)

    setContent {
      GeoficationTheme {
        FullScreenAlarm(
          tNotif.message,
          tNotif.link,
          onStopAlarm = {
            stopAlarm()
          },
          onOpenLink = {
            openWebLink(it)
          }
        )
      }
    }
  }

  private fun stopAlarm() {
    ringtone?.stop()
    cancelVibration(this)
    finish()
  }

  private fun openWebLink(link: String) {
    openLink(link, this)
    finish()
  }

  override fun onDestroy() {
    super.onDestroy()
    stopAlarm()
  }

  /**
   * Source: https://github.com/yuriykulikov/AlarmClock
   * ## Turns the screen on
   *
   * See https://github.com/yuriykulikov/AlarmClock/issues/360 It seems that on some devices with
   * API>=27 calling `setTurnScreenOn(true)` is not enough, so we will just add all flags regardless
   * of the API level, and call `setTurnScreenOn(true)` if API level is 27+
   *
   * ### 3.07.01 reference In `3.07.01` we added these 4 flags:
   * ```
   * final Window win = getWindow();
   * win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
   * win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
   *         | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
   *         | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
   * ```
   */
  private fun turnScreenOn() {
    if (Build.VERSION.SDK_INT >= 27) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
    }
    // Deprecated flags are required on some devices, even with API>=27
    window.addFlags(
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
          WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
  }
}


@Composable
fun FullScreenAlarm(
    message: String,
    link: String,
    onOpenLink: (String) -> Unit,
    onStopAlarm: () -> Unit
) {

  Box(
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    contentAlignment = BiasAlignment(0f, -0.25f)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(R.string.geofication),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = message,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(Modifier.height(16.dp))
      Button(onClick = onStopAlarm) {
        Text(stringResource(R.string.stop_alarm))
      }
      if (link.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = {
          onOpenLink(link)
        }) {
          Text(stringResource(R.string.open_notification_link))
        }
      }
    }
  }
}
