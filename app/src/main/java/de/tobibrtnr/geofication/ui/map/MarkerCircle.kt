package de.tobibrtnr.geofication.ui.map

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import de.tobibrtnr.geofication.util.storage.Geofence
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun MarkerCircle(geofence: Geofence, onClick: () -> Unit) {
  val bdf = BitmapDescriptorFactory.defaultMarker(geofence.color.hue)
  val mState = MarkerState(position = LatLng(geofence.latitude, geofence.longitude))

  MarkerInfoWindow(alpha = if (geofence.active) 1.0f else 0.25f,
    state = mState,
    title = geofence.fenceName,
    onClick = {
      onClick()
      false
    },
    icon = bdf,
    content = { _ ->
      Text("")
    })
  Circle(
    center = LatLng(geofence.latitude, geofence.longitude),
    radius = geofence.radius.toDouble(),
    strokeColor = if (geofence.active) {
      geofence.color.color
    } else {
      geofence.color.color.copy(alpha = 0.3f)
    },
    fillColor = if (geofence.active) {
      geofence.color.color.copy(alpha = 0.25f)
    } else {
      geofence.color.color.copy(
        alpha = 0.1f
      )
    }
  )
}