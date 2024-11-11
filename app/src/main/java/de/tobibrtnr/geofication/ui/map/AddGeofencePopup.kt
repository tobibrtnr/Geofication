package de.tobibrtnr.geofication.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.LatLng
import de.tobibrtnr.geofication.ui.common.MarkerColor
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofence.GeofenceViewModel
import de.tobibrtnr.geofication.util.storage.geofication.Geofication

fun addGeofence(
  context: Context,
  geofenceViewModel: GeofenceViewModel,
  geofence: Geofence,
  geofication: Geofication
) {
  geofenceViewModel.addGeofence(
    context,
    geofence,
    geofication
  )
}

@Composable
fun AddGeofencePopup(
  pos: LatLng,
  rad: Double,
  geofenceViewModel: GeofenceViewModel,
  onDismissRequest: () -> Unit,
) {
  // New Geofication with default values
  val geofication by remember { mutableStateOf(Geofication(
    fenceid = 0,
    message = "",
    flags = 1,
    delay = 0,
    repeat = true,
    active = true,
    onTrigger = 1,
    triggerCount = 0,
    created = System.currentTimeMillis(),
    lastEdit = System.currentTimeMillis(),
    link = ""
  )) }

  // New Geofence with default values
  val geofence by remember { mutableStateOf(Geofence(
    fenceName = "",
    latitude = pos.latitude,
    longitude = pos.longitude,
    radius = rad.toFloat(),
    color = MarkerColor.RED,
    active = true,
    triggerCount = 0,
    created = System.currentTimeMillis(),
    lastEdit = System.currentTimeMillis()
  )) }

  GeofencePopup(
    geofence,
    geofication,
    geofenceViewModel,
    false,
    ::addGeofence,
    onDismissRequest
  )
}
