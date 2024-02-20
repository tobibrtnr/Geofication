package de.tobibrtnr.geofication.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import kotlinx.coroutines.tasks.await

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  locationClient: FusedLocationProviderClient,
  geofencingClient: GeofencingClient,
  db: AppDatabase
) {

  val uiSettings by remember {
    mutableStateOf(MapUiSettings(zoomControlsEnabled = false))
  }

  val properties by remember {
    mutableStateOf(MapProperties(isMyLocationEnabled = true, isBuildingEnabled = true))
  }

  val cameraPositionState = rememberCameraPositionState {
    CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
  }

  // If location permission is given, get it and set the camera position to it
  if (ActivityCompat.checkSelfPermission(
      LocalContext.current,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
      LocalContext.current,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
  ) {
    // Asynchronously set the position of the map camera to current position on startup
    LaunchedEffect(Unit) {
      val location = locationClient.lastLocation.await()
      val currentLocation = LatLng(location.latitude, location.longitude)

      // Update the camera position state with the current location
      cameraPositionState.position =
        CameraPosition(currentLocation, 15f, 0f, 0f)
    }
  }

  // Fetch all active geofences from storage
  var geofencesArray by remember { mutableStateOf(emptyList<Geofence>()) }
  LaunchedEffect(Unit) {
    val geofences = GeofenceUtil.getGeofences(db)
    geofencesArray = geofences
  }

  // Return Composable
  Box(Modifier.fillMaxSize()) {
    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapClick = {
        println("Click! $it")
      },
      onMapLongClick = {
        println("Long Click! $it")
      },
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState
    ) {

      // Place each Geofence as Marker and Circle on the Map
      geofencesArray.forEach {
        AdvancedMarker(
          state = MarkerState(position = LatLng(it.latitude, it.longitude)),
          title = it.gid
        )
        Circle(
          center = LatLng(it.latitude, it.longitude),
          radius = it.radius.toDouble(),
          strokeColor = Color.Red,
          fillColor = Color.Red.copy(alpha = 0.25f)
        )
      }

    }

    DisappearingScaleBar(
      modifier = Modifier
        .padding(top = 5.dp, end = 15.dp)
        .align(Alignment.BottomEnd),
      cameraPositionState = cameraPositionState,

      )
  }
}
