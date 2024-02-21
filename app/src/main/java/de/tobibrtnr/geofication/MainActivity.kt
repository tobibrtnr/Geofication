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

class MainActivity : ComponentActivity() {
  // Room storage object
  private lateinit var db: AppDatabase

  // Globally needed clients
  private lateinit var geofencingClient: GeofencingClient
  private lateinit var locationClient: FusedLocationProviderClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    db = Room.databaseBuilder(
      this,
      AppDatabase::class.java, "geofication-database"
    ).build()

    locationClient = LocationServices.getFusedLocationProviderClient(this)
    geofencingClient = LocationServices.getGeofencingClient(this)

    setContent {
      GeoficationTheme {
        GeoficationApp(
          db = db,
          geofencingClient = geofencingClient,
          locationClient = locationClient
        )
      }
    }
  }
}
