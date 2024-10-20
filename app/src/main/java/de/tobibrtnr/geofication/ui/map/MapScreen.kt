package de.tobibrtnr.geofication.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.ui.infos.FaqScreen
import de.tobibrtnr.geofication.ui.infos.FeedbackScreen
import de.tobibrtnr.geofication.ui.infos.SupportScreen
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.UnitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

@Composable
fun MapScreen(
  modifier: Modifier = Modifier,
  topPadding: Dp,
  openGeoId: Int?,
  edit: Boolean?,
  intentQuery: String,
  mapViewModel: MapViewModel = viewModel(),
) {
  val mapNavController = rememberNavController()

  NavHost(navController = mapNavController, startDestination = "mapMain") {
    composable("mapMain") {
      MapScreenMain(
        topPadding = topPadding,
        openGeoId = openGeoId,
        edit = edit,
        intentQuery = intentQuery,
        mapViewModel = mapViewModel,
        navController = mapNavController
      )
    }
    composable("faq") {
      FaqScreen(
        navController = mapNavController
      )
    }
    composable("support") {
      SupportScreen(
        navController = mapNavController
      )
    }
    composable("feedback") {
      FeedbackScreen(
        navController = mapNavController
      )
    }
  }
}

@Composable
fun MapScreenMain(
  modifier: Modifier = Modifier,
  topPadding: Dp,
  openGeoId: Int?,
  edit: Boolean?,
  intentQuery: String,
  mapViewModel: MapViewModel,
  navController: NavHostController
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

  val mapStyleOptions = if (isSystemInDarkTheme()) MapStyleOptions.loadRawResourceStyle(
    context,
    R.raw.google_maps_style_dark_mode
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
  val geofencesArray by mapViewModel.geofencesArray.collectAsState()
  val geoficationsArray by mapViewModel.geoficationsArray.collectAsState()

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

  var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

  val gestureCoroutineScope = rememberCoroutineScope()

  /*
   * Check Composable Route Parameters
   */


  var openedGeofence by remember { mutableStateOf<Geofence?>(null) }
  var usedGeoId by remember { mutableStateOf(openGeoId ?: 0) }

  var usedEdit by remember { mutableStateOf(edit ?: false) }

  LaunchedEffect(Unit) {
    if (usedGeoId > 0) {
      val tmpGeofence = GeofenceUtil.getGeofenceById(usedGeoId)
      if (usedEdit) {
        openedGeofence = tmpGeofence
      } else {
        MainScope().launch {
          val geoLocation = LatLng(tmpGeofence.latitude, tmpGeofence.longitude)

          cameraPositionState.position = CameraPosition(geoLocation, 15f, 0f, 0f)
        }
      }
      usedGeoId = 0
      usedEdit = false
    } else {
      openedGeofence = null
    }
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
      if (usedGeoId <= 0) {
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

  LaunchedEffect(Unit) {
    if (intentQuery.isNotEmpty()) {
      val geocoder = Geocoder(context)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Implementation of GeocodeListener
        val listener = object : Geocoder.GeocodeListener {
          override fun onGeocode(p0: MutableList<Address>) {
            if (p0.isNotEmpty()) {
              handleAddresses(p0) {
                MainScope().launch {
                  cameraPositionState.position =
                    CameraPosition(it, 15f, 0f, 0f)
                }
              }
            }
          }

          override fun onError(errorMessage: String?) {
            println(errorMessage)
          }
        }
        geocoder.getFromLocationName(intentQuery, 1, listener)
      } else {
        val addresses = geocoder.getFromLocationName(intentQuery, 1)
        if (addresses != null) {
          handleAddresses(addresses) {
            MainScope().launch {
              cameraPositionState.position =
                CameraPosition(it, 15f, 0f, 0f)
            }
          }
        }
      }
    }
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

            if (SphericalUtil.computeDistanceBetween(
                cameraPositionState.position.target,
                currentLocation
              ) > 0.1
            ) {
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
            contentDescription = stringResource(R.string.get_location)
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
            contentDescription = stringResource(R.string.switch_map_type)
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
        Row(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(CircleShape)
            .border(
              1.dp,
              Color.LightGray,
              CircleShape
            )
            .shadow(
              elevation = 16.dp,
              shape = CircleShape
            )
        ) {
          LocationSearchBar(
            modifier = Modifier
              .weight(1f)
              .focusRequester(focusRequester)
              .onFocusChanged {
                if (it.isFocused) {
                  resultsShown = true
                }
              },
            input = searchInputState,
            callback = {
              removeFocusFromSearchBar()
              MainScope().launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
              }
            }, clearFocus = {
              removeFocusFromSearchBar()
            })
          DropdownInfoButton(searchInputState, navController) { removeFocusFromSearchBar() }
        }
        if (resultsShown) {
          SearchResultList(searchInputState, searchGlobally = { query ->
            searchLocation(query, context, callback = {
              removeFocusFromSearchBar()
              MainScope().launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
              }
            }, clearFocus = {
              removeFocusFromSearchBar()
            })
          }, goToLocation = { lat, lng ->
            removeFocusFromSearchBar()
            MainScope().launch {
              cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                  LatLng(lat, lng),
                  15f
                )
              )
            }
          })
        } else {
          Column {
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
            if (geoficationsArray.filter {
                it.active
              }.isEmpty()) {
              Spacer(Modifier.height(8.dp))
            }
            if (cameraPositionState.position.bearing != 0f) {
              Row {
                Spacer(Modifier.width(16.dp))
                Compass(
                  Modifier
                    .size(32.dp), cameraPositionState
                ) {
                  MainScope().launch {
                    cameraPositionState.animate(
                      CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(cameraPositionState.position)
                          .bearing(0f)
                          .build()
                      )
                    )
                  }
                }
              }
            }
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

                    gestureCoroutineScope.launch {
                      if (tempGeofenceLocation != null) {
                        val projection = cameraPositionState.projection
                        if (projection != null) {
                          val pointerLocation = projection.fromScreenLocation(
                            Point(
                              it.position.x.toInt(),
                              it.position.y.toInt()
                            )
                          )
                          var newTempGeofenceRadius =
                            SphericalUtil.computeDistanceBetween(
                              tempGeofenceLocation,
                              pointerLocation
                            )
                          if (newTempGeofenceRadius < 30.0) newTempGeofenceRadius = 30.0
                          if (Math.abs(newTempGeofenceRadius - tempGeofenceRadius) > 3) {
                            Vibrate.vibrate(context, 1)
                          }
                          tempGeofenceRadius = newTempGeofenceRadius
                        }
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
        Marker(
          state = MarkerState(
            position = LatLng(
              tempGeofenceLocation!!.latitude,
              tempGeofenceLocation!!.longitude
            )
          )
        )
      }

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

    ScaleBar(
      modifier = Modifier
        .padding(bottom = 10.dp, end = 105.dp)
        .align(Alignment.BottomEnd),
      cameraPositionState = cameraPositionState
    )
  }
}
