package de.tobibrtnr.geofication.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.tobibrtnr.geofication.ui.common.CircleWithColor
import de.tobibrtnr.geofication.util.storage.Geofence
import de.tobibrtnr.geofication.util.storage.GeofenceUtil
import de.tobibrtnr.geofication.util.storage.Geofication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GeoficationPopup(selectedMarkerId: Int, onDismissRequest: () -> Unit, onDeleteRequest: () -> Unit) {
  Dialog(onDismissRequest = onDismissRequest) {
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
        Spacer(modifier = Modifier.height(16.dp))
        var selectedGeofenceNotifs by remember { mutableStateOf(emptyList<Geofication>()) }
        var selectedGeofence by remember { mutableStateOf<Geofence?>(null) }
        LaunchedEffect(Unit) {
          selectedGeofenceNotifs = GeofenceUtil.getGeoficationByGeofence(selectedMarkerId)
          selectedGeofence = GeofenceUtil.getGeofenceById(selectedMarkerId)
        }
        selectedGeofence?.let {
          selectedGeofenceNotifs.forEach {
            Row {
              CircleWithColor(color = selectedGeofence!!.color.color, radius = 8.dp)
              Spacer(modifier = Modifier.width(16.dp))
              Text(
                it.message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
              )
              Spacer(modifier = Modifier.width(16.dp))
              Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Geofication",
                modifier = Modifier
                  .clickable {
                    onDeleteRequest()
                  }
              )
            }
            Spacer(Modifier.height(8.dp))

            Row { Text(selectedGeofence!!.fenceName) }
          }
        }
      }
    }
  }
}