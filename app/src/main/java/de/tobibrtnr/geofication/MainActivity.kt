package de.tobibrtnr.geofication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import de.tobibrtnr.geofication.ui.GeoficationApp
import de.tobibrtnr.geofication.ui.theme.GeoficationTheme
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.ServiceProvider

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    ServiceProvider.setInstance(this)

    setContent {
      GeoficationTheme {
        GeoficationApp()
      }
    }
  }
}
