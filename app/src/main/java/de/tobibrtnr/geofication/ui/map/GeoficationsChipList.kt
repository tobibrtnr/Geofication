package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import de.tobibrtnr.geofication.util.storage.setting.UnitUtil
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication
import kotlin.math.roundToInt

/**
 * A horizontal list of chips that displays every active Geofication with the
 * distance to the current position.
 */
@Composable
fun GeoficationsChipList(
  geoficationsArray: List<Geofication>,
  geofencesArray: List<Geofence>,
  currentLocation: LatLng,
  cameraPositionState: CameraPositionState
) {

  val geoficationsRow = rememberScrollState()

  Row(Modifier.horizontalScroll(geoficationsRow)) {
    Spacer(Modifier.width(16.dp))
    geoficationsArray.filter {
      it.active
    }.sortedBy {
      val fence = geofencesArray.firstOrNull { it2 ->
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
