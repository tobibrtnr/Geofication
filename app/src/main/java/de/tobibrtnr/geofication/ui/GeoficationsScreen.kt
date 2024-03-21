package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun GeoficationsScreen(
  modifier: Modifier = Modifier
) {

  var geoficationsArray by remember { mutableStateOf(emptyList<Geofication>()) }
  LaunchedEffect(Unit) {
    val geofications = GeofenceUtil.getGeofications()
    geoficationsArray = geofications
  }

  // UI
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(text = "Geofications", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text("${geoficationsArray.size} Geofication${if (geoficationsArray.size == 1) "" else "s"} created.")
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(geoficationsArray) {
        ListItem(it) {
          val geofications = GeofenceUtil.getGeofications()
          geoficationsArray = geofications
        }
      }

    }
  }
}

@Composable
fun ListItem(geofication: Geofication, refreshData: suspend () -> Unit) {
  Card(
    colors = CardDefaults.cardColors()
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row {
          CircleWithColor(color = geofication.color.color, radius = 15.dp)
          Spacer(Modifier.width(8.dp))
          Text(
            geofication.gid,
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = if (geofication.active) FontStyle.Normal else FontStyle.Italic
          )
        }

        Switch(checked = geofication.active, onCheckedChange = {
          CoroutineScope(SupervisorJob()).launch {
            GeofenceUtil.setNotifActive(geofication.gid, it)
            refreshData()
          }
        })
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Geofence: ${geofication.fenceid}")
      Spacer(modifier = Modifier.height(4.dp))
      Text("Trigger Count: ${geofication.triggerCount}")
    }
  }
  Spacer(modifier = Modifier.height(8.dp))

}
