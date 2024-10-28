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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.MapsInitializer
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.LocaleUtil
import de.tobibrtnr.geofication.util.storage.SettingsUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {

    installSplashScreen()
    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    var intentQuery = ""
    val uri: Uri? = intent.data
    uri?.let {
      if(uri.scheme == "geo") {
        val query = uri.query
        val ssp = uri.schemeSpecificPart
        if(query != null && query.startsWith("q=")) {
          intentQuery = query.substring(2).split("(")[0]
        } else if (ssp != null) {
          intentQuery = ssp.split("?")[0]
        }
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
      if (alarmManager?.canScheduleExactAlarms() == false) {
        Intent().also { intent ->
          intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
          this.startActivity(intent)
        }
      }
    }

    MapsInitializer.initialize(this)
    ServiceProvider.setInstance(this)

    LocaleUtil.init(this)
    UnitUtil.init(this)
    SettingsUtil.init()

    val openGeoId = intent.getIntExtra("openGeoId", -1)

    setContent {
      GeoficationTheme {
        GeoficationApp(openGeoId = openGeoId, intentQuery = intentQuery)
      }
    }
  }

  // Implementing this method stops recreating the activity when
  // dark mode is enabled or disabled.
  override fun onConfigurationChanged(newConfig: Configuration) {
    LocaleUtil.setLocale(this, LocaleUtil.getLocale())
    super.onConfigurationChanged(newConfig)
  }
}
