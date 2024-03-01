package de.tobibrtnr.geofication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.ServiceProvider
import de.tobibrtnr.geofication.util.Vibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
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
      val locationClient = ServiceProvider.location()
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
    val geofences = GeofenceUtil.getGeofences()
    geofencesArray = geofences
  }

  var openDialog by remember { mutableStateOf(false) }
  var openDialogGeofence by remember { mutableStateOf(false) }

  val context = LocalContext.current
  var selectedPosition by remember { mutableStateOf(LatLng(0.0, 0.0))}

  fun longClick(latLng: LatLng) {
    Vibrate.vibrate(context, 15)
    selectedPosition = latLng
    openDialogGeofence = true
  }

  // Return Composable
  if (openDialog) {
    AddGeoficationPopup { openDialog = false }
  }
  if (openDialogGeofence) {
    AddGeofencePopup(selectedPosition, { openDialogGeofence = false }, {
      CoroutineScope(Dispatchers.Default).launch {
        val geofences = GeofenceUtil.getGeofences()
        geofencesArray = geofences
      }
      openDialogGeofence = false
    })
  }
  Box(Modifier.fillMaxSize()) {
    IconButton(
      onClick = { openDialog = true },
      modifier = Modifier
        .size(100.dp)
        .padding(16.dp)
        .clipToBounds()
        .background(Color(0xFFC1E4CB), MaterialTheme.shapes.medium)
        .border(1.dp, Color(0xFFA8DAB5), MaterialTheme.shapes.medium)
        .zIndex(1f)
        .align(Alignment.BottomEnd)
    ) {
      Icon(Icons.Filled.Add, contentDescription = "Add")
    }

    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapLongClick = {
        longClick(it)
      },
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
    ) {

      // Place each Geofence as Marker and Circle on the Map
      geofencesArray.forEach { geo ->
        val bdf = BitmapDescriptorFactory.defaultMarker(geo.color.hue)

        val mState = MarkerState(position = LatLng(geo.latitude, geo.longitude))
        MarkerInfoWindowContent(
          state = mState,
          title = geo.gid,
          onInfoWindowClick = {
            mState.hideInfoWindow()
            CoroutineScope(Dispatchers.Default).launch {
              GeofenceUtil.deleteGeofence(geo.gid)

              val geofences = GeofenceUtil.getGeofences()
              geofencesArray = geofences

            }
          },
          icon = bdf,
          content = { _ ->

            Column(modifier = Modifier.padding(16.dp)) {
              Text(geo.gid)
              Spacer(modifier = Modifier.height(8.dp))
              Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete geofence"
              )
            }
          }

        )
        Circle(
          center = LatLng(geo.latitude, geo.longitude),
          radius = geo.radius.toDouble(),
          strokeColor = geo.color.color,
          fillColor = geo.color.color.copy(alpha = 0.25f)
        )
      }
    }

    DisappearingScaleBar(
      modifier = Modifier
        .padding(bottom = 10.dp, end = 105.dp)
        .align(Alignment.BottomEnd),
      cameraPositionState = cameraPositionState
    )
  }
}