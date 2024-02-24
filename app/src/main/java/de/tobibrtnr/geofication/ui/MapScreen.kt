package de.tobibrtnr.geofication.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Vibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

  ////////////////////////////////////////
  // TODO These local functions are maybe convenient, but look really bad
  val context = LocalContext.current

  fun processInput(
    enteredString: String,
    enteredFloat: Float,
    pos: LatLng
  ) {
    GeofenceUtil.addGeofence(
      context,
      geofencingClient,
      db,
      enteredString,
      pos.latitude,
      pos.longitude,
      enteredFloat
    )

    CoroutineScope(Dispatchers.Default).launch {
      val geofences = GeofenceUtil.getGeofences(db)
      geofencesArray = geofences
    }
  }

  fun openPopup(pos: LatLng) {
    // Create an AlertDialog.Builder
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Enter String and Float")

    // Create layout for EditTexts
    val layout = LinearLayout(context)
    layout.orientation = LinearLayout.VERTICAL

    // Create EditText for string input
    val stringInput = EditText(context)
    stringInput.hint = "Enter String"
    layout.addView(stringInput)

    // Create EditText for float input
    val floatInput = EditText(context)
    floatInput.hint = "Enter Float"
    layout.addView(floatInput)

    builder.setView(layout)

    // Set up the buttons
    builder.setPositiveButton("OK") { _, _ ->
      val enteredString = stringInput.text.toString()
      val enteredFloat = try {
        floatInput.text.toString().toFloat()
      } catch (e: NumberFormatException) {
        Toast.makeText(context, "Invalid float input", Toast.LENGTH_SHORT).show()
        return@setPositiveButton
      }

      // Process the entered values as needed
      processInput(enteredString, enteredFloat, pos)
    }

    builder.setNegativeButton("Cancel") { dialog, _ ->
      dialog.cancel()
    }

    // Create and show the AlertDialog
    val alertDialog = builder.create()
    alertDialog.show()
  }

  fun longClick(latLng: LatLng) {
    Vibrate.vibrate(context, 15)
    openPopup(latLng)
  }
  // END local functions
  ////////////////////////////

  // Return Composable
  Box(Modifier.fillMaxSize()) {
    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapLongClick = {
        longClick(it)
      },
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState
    ) {

      // Place each Geofence as Marker and Circle on the Map
      geofencesArray.forEach { geo ->
        val mState = MarkerState(position = LatLng(geo.latitude, geo.longitude))
        MarkerInfoWindowContent(
          state = mState,
          title = geo.gid,
          onInfoWindowClick = {
            mState.hideInfoWindow()
            CoroutineScope(Dispatchers.Default).launch {
              GeofenceUtil.deleteGeofence(geo.gid, geofencingClient, db)

              val geofences = GeofenceUtil.getGeofences(db)
              geofencesArray = geofences

            }
          },
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
