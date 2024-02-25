package de.tobibrtnr.geofication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tobibrtnr.geofication.util.AppDatabase
import de.tobibrtnr.geofication.util.Geofence
import de.tobibrtnr.geofication.util.GeofenceUtil
import de.tobibrtnr.geofication.util.Geofication

@Composable
fun GeoficationsScreen(
  modifier: Modifier = Modifier
) {

  var geoficationsArray by remember { mutableStateOf(emptyList<Geofication>()) }
  LaunchedEffect(Unit) {
    val geofications = GeofenceUtil.getGeofications()
    geoficationsArray = geofications
  }

  Text("${geoficationsArray.size} Geofication(s) created.")

  LazyColumn {
    items(geoficationsArray) {
      ListItem(it)
    }
  }
}

@Composable
fun ListItem(geofication: Geofication) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(geofication.gid)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Geofence: ${geofication.fenceid}")
  }
}
