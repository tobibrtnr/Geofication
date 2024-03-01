package de.tobibrtnr.geofication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.maps.MapsInitializer
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.ServiceProvider

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    MapsInitializer.initialize(this)

    enableEdgeToEdge()

    ServiceProvider.setInstance(this)

    setContent {
      GeoficationTheme {
        GeoficationApp()
      }
    }
  }
}
