package de.tobibrtnr.geofication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import de.tobibrtnr.geofication.ui.GeoMap
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

    db = Room.databaseBuilder(
      this,
      AppDatabase::class.java, "geofication-database"
    ).build()

    locationClient = LocationServices.getFusedLocationProviderClient(this)
    geofencingClient = LocationServices.getGeofencingClient(this)

    // Add a geofence for testing
    GeofenceUtil.addGeofence(
      this,
      geofencingClient,
      db,
      "myTestGeofence",
      37.43273342801908,
      -122.09336310625076,
      25.0f
    )

    GeofenceUtil.addGeofence(
      this,
      geofencingClient,
      db,
      "athleticFields",
      37.42792352846094,
      -122.09414027631283,
      50.25f
    )

    setContent {
      GeoficationTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          GeoMap(locationClient = locationClient, geofencingClient = geofencingClient, db = db)
        }
      }
    }
  }
}
