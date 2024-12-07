package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.CameraPositionState
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.util.storage.geofence.Geofence
import de.tobibrtnr.geofication.util.storage.geofication.Geofication

// A chip that will be displayed below the search bar in
// order to quickly navigate to an active Geofication.
@Composable
fun GeoficationChip(geofence: Geofence, geofication: Geofication, meterText: String, cameraPositionState: CameraPositionState) {
  AssistChip(
    shape = CircleShape,
    border = AssistChipDefaults.assistChipBorder(enabled = false),
    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    onClick = {
      animateCameraToGeofence(cameraPositionState, geofence)
    },
    label = {
      Text(
        "${
          geofication.message.take(15).trim()
        }${if (geofication.message.length > 15) "..." else ""} | $meterText"
      )
    },
    leadingIcon = {
      CircleWithColor(
        color = geofence.color.color,
        radius = 8.dp
      )
    }
  )
}
