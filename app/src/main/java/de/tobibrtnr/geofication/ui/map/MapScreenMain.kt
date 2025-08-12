package de.tobibrtnr.geofication.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import de.tobibrtnr.geofication.R
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.util.misc.ServiceProvider
import de.tobibrtnr.geofication.util.misc.Vibrate
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.GeoficationViewModel
import de.tobibrtnr.geofication.util.storage.setting.SettingsUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

// The main screen of the app. Here, the all Geofications
// are displayed on a map, and you can also add and edit
// Geofications.
@Composable
fun MapScreenMain(
  topPadding: Dp,
  openGeoId: Int?,
  edit: Boolean?,
  intentQuery: String,
  navController: NavHostController,
  geofenceViewModel: GeofenceViewModel,
  geoficationViewModel: GeoficationViewModel
) {
  val context = LocalContext.current

  val isDarkTheme = isSystemInDarkTheme()
  val currentThemeState by rememberUpdatedState(isDarkTheme)

  // Map style settings
  var uiSettings by remember {
    mutableStateOf(
      MapUiSettings(
        zoomControlsEnabled = false,
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false,
        compassEnabled = false,
      )
    )
  }

  val mapStyleOptions = if (currentThemeState) MapStyleOptions.loadRawResourceStyle(
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

  // Geofications and geofence data. Use mapState for holding data
  val geofencesArray by geofenceViewModel.getAllFlow.collectAsState()
  val geoficationsArray by geoficationViewModel.getAllFlow.collectAsState()

  // Temporary Geofence that is being created
  var tempGeofenceLocation by remember { mutableStateOf<LatLng?>(null) }
  var tempGeofenceRadius by remember { mutableDoubleStateOf(30.0) }

  var openDialogGeofence by remember { mutableStateOf(false) }

  var selectedPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
  var newRadius by remember { mutableDoubleStateOf(0.0) }

  var markerPopupVisible by remember { mutableStateOf(false) }
  var selectedMarkerId by remember { mutableIntStateOf(-1) }


  val cameraPositionState = rememberCameraPositionState {
    CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
  }

  val searchInputState = remember { mutableStateOf("") }

  var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

  val gestureCoroutineScope = rememberCoroutineScope()

  var firstStartup by remember { mutableStateOf(SettingsUtil.getFirstStartup()) }

  var showLoadingHints by remember { mutableIntStateOf(0) }

  // Animate the blur effect
  val blurRadius by animateDpAsState(
    targetValue = if (firstStartup) 4.dp else 0.dp,
    animationSpec = tween(durationMillis = 200),
    label = "blur_animation"
  )

  // Animate alpha on Map Load
  var isMapLoaded by remember { mutableStateOf(false) }

  val alpha by animateFloatAsState(
    targetValue = if (isMapLoaded) 1f else 0.1f,
    animationSpec = tween(durationMillis = 200), label = "mapAlpha"
  )

  // Composable route parameters
  var openedGeofence by remember { mutableStateOf<Geofence?>(null) }
  var usedGeoId by remember { mutableIntStateOf(openGeoId ?: 0) }

  var usedEdit by remember { mutableStateOf(edit ?: false) }

  // Outline for the custom search bar
  var searchBarOutline = if (currentThemeState) {
    Color.DarkGray
  } else {
    Color.LightGray
  }

  // On Theme Update, update outline and Map style
  LaunchedEffect(currentThemeState) {
    searchBarOutline = if (currentThemeState) {
      Color.DarkGray
    } else {
      Color.LightGray
    }

    val newMapStyleOptions = if (currentThemeState) MapStyleOptions.loadRawResourceStyle(
      context,
      R.raw.google_maps_style_dark_mode
    ) else null

    properties = properties.copy(mapStyleOptions = newMapStyleOptions)
  }

  // When the map is opened, go to the
  // currently selected Geofication entry.
  LaunchedEffect(Unit) {
    if (usedGeoId > 0) {
      val tmpGeofence = geofencesArray.find { it.id == usedGeoId }
      if (usedEdit) {
        openedGeofence = tmpGeofence
      } else {
        MainScope().launch {
          tmpGeofence?.let {
            val geoLocation = LatLng(tmpGeofence.latitude, tmpGeofence.longitude)
            cameraPositionState.position = CameraPosition(geoLocation, 15f, 0f, 0f)
            animateCameraToGeofence(cameraPositionState, tmpGeofence)
          }
        }

      }
      usedGeoId = 0
      usedEdit = false
    } else {
      openedGeofence = null
    }
  }

  // Show hints when loading the map takes too long
  LaunchedEffect(Unit) {
    delay(1000)
    showLoadingHints += 1
    delay(3000)
    showLoadingHints += 1
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

      // On first location get, set camera position state with the current location
      if(usedGeoId <= 0) {
        locationClient.lastLocation.await()?.let {
          val newLocation = LatLng(it.latitude, it.longitude)

          currentLocation = newLocation

          cameraPositionState.position =
            CameraPosition(newLocation, 15f, 0f, 0f)
        }
      }

      // After that, use a passive listener for location
      val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_PASSIVE,
        10000L
      ).setWaitForAccurateLocation(false).build()

      val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          locationResult.lastLocation?.let {
            val newLocation = LatLng(it.latitude, it.longitude)
            currentLocation = newLocation
          }
        }
      }

      locationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
      )
    }
  }

  // Launched effect: Everything that should
  // be executed when the screen is opened.
  LaunchedEffect(Unit) {
    // If the app was opened with an intent query ("geo:"),
    // try to search for the location and go to it.
    if (intentQuery.isNotEmpty()) {
      searchGlobally(
        context,
        intentQuery, {
          handleAddresses(it) {
            MainScope().launch {
              cameraPositionState.position =
                CameraPosition(it, 15f, 0f, 0f)
            }
          }
        }
      )
    }

    // Move to geofence if one is selected on startup
    if (openedGeofence != null) {
      markerPopupVisible = true
      selectedMarkerId = openedGeofence!!.id
      MainScope().launch {
        val geoLocation = LatLng(openedGeofence!!.latitude, openedGeofence!!.longitude)

        cameraPositionState.position =
          CameraPosition(geoLocation, 15f, 0f, 0f)

        animateCameraToGeofence(cameraPositionState, openedGeofence!!)
      }
    }
  }

  // On long press on the map, a new Geofication can be created
  fun longClick(latLng: LatLng, tempGeofenceRadius: Double) {
    newRadius = tempGeofenceRadius
    selectedPosition = latLng
    openDialogGeofence = true
  }

  // Function to remove focus from search bar
  fun removeFocusFromSearchBar() {
    focusManager.clearFocus()
    resultsShown = false
  }


  // Popup that is used to edit a Geofication.
  if (markerPopupVisible && selectedMarkerId >= 0) {
    val geofence = geofencesArray.find { it.id == selectedMarkerId }
    val geofication = geoficationsArray.find { it.fenceid == selectedMarkerId }

    geofence?.let {
      geofication?.let {
        EditGeoficationPopup(
          geofence,
          geofication,
          geofenceViewModel, {
            markerPopupVisible = false
            openedGeofence = null
          }, {
            geofenceViewModel.delete(selectedMarkerId)
            markerPopupVisible = false
            openedGeofence = null
          }
        )
      }
    }
  }

  // Popup to add a new Geofication
  if (openDialogGeofence) {
    AddGeofencePopup(selectedPosition, newRadius, geofenceViewModel) {
      openDialogGeofence = false
    }
  }

  // UI
  Box(Modifier.fillMaxSize()) {
    BackHandler(enabled = resultsShown) {
      removeFocusFromSearchBar()
    }
    Box(
      Modifier
        .align(Alignment.BottomEnd)
        .zIndex(1f)
        .padding(16.dp)
    ) {
      // Floating action buttons that can be used to go
      // to current location and toggle satellite view.
      AnimatedVisibility(
        visible = isMapLoaded,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
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
                  animateCamera(cameraPositionState, currentLocation)
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
    }

    // UI elements at the top of the screen
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .zIndex(1f)
        .padding(0.dp, topPadding + 8.dp, 0.dp, 8.dp),
    ) {
      AnimatedVisibility(
        visible = isMapLoaded,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
      ) {
        Column {
          // Search bar text field with dropdown button
          Row(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .clip(CircleShape)
              .border(1.dp, searchBarOutline, CircleShape)
              .shadow(elevation = 16.dp, shape = CircleShape)
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
                animateCamera(cameraPositionState, it)
              }, clearFocus = {
                removeFocusFromSearchBar()
              })
            DropdownInfoButton(navController) { removeFocusFromSearchBar() }
          }

          // List of results for a search query.
          // Shown, if the focus is on the search bar.
          AnimatedVisibility(
            visible = resultsShown
          ) {
            SearchResultList(searchInputState, geoficationViewModel, searchGlobally = { query ->
              searchLocation(query, context, callback = {
                removeFocusFromSearchBar()
                animateCamera(cameraPositionState, it)
              }, clearFocus = {
                removeFocusFromSearchBar()
              })
            }, goToLocation = { lat, lng, radius ->
              removeFocusFromSearchBar()
              animateCameraToGeofence(cameraPositionState, lat, lng, radius)
            })
          }

          // Geofication Chips below the search bar. They
          // are only visible if no search results are shown.
          AnimatedVisibility(
            visible = !resultsShown
          ) {
            Column {
              GeoficationsChipList(
                geoficationsArray,
                geofencesArray,
                currentLocation,
                cameraPositionState
              )
              if (geoficationsArray.none {
                  it.active
                }) {
                Spacer(Modifier.height(8.dp))
              }

              AnimatedVisibility(
                visible = cameraPositionState.position.bearing != 0f,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
              ) {
                Row {
                  Spacer(Modifier.width(16.dp))
                  Compass(
                    Modifier
                      .size(32.dp), cameraPositionState
                  ) {
                    val currPos = cameraPositionState.position
                    animateCamera(cameraPositionState, currPos.target, currPos.zoom, currPos.tilt)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Show a tutorial screen on first startup
    AnimatedVisibility(
      modifier = Modifier.zIndex(100f),
      visible = firstStartup,
      enter = fadeIn(animationSpec = tween(durationMillis = 200)),
      exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
      StartupTutorialScreen()
    }

    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      AnimatedVisibility(
        modifier = Modifier.zIndex(100f),
        visible = showLoadingHints > 0 && !isMapLoaded,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
      ) {
        CircularProgressIndicator(Modifier.size(50.dp), strokeWidth = 6.dp)
      }
      AnimatedVisibility(
        modifier = Modifier.zIndex(100f),
        visible = showLoadingHints > 1 && !isMapLoaded,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
      ) {
        Text(
          text = stringResource(R.string.check_internet_connection),
          modifier = Modifier.padding(top = 110.dp, start = 20.dp, end = 20.dp),
          textAlign = TextAlign.Center
        )
      }
    }

    // Main map composable
    GoogleMap(
      uiSettings = uiSettings,
      properties = properties,
      onMapClick = {
        removeFocusFromSearchBar()
      },
      onMapLoaded = {
        isMapLoaded = true
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
          // On first startup, the first press is
          // used to remove the tutorial screen.
          if (firstStartup) {
            firstStartup = false
            SettingsUtil.setFirstStartup(false)
          }

          // Use gestures to draw a new geofence.
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

                          newTempGeofenceRadius = newTempGeofenceRadius.coerceIn(30.0, 1000000.0)
                          if (abs(newTempGeofenceRadius - tempGeofenceRadius) > 3) {
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
        }
        .then(
          Modifier
            .blur(blurRadius)
            .alpha(alpha)
        ),
      cameraPositionState = cameraPositionState,
      contentPadding = PaddingValues.Absolute(0.dp, 60.dp, 0.dp, 0.dp),
    ) {
      // Marker and Circle for temporary geofence
      // that is being dragged and created right now.
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
          animateCameraToGeofence(cameraPositionState, geo)
        }
      }
    }
  }
}

// Animate the camera to a new position
// and optional zoom, tilt and bearing.
fun animateCamera(
  cameraPositionState: CameraPositionState,
  position: LatLng,
  zoom: Float = 15f,
  tilt: Float = 0f,
  bearing: Float = 0f
) {
  MainScope().launch {
    cameraPositionState.animate(
      CameraUpdateFactory.newCameraPosition(
        CameraPosition(
          position, zoom, tilt, bearing
        )
      )
    )
  }
}

// Animate the camera to a given geofence by its properties.
fun animateCameraToGeofence(
  cameraPositionState: CameraPositionState,
  lat: Double,
  lon: Double,
  radius: Float
) {
  MainScope().launch {
    val pos = LatLng(lat, lon)
    val distance = 2.0 * radius

    // Calculate the southwest and northeast bounds based on the distance
    val southwest = SphericalUtil.computeOffset(pos, distance, 225.0) // 225° SW
    val northeast = SphericalUtil.computeOffset(pos, distance, 45.0)   // 45° NE

    val bounds = LatLngBounds(southwest, northeast)
    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 0))
  }
}

// Animate the camera to a given geofence.
fun animateCameraToGeofence(
  cameraPositionState: CameraPositionState,
  geofence: Geofence
) {
  animateCameraToGeofence(cameraPositionState, geofence.latitude, geofence.longitude, geofence.radius)
}
