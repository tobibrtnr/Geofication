package de.tobibrtnr.geofication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.maps.MapsInitializer
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.storage.UnitUtil

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()

    super.onCreate(savedInstanceState)

    MapsInitializer.initialize(this)
    ServiceProvider.setInstance(this)

    UnitUtil.init()

    val openGeoId = intent.getIntExtra("openGeoId", -1)

    setContent {
      GeoficationTheme {
        GeoficationApp(openGeoId = openGeoId)
      }
    }
  }
}
