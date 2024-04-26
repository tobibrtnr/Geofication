package de.tobibrtnr.geofication.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.DeleteConfirmPopup
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  topPadding: Dp,
  openGeoId: Int?,
  mapViewModel: MapViewModel = viewModel()
) {

  var uiSettings by remember {
    mutableStateOf(
      MapUiSettings(
        zoomControlsEnabled = false,
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false,
        compassEnabled = false, // TODO maybe add own compass component and position
      )
    )
  }

  val context = LocalContext.current

  val tmpGeofenceOpen = runBlocking {
    if (openGeoId != null && openGeoId > 0) GeofenceUtil.getGeofenceById(openGeoId) else null
  }

  var openedGeofence by remember { mutableStateOf(tmpGeofenceOpen) }

  val mapStyleOptions = if (isSystemInDarkTheme()) MapStyleOptions.loadRawResourceStyle(
    context,
    R.raw.google_maps_style_aubergine
  ) else null

  var properties by remember {
    mutableStateOf(
      MapProperties(
        isMyLocationEnabled = true,
        isBuildingEnabled = true,
        mapStyleOptions = mapStyleOptions,
        mapType = MapType.NORMAL
      )
    )
  }

  // TextField Focus (search bar)
  val focusRequester by remember { mutableStateOf(FocusRequester()) }
  val focusManager = LocalFocusManager.current

  var resultsShown by remember { mutableStateOf(false) }

  // Use mapState for holding data
  //val mapState by mapViewModel.uiState.collectAsState()
  val geofencesArray by mapViewModel.geofencesArray.collectAsState()
  val geoficationsArray by mapViewModel.geoficationsArray.collectAsState()

  // Fetch all active geofences from storage
  //var geofencesArray by remember { mutableStateOf(emptyList<Geofence>()) }
  //var geoficationsArray by remember { mutableStateOf(emptyList<Geofication>()) }

  var tempGeofenceLocation by remember { mutableStateOf<LatLng?>(null) }
  var tempGeofenceRadius by remember { mutableStateOf(30.0) }

  var openDialogGeofence by remember { mutableStateOf(false) }

  var selectedPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
  var newRadius by remember { mutableStateOf(0.0) }

  var isMapLoaded by remember { mutableStateOf(false) }

  var markerPopupVisible by remember { mutableStateOf(false) }
  var selectedMarkerId by remember { mutableStateOf(-1) }

  val geoficationsRow = rememberScrollState()

  val cameraPositionState = rememberCameraPositionState {
    CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
  }

  val searchInputState = remember { mutableStateOf("") }
  var searchInput by searchInputState

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

      if (location == null) {
        println("location is null!?")
        return@LaunchedEffect
      }

      currentLocation = LatLng(location.latitude, location.longitude)

      // Update the camera position state with the current location
      cameraPositionState.position =
        CameraPosition(currentLocation, 15f, 0f, 0f)
    }
  }

  fun longClick(latLng: LatLng, tempGeofenceRadius: Double) {
    newRadius = tempGeofenceRadius
    selectedPosition = latLng
    openDialogGeofence = true
  }

  fun removeFocusFromSearchBar() {
    focusManager.clearFocus()
    resultsShown = false
  }

  // Return Composable
  if (markerPopupVisible && selectedMarkerId >= 0) {
    EditGeoficationPopup(selectedMarkerId, {
      markerPopupVisible = false
      openedGeofence = null
    }, {
      CoroutineScope(Dispatchers.Default).launch {
        GeofenceUtil.deleteGeofence(selectedMarkerId)
      }
      markerPopupVisible = false
      openedGeofence = null
    })
  }

  // Move to geofence if one is selected on startup
  if (openedGeofence != null) {
    markerPopupVisible = true
    selectedMarkerId = openedGeofence!!.id
    MainScope().launch {
      val geoLocation = LatLng(openedGeofence!!.latitude, openedGeofence!!.longitude)

      cameraPositionState.position =
        CameraPosition(geoLocation, 15f, 0f, 0f)
    }
  }

  if (openDialogGeofence) {
    AddGeofencePopup(selectedPosition, newRadius) {
      openDialogGeofence = false
    }
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
            removeFocusFromSearchBar()
            MainScope().launch {
              // Asynchronously set the position of the map camera to current position
              val locationClient = ServiceProvider.location()
              val location = locationClient.lastLocation.await()

              if (location == null) {
                println("location is null!?")
                return@launch
              }

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
          onClick = {
            properties = properties.copy(
              mapType = if (properties.mapType == MapType.NORMAL) {
                MapType.HYBRID
              } else {
                MapType.NORMAL
              }
            )
            removeFocusFromSearchBar()
          },
        ) {
          Icon(
            if (properties.mapType == MapType.NORMAL) Icons.Filled.Satellite else Icons.Filled.Map,
            contentDescription = "Switch Map Type"
          )
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
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
          LocationSearchBar(
            modifier = Modifier
              .weight(1f)
              .focusRequester(focusRequester)
              .onFocusChanged {
                if (it.isFocused) {
                  resultsShown = true
                }
              },
            input = searchInputState
          ) {
            removeFocusFromSearchBar()
            MainScope().launch {
              cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
          }
          DropdownInfoButton(searchInputState) { removeFocusFromSearchBar() }
        }
        if (resultsShown) {
          SearchResultList(searchInputState, searchGlobally = { query ->
            searchLocation(query, context) {
              removeFocusFromSearchBar()
              MainScope().launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
              }
            }
          }, goToLocation = { lat, lng ->
            removeFocusFromSearchBar()
            MainScope().launch {
              cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
            }
          })
        } else {
          Row(Modifier.horizontalScroll(geoficationsRow)) {
            Spacer(Modifier.width(16.dp))
            geoficationsArray.filter {
              it.active
            }.sortedBy {
              val fence = geofencesArray.firstOrNull() { it2 ->
                it2.id == it.fenceid
              }

              if (fence == null) {
                Double.MAX_VALUE
              } else {
                SphericalUtil.computeDistanceBetween(
                  LatLng(fence.latitude, fence.longitude),
                  currentLocation
                ) - fence.radius
              }
            }.forEach {
              var fence: Geofence? = null
              var meterText = ""
              try {
                fence = geofencesArray.first { it2 ->
                  it2.id == it.fenceid
                }

                val distance = (SphericalUtil.computeDistanceBetween(
                  LatLng(fence.latitude, fence.longitude),
                  currentLocation
                ) - fence.radius).roundToInt()

                meterText =
                  if (distance < 0) {
                    "✅"
                  } else {
                    UnitUtil.appendUnit(distance)
                  }
              } catch (e: NoSuchElementException) {
                println("no such element exception")
              }

              fence?.let { _ ->
                GeoficationChip(fence, it, meterText, cameraPositionState)
              }
              Spacer(Modifier.width(8.dp))
            }
            Spacer(Modifier.width(8.dp))
          }
        }
      }
    }

    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapClick = {
        removeFocusFromSearchBar()
      },
      onMapLongClick = {
        Vibrate.vibrate(context, 15)
        tempGeofenceLocation = it

        uiSettings = uiSettings.copy(
          scrollGesturesEnabled = false,
          zoomGesturesEnabled = false,
          tiltGesturesEnabled = false,
          rotationGesturesEnabled = false
        )
      },
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          awaitEachGesture {
            do {
              val event = awaitPointerEvent()
              event.changes.forEach {
                when (event.type) {
                  PointerEventType.Move -> {
                    if (tempGeofenceLocation != null) {
                      val projection = cameraPositionState.projection
                      if (projection != null) {
                        val pointerLocation = projection.fromScreenLocation(
                          Point(
                            it.position.x.toInt(),
                            it.position.y.toInt()
                          )
                        )
                        tempGeofenceRadius =
                          SphericalUtil.computeDistanceBetween(
                            tempGeofenceLocation,
                            pointerLocation
                          )
                        if (tempGeofenceRadius < 30.0) tempGeofenceRadius = 30.0
                        /*if (dragAmount.x > 0.5 || dragAmount.y > 0.5) {
                          Vibrate.vibrate(context, 1)
                        }*/
                      }
                    }
                  }

                  PointerEventType.Release -> {
                    uiSettings = uiSettings.copy(
                      scrollGesturesEnabled = true,
                      zoomGesturesEnabled = true,
                      tiltGesturesEnabled = true,
                      rotationGesturesEnabled = true
                    )
                    tempGeofenceLocation?.let { longClick(it, tempGeofenceRadius) }
                    tempGeofenceLocation = null
                    tempGeofenceRadius = 30.0
                  }
                }
              }
            } while (event.changes.any { it.pressed })
          }
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
          strokeColor = MarkerColor.RED.color,
          fillColor = MarkerColor.RED.color.copy(alpha = 0.25f)
        )
      }

      // Place each Geofence as Marker and Circle on the Map
      geofencesArray.forEach { geo ->
        MarkerCircle(geo) {
          markerPopupVisible = true
          selectedMarkerId = geo.id
          MainScope().launch {
            cameraPositionState.animate(
              CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                  LatLng(geo.latitude, geo.longitude), 15f, 0f, 0f
                )
              )
            )
          }
        }
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
