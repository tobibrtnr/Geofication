package de.tobibrtnr.geofication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication
import de.tobibrtnr.geofication.util.ServiceProvider
import de.tobibrtnr.geofication.util.Vibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  topPadding: Dp
) {

  val uiSettings by remember {
    mutableStateOf(
      MapUiSettings(
        zoomControlsEnabled = false,
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false,
        compassEnabled = false // TODO maybe add own compass component and position
      )
    )
  }

  val properties by remember {
    mutableStateOf(MapProperties(isMyLocationEnabled = true, isBuildingEnabled = true))
  }

  val cameraPositionState = rememberCameraPositionState {
    CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
  }

  var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

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
      currentLocation = LatLng(location.latitude, location.longitude)

      // Update the camera position state with the current location
      cameraPositionState.position =
        CameraPosition(currentLocation, 15f, 0f, 0f)
    }
  }

  // Fetch all active geofences from storage
  var geofencesArray by remember { mutableStateOf(emptyList<Geofence>()) }
  var geoficationsArray by remember { mutableStateOf(emptyList<Geofication>()) }

  LaunchedEffect(Unit) {
    val geofences = GeofenceUtil.getGeofences()
    geofencesArray = geofences
    geoficationsArray = GeofenceUtil.getGeofications()
  }

  var tempGeofenceLocation by remember { mutableStateOf<LatLng?>(null) }
  var tempGeofenceRadius by remember { mutableStateOf(0.0) }

  var openDialog by remember { mutableStateOf(false) }
  var openDialogGeofence by remember { mutableStateOf(false) }

  val context = LocalContext.current
  var selectedPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
  var newRadius by remember { mutableStateOf(0.0) }

  var isMapLoaded by remember { mutableStateOf(false) }

  var markerPopupVisible by remember { mutableStateOf(false) }
  var selectedMarkerId by remember { mutableStateOf("") }

  val geoficationsRow = rememberScrollState()

  fun longClick(latLng: LatLng, tempGeofenceRadius: Double) {
    newRadius = tempGeofenceRadius
    selectedPosition = latLng
    openDialogGeofence = true
  }

  // Return Composable
  if (markerPopupVisible && selectedMarkerId.isNotEmpty()) {
    Dialog(onDismissRequest = { markerPopupVisible = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(400.dp)
          .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row {
            Text(text = selectedMarkerId, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Delete geofence",
              modifier = Modifier
                .clickable {
                  CoroutineScope(Dispatchers.Default).launch {
                    GeofenceUtil.deleteGeofence(selectedMarkerId)

                    val geofences = GeofenceUtil.getGeofences()
                    geofencesArray = geofences

                  }
                  markerPopupVisible = false
                }
            )
          }



          Spacer(modifier = Modifier.height(16.dp))
          var selectedGeofenceNotifs by remember { mutableStateOf(emptyList<Geofication>()) }
          LaunchedEffect(Unit) {
            selectedGeofenceNotifs = GeofenceUtil.getGeoficationByGeofence(selectedMarkerId)
          }
          selectedGeofenceNotifs.forEach {
            Row {
              CircleWithColor(color = it.color.color, radius = 8.dp)
              Spacer(modifier = Modifier.width(16.dp))
              Text(it.gid)
              Spacer(modifier = Modifier.width(16.dp))
              Text(it.flags.toString())
              Spacer(modifier = Modifier.width(16.dp))
              Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Geofication",
                modifier = Modifier
                  .clickable {
                    CoroutineScope(Dispatchers.Default).launch {
                      GeofenceUtil.deleteGeofication(it.gid)
                      selectedGeofenceNotifs =
                        GeofenceUtil.getGeoficationByGeofence(selectedMarkerId)
                    }
                  }
              )
            }
            Spacer(Modifier.height(8.dp))
          }
        }
      }
    }
  }
  if (openDialog) {
    AddGeoficationPopup { openDialog = false }
  }
  if (openDialogGeofence) {
    AddGeofencePopup(selectedPosition, newRadius, { openDialogGeofence = false; }, {
      CoroutineScope(Dispatchers.Default).launch {
        val geofences = GeofenceUtil.getGeofences()
        geofencesArray = geofences
      }
      openDialogGeofence = false
    })
  }
  Box(Modifier.fillMaxSize()) {
    Box(
      Modifier
        .align(Alignment.BottomEnd)
        .zIndex(1f)
        .padding(16.dp)
    ) {
      Column {
        FloatingActionButton(
          onClick = {
            MainScope().launch {
              // Asynchronously set the position of the map camera to current position
              val locationClient = ServiceProvider.location()
              val location = locationClient.lastLocation.await()
              currentLocation = LatLng(location.latitude, location.longitude)

              // Update the camera position state with the current location
              cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                  CameraPosition(
                    currentLocation,
                    15f,
                    0f,
                    0f
                  )
                )
              )
            }
          },
          shape = CircleShape
        ) {
          Icon(
            imageVector = if (SphericalUtil.computeDistanceBetween(
                cameraPositionState.position.target,
                currentLocation
              ) < 0.1
            ) Icons.Filled.MyLocation else Icons.Filled.LocationSearching,
            contentDescription = "Get location"
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FloatingActionButton(
          onClick = { openDialog = true },
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Geofication")
        }
      }
    }

    //if(!isMapLoaded) {
    /*AnimatedVisibility(
      visible = !isMapLoaded,
      modifier = Modifier.matchParentSize(),
      enter = EnterTransition.None,
      exit = fadeOut()
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .wrapContentSize()
      )
    }*/
    //}
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .zIndex(1f)
        .padding(0.dp, topPadding + 8.dp, 0.dp, 8.dp),
    ) {
      Column {
        LocationSearchBar(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          MainScope().launch {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
          }
        }
        Row(Modifier.horizontalScroll(geoficationsRow)) {
          Spacer(Modifier.width(16.dp))
          geoficationsArray.filter {
            it.active
          }.sortedBy {
            val fence = geofencesArray.first { it2 ->
              it2.gid == it.fenceid
            }

            SphericalUtil.computeDistanceBetween(
              LatLng(fence.latitude, fence.longitude),
              currentLocation
            )
          }.forEach {

            val fence = geofencesArray.first { it2 ->
              it2.gid == it.fenceid
            }

            val distance = SphericalUtil.computeDistanceBetween(
              LatLng(fence.latitude, fence.longitude),
              currentLocation
            ).roundToInt()

            val meterText = if (distance > 1000) {
              "${distance / 1000} km"
            } else {
              "${distance} m"
            }

            AssistChip(
              shape = CircleShape,
              border = AssistChipDefaults.assistChipBorder(enabled = false),
              colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              onClick = {
                MainScope().launch {
                  cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                      CameraPosition(
                        LatLng(fence.latitude, fence.longitude),
                        15f,
                        0f,
                        0f
                      )
                    )
                  )
                }
              },
              label = {
                Text(
                  "${it.gid.take(15).trim()}${if (it.gid.length > 15) "..." else ""} | $meterText"
                )
              },
              leadingIcon = {
                CircleWithColor(
                  color = it.color.color,
                  radius = 8.dp
                )
              })
            Spacer(Modifier.width(8.dp))
          }
          Spacer(Modifier.width(8.dp))
        }

      }
    }

    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapLongClick = {
        Vibrate.vibrate(context, 15)
        tempGeofenceLocation = it
        //longClick(it)
      },
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectDragGesturesAfterLongPress(onDrag = { change, dragAmount ->
            if (tempGeofenceLocation != null) {
              //println(change)
              println(dragAmount)
              //tempGeofenceRadius = ((abs(dragAmount.x) + abs(dragAmount.y)) * 10).toInt()
              val projection = cameraPositionState.projection
              if (projection != null) {
                val pointerLocation = projection.fromScreenLocation(
                  Point(
                    change.position.x.toInt(),
                    change.position.y.toInt()
                  )
                )
                tempGeofenceRadius =
                  SphericalUtil.computeDistanceBetween(tempGeofenceLocation, pointerLocation)
                if (dragAmount.x > 0.5 || dragAmount.y > 0.5) {
                  Vibrate.vibrate(context, 1)
                }
            }
          }
        }, onDragCancel = {
        tempGeofenceLocation?.let { longClick(it, tempGeofenceRadius) }
        tempGeofenceLocation = null
        tempGeofenceRadius = 0.0
      }, onDragEnd = {
        tempGeofenceLocation?.let { longClick(it, tempGeofenceRadius) }
        tempGeofenceLocation = null
        tempGeofenceRadius = 0.0
      })
  },
  cameraPositionState = cameraPositionState,
  onMapLoaded = {
    isMapLoaded = true
  },
  contentPadding = PaddingValues.Absolute(0.dp, 60.dp, 0.dp, 0.dp),
  ) {

    tempGeofenceLocation?.let {
      Circle(
        center = LatLng(it.latitude, it.longitude),
        radius = tempGeofenceRadius,
        strokeColor = Color.Red,
        fillColor = Color.Red.copy(alpha = 0.25f)
      )
    }

    // Place each Geofence as Marker and Circle on the Map
    geofencesArray.forEach { geo ->
      val bdf = BitmapDescriptorFactory.defaultMarker(geo.color.hue)

      val mState = MarkerState(position = LatLng(geo.latitude, geo.longitude))
      MarkerInfoWindow(
        alpha = if(geo.active) 1.0f else 0.5f,
        state = mState,
        title = geo.gid,
        onClick = {
          markerPopupVisible = true
          selectedMarkerId = geo.gid
          MainScope().launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                  LatLng(geo.latitude, geo.longitude),
                  15f,
                  0f,
                  0f
                )
              )
            )
          }
          false
        },
        icon = bdf,
        content = { _ ->
          Text("")
        }

      )
      Circle(
        center = LatLng(geo.latitude, geo.longitude),
        radius = geo.radius.toDouble(),
        strokeColor = if(geo.active) geo.color.color else geo.color.color.copy(alpha = 0.4f),
        fillColor = if(geo.active) geo.color.color.copy(alpha = 0.25f) else geo.color.color.copy(alpha = 0.125f)
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
