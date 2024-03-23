package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication
import de.tobibrtnr.geofication.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun GeofencesScreen(
  modifier: Modifier = Modifier
) {

  var geofencesArray by remember { mutableStateOf(emptyList<Geofence>()) }
  LaunchedEffect(Unit) {
    val geofences = GeofenceUtil.getGeofences()
    geofencesArray = geofences
  }

  // UI
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(text = "Geofences", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text("${geofencesArray.size} Geofence${if (geofencesArray.size == 1) "" else "s"} created.")
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(geofencesArray) {
        ListItem(it) {
          val geofences = GeofenceUtil.getGeofences()
          geofencesArray = geofences
        }
      }

    }
  }
}

@Composable
fun ListItem(geofence: Geofence, refreshData: suspend () -> Unit) {
  Card(
    colors = CardDefaults.cardColors()
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .padding(16.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row {
          CircleWithColor(color = geofence.color.color, radius = 15.dp, modifier = Modifier.shadow(4.dp, CircleShape))
          Spacer(Modifier.width(8.dp))
          Row {
            Text(
              modifier = Modifier.fillMaxWidth(0.75f),
              text = geofence.gid,
              style = MaterialTheme.typography.headlineSmall,
              fontStyle = if (geofence.active) FontStyle.Normal else FontStyle.Italic,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Switch(checked = geofence.active, onCheckedChange = {
          CoroutineScope(SupervisorJob()).launch {
            GeofenceUtil.setFenceActive(geofence.gid, it)
            refreshData()
          }
        })
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Radius: ${geofence.radius}m", maxLines = 1, overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(4.dp))
      Text("Location: ${String.format("%.4f", geofence.latitude)}, ${String.format("%.4f", geofence.longitude)}")
      Spacer(modifier = Modifier.height(4.dp))
      Text("Trigger Count: ${geofence.triggerCount}")
    }
  }
  Spacer(modifier = Modifier.height(8.dp))
}
