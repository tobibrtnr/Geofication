package de.tobibrtnr.geofication

import android.app.AlarmManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.MapsInitializer
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.receivers.AlarmForegroundService
import de.tobibrtnr.geofication.util.storage.setting.LocaleUtil
import de.tobibrtnr.geofication.util.storage.setting.UnitUtil
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil
import java.util.Locale

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    // Create custom splash screen with fade out animation
    val splashScreen = installSplashScreen()
    splashScreen.setOnExitAnimationListener { splashScreenProvider ->
      enableEdgeToEdge()
      val fadeOut = ObjectAnimator.ofFloat(splashScreenProvider.view, View.ALPHA, 0f)
      fadeOut.duration = 300L
      fadeOut.doOnEnd {
        splashScreenProvider.remove()
        enableEdgeToEdge()
      }
      fadeOut.start()
    }

    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    // Stop alarm foreground service if one is running
    val stopServiceIntent = Intent(this, AlarmForegroundService::class.java)
    stopService(stopServiceIntent)

    // Get optional geo intent query
    val intentQuery = getIntentQuery(intent)

    // Starting with Android S, you have to ask for permission to schedule an exact alarm
    // (for e.g. "Notify x minutes after entering Geofence")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
      if (alarmManager?.canScheduleExactAlarms() == false) {
        Intent().also { intent ->
          intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
          this.startActivity(intent)
        }
      }
    }

    // Initialize utilities
    MapsInitializer.initialize(this)
    ServiceProvider.setInstance(this)
    LocaleUtil.init(this)
    SettingsUtil.init()
    UnitUtil.init()

    // Get Geofication ID if there is one to open on startup
    val openGeoId = intent.getIntExtra("openGeoId", -1)

    setContent {
      GeoficationTheme {
        GeoficationApp(openGeoId = openGeoId, intentQuery = intentQuery)
      }
    }
  }

  // Implementing this method restarts the activity
  // when dark mode is enabled or disabled.
  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    restartActivity(this)
  }

  // Set locale when attaching the base context at the
  // start of the application
  override fun attachBaseContext(newBase: Context) {
    ServiceProvider.setInstance(newBase)
    LocaleUtil.init(newBase)

    val newContext = setContextLocale(newBase, LocaleUtil.getLocale())
    super.attachBaseContext(newContext)
  }

  // Get and handle an intent query (if you select "Open with Geofication"
  // on a location or on a "geo:" query
  private fun getIntentQuery(intent: Intent): String {
    val uri: Uri? = intent.data
    uri?.let {
      if(uri.scheme == "geo") {
        val query = uri.query
        val ssp = uri.schemeSpecificPart

        if(query != null && query.startsWith("q=")) {
          return query.substring(2).split("(")[0]
        } else if (ssp != null) {
          return ssp.split("?")[0]
        }
      }
    }
    return ""
  }
}

// Create a new Context object with the current
// language applied.
private fun setContextLocale(context: Context, language: String): Context {
  val configuration = context.resources.configuration

  val locale = Locale(language)
  Locale.setDefault(locale)

  return context.createConfigurationContext(configuration.apply {
    setLocale(locale)
  })
}

// Restart the app
private fun restartActivity(activity: Activity) {
  val intent = Intent(activity, activity::class.java)
  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
  activity.startActivity(intent)
  activity.finish()
}
